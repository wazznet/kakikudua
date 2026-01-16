package com.wazzgroup.penagihanwifi.settings.printer;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.wazzgroup.penagihanwifi.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.dantsu.escposprinter.EscPosPrinter;

import java.util.ArrayList;
import java.util.Set;

public class PrinterActivity extends AppCompatActivity {

    private static final String TAG = "PRINTER_DEBUG";

    private BluetoothAdapter bluetoothAdapter;
    private ListView listPrinter;
    private Button btnScan, btnPrint;

    private final ArrayList<BluetoothConnection> printerList = new ArrayList<>();
    private BluetoothConnection selectedPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);

        Log.d(TAG, "PrinterActivity START");

        listPrinter = findViewById(R.id.listPrinter);
        btnScan = findViewById(R.id.btnScan);
        btnPrint = findViewById(R.id.btnPrint);

        btnPrint.setEnabled(false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth NOT supported");
            Toast.makeText(this, "Bluetooth tidak didukung", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        checkPermission();

        btnScan.setOnClickListener(v -> {
            Log.d(TAG, "Klik SCAN PRINTER");
            scanPrinter();
        });

        btnPrint.setOnClickListener(v -> {
            Log.d(TAG, "Klik PRINT");
            printTest();
        });
    }

    // ================= PERMISSION =================
    private void checkPermission() {
        Log.d(TAG, "Check permission");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                    }, 100);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "Permission result");

        for (int i = 0; i < permissions.length; i++) {
            Log.d(TAG, permissions[i] + " = " +
                    (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));
        }
    }

    // ================= SCAN PRINTER =================
    private void scanPrinter() {

        Log.d(TAG, "Scan printer start");

        if (!bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth OFF");
            Toast.makeText(this, "Bluetooth belum aktif", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "BLUETOOTH_CONNECT permission denied");
            Toast.makeText(this, "Permission Bluetooth belum diizinkan", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.d(TAG, "Paired devices count: " + pairedDevices.size());

        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "Tidak ada printer yang dipair", Toast.LENGTH_SHORT).show();
            return;
        }

        printerList.clear();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        for (BluetoothDevice device : pairedDevices) {
            Log.d(TAG, "Device found: " + device.getName() + " | " + device.getAddress());

            printerList.add(new BluetoothConnection(device));
            adapter.add(device.getName() + "\n" + device.getAddress());
        }

        listPrinter.setAdapter(adapter);

        listPrinter.setOnItemClickListener((parent, view, position, id) -> {
            selectedPrinter = printerList.get(position);
            btnPrint.setEnabled(true);

            Log.d(TAG, "Printer selected: " +
                    selectedPrinter.getDevice().getName());

            Toast.makeText(this,
                    "Dipilih: " + selectedPrinter.getDevice().getName(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    // ================= PRINT =================
    private void printTest() {

        if (selectedPrinter == null) {
            Toast.makeText(this, "Printer belum dipilih", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                Log.d(TAG, "Connect printer...");
                selectedPrinter.connect();

                Log.d(TAG, "Create printer instance");
                EscPosPrinter printer = new EscPosPrinter(
                        selectedPrinter,
                        203,
                        48f,
                        32
                );

                Log.d(TAG, "Start printing");
                printer.printFormattedText(
                        "[C]<b>TEST PRINTER</b>\n" +
                                "[C]================\n" +
                                "[L]Tanggal : " + System.currentTimeMillis() + "\n" +
                                "[L]Status  : OK\n\n" +
                                "[C]-- TERIMA KASIH --\n\n"
                );

                selectedPrinter.disconnect(); // ✅ DISCONNECT DI SINI
                Log.d(TAG, "Print SUCCESS");

                runOnUiThread(() ->
                        Toast.makeText(this, "Print berhasil", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                Log.e(TAG, "PRINT ERROR", e);
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Print gagal: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}

package com.wazzgroup.penagihanwifi.settings.printer;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class PrinterSettingActivity extends AppCompatActivity {

    private ListView listPrinter;
    private TextView txtStatusPrinter;
    private Button btnHapusPrinter;

    private final List<BluetoothConnection> printerList = new ArrayList<>();

    private static final int REQUEST_PERMISSION_BT = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_printer_setting);

        initView();
        tampilkanStatusPrinter();

        ImageView kembali = findViewById(R.id.back2);
        kembali.setOnClickListener(v -> kembaliKeList());

        btnHapusPrinter.setOnClickListener(v -> dialogHapusPrinter());

        if (!isBluetoothEnabled()) {
            Toast.makeText(this, "Bluetooth belum aktif", Toast.LENGTH_LONG).show();
            return;
        }

        cekPermissionDanScan();
    }

    // ================= INIT VIEW =================
    private void initView() {
        listPrinter = findViewById(R.id.listPrinter);
        txtStatusPrinter = findViewById(R.id.txtStatusPrinter);
        btnHapusPrinter = findViewById(R.id.btnHapusPrinter);
    }

    // ================= CEK BLUETOOTH =================
    private boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    // ================= STATUS PRINTER =================
    private void tampilkanStatusPrinter() {
        SharedPreferences sp = getSharedPreferences("printer", MODE_PRIVATE);
        String mac = sp.getString("mac", null);

        if (mac != null) {
            txtStatusPrinter.setText("Printer tersimpan:\n" + mac);
        } else {
            txtStatusPrinter.setText("Printer: Belum disetel");
        }
    }

    // ================= CEK PERMISSION =================
    private void cekPermissionDanScan() {

        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }

        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_PERMISSION_BT
            );
        } else {
            scanPrinter();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_BT) {
            scanPrinter();
        }
    }

    // ================= SCAN PRINTER =================
    private void scanPrinter() {

        BluetoothConnection[] printers =
                new BluetoothPrintersConnections().getList();

        if (printers == null || printers.length == 0) {
            Toast.makeText(this,
                    "Printer Bluetooth tidak ditemukan.\nPastikan printer sudah dipair.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        printerList.clear();
        List<String> names = new ArrayList<>();

        for (BluetoothConnection printer : printers) {

            printerList.add(printer);

            try {
                names.add(
                        printer.getDevice().getName() + "\n" +
                                printer.getDevice().getAddress()
                );
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                names) {

            @Override
            public android.view.View getView(int position,
                                             android.view.View convertView,
                                             android.view.ViewGroup parent) {

                android.view.View view = super.getView(position, convertView, parent);

                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(getResources().getColor(android.R.color.black));
                textView.setTextSize(16);

                return view;
            }
        };


        listPrinter.setAdapter(adapter);

        listPrinter.setOnItemClickListener((parent, view, position, id) -> {

            BluetoothConnection selected = printerList.get(position);
            simpanPrinter(selected.getDevice().getAddress());

            Toast.makeText(this,
                    "Printer berhasil disimpan",
                    Toast.LENGTH_SHORT).show();

            tampilkanStatusPrinter();
            finish();
        });
    }

    // ================= SIMPAN PRINTER =================
    private void simpanPrinter(String mac) {
        SharedPreferences sp = getSharedPreferences("printer", MODE_PRIVATE);
        sp.edit().putString("mac", mac).apply();
    }

    // ================= HAPUS PRINTER =================
    private void dialogHapusPrinter() {

        SharedPreferences sp = getSharedPreferences("printer", MODE_PRIVATE);
        String mac = sp.getString("mac", null);

        if (mac == null) {
            Toast.makeText(this,
                    "Belum ada printer yang disimpan",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Hapus Printer")
                .setMessage("Yakin ingin menghapus printer yang tersimpan?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    sp.edit().clear().apply();
                    Toast.makeText(this,
                            "Printer berhasil dihapus",
                            Toast.LENGTH_SHORT).show();
                    tampilkanStatusPrinter();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ================= KEMBALI =================
    private void kembaliKeList() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}

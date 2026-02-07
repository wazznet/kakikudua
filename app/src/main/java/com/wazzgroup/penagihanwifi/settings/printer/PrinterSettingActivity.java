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

import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class PrinterSettingActivity extends AppCompatActivity {

    ListView listPrinter;
    TextView txtStatusPrinter;
    Button btnHapusPrinter;

    List<BluetoothConnection> printerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_printer_setting);

        listPrinter = findViewById(R.id.listPrinter);
        txtStatusPrinter = findViewById(R.id.txtStatusPrinter);
        btnHapusPrinter = findViewById(R.id.btnHapusPrinter);

        ImageView kembali = findViewById(R.id.back2);
        kembali.setOnClickListener(v -> kembaliKeList());
        tampilkanStatusPrinter();

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Toast.makeText(this, "Bluetooth belum aktif", Toast.LENGTH_LONG).show();
            return;
        }

        cekPermission();
        scanPrinter();

        btnHapusPrinter.setOnClickListener(v -> dialogHapusPrinter());
    }

    // ================= STATUS PRINTER =================
    private void tampilkanStatusPrinter() {
        SharedPreferences sp = getSharedPreferences("printer", MODE_PRIVATE);
        String mac = sp.getString("mac", null);

        if (mac != null) {
            txtStatusPrinter.setText("Printer tersimpan: " + mac);
        } else {
            txtStatusPrinter.setText("Printer: Belum disetel");
        }
    }

    // ================= SCAN PRINTER =================
    private void scanPrinter() {
        BluetoothConnection[] printers =
                new BluetoothPrintersConnections().getList();

        if (printers == null || printers.length == 0) {
            Toast.makeText(this, "Printer Bluetooth tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> names = new ArrayList<>();
        printerList.clear();

        for (BluetoothConnection printer : printers) {
            printerList.add(printer);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            names.add(
                    printer.getDevice().getName() + "\n" +
                            printer.getDevice().getAddress()
            );
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        listPrinter.setAdapter(adapter);

        listPrinter.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothConnection selected = printerList.get(position);
            simpanPrinter(selected.getDevice().getAddress());
            Toast.makeText(this, "Printer berhasil disimpan", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Belum ada printer yang disimpan", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Hapus Printer")
                .setMessage("Yakin ingin menghapus printer yang tersimpan?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    sp.edit().clear().apply();
                    Toast.makeText(this, "Printer berhasil dihapus", Toast.LENGTH_SHORT).show();
                    tampilkanStatusPrinter();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ================= PERMISSION =================
    private void cekPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        10
                );
            }
        }
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

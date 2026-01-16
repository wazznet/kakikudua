package com.wazzgroup.penagihanwifi.settings.printer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.wazzgroup.penagihanwifi.R;

public class PrinterTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_test);

        Button btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(v -> printTest());
    }

    private void printTest() {
        try {
            SharedPreferences sp = getSharedPreferences("printer", MODE_PRIVATE);
            String mac = sp.getString("mac", null);

            if (mac == null) {
                Toast.makeText(this, "Printer belum diset", Toast.LENGTH_SHORT).show();
                return;
            }

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = adapter.getRemoteDevice(mac);

            BluetoothConnection connection = new BluetoothConnection(device);

            EscPosPrinter printer =
                    new EscPosPrinter(connection, 203, 48f, 32);

            printer.printFormattedText(
                    "[C]<b>TEST PRINTER</b>\n" +
                            "[C]Printer siap digunakan\n\n\n"
            );

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}

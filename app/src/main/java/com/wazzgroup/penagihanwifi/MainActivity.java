package com.wazzgroup.penagihanwifi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.wazzgroup.penagihanwifi.client.ClientActivity;
import com.wazzgroup.penagihanwifi.komplen.KomplainActivity;
import com.wazzgroup.penagihanwifi.map.MapActivity;
import com.wazzgroup.penagihanwifi.map.OdpActivity;
import com.wazzgroup.penagihanwifi.pembayaran.tagihan.ManajemenPenagihan;
import com.wazzgroup.penagihanwifi.psb.PsbActivity;
import com.wazzgroup.penagihanwifi.settings.SettingsActivity;
import com.wazzgroup.penagihanwifi.tools.PingActivity;
import com.wazzgroup.penagihanwifi.tools.TracerouteActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private Handler handler = new Handler();
    private TextView textMeta,textTiktok,textYoutube,textCloudflare,forr;
    private Runnable pingRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teknisi);
        textMeta = findViewById(R.id.textMeta);
        textTiktok = findViewById(R.id.textTiktok);
        textYoutube = findViewById(R.id.textYoutube);
        textCloudflare = findViewById(R.id.textCloudflare);
        ImageView fotoLogout = findViewById(R.id.profile);

        forr = findViewById(R.id.forr);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        //String iduserr = prefs.getString("iduser", "");
        String akses = prefs.getString("nama_wifi", "");

        forr.setText("for "+akses);

        // Jika foto di klik, langsung pindah ke LogoutActivity
        fotoLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        startRealtimePing();


    }

    // Fungsi dipindah ke luar onCreate()
    public void onLinearLayoutClick(View view) {
        int id = view.getId();

        if (id == R.id.penagihan) {
            startActivity(new Intent(this, ManajemenPenagihan.class));
        } else if (id == R.id.psb) {
            startActivity(new Intent(this, PsbActivity.class));
        }else if (id == R.id.client) {
            startActivity(new Intent(this, ClientActivity.class));
        }else if (id == R.id.map) {
            startActivity(new Intent(this, MapActivity.class));
        }else if (id == R.id.ping) {
            startActivity(new Intent(this, PingActivity.class));
        }else if (id == R.id.traceroute) {
            startActivity(new Intent(this, TracerouteActivity.class));
        }else if (id == R.id.ACS) {
            startActivity(new Intent(this, OdpActivity.class));
        }else if (id == R.id.komplain) {
            startActivity(new Intent(this, KomplainActivity.class));
        }
    }
    private void startRealtimePing() {
        pingRunnable = new Runnable() {
            @Override
            public void run() {
                pingServer("facebook.com", textMeta);
                pingServer("tiktok.com", textTiktok);
                pingServer("youtube.com", textYoutube);
                pingServer("1.1.1.1", textCloudflare);

                handler.postDelayed(this, 1000); // Ulang setiap 5 detik
            }
        };
        handler.post(pingRunnable);
    }

    private void stopRealtimePing() {
        handler.removeCallbacks(pingRunnable);
    }
    private void pingServer(String host, TextView textView) {
        new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 " + host);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                String timeMs = "N/A";

                while ((line = reader.readLine()) != null) {
                    if (line.contains("time=")) {
                        int index = line.indexOf("time=");
                        timeMs = line.substring(index + 5, line.indexOf(" ms", index)) + " ms";
                    }
                }

                process.waitFor();

                final String result =  timeMs;

                runOnUiThread(() -> textView.setText(result));

            } catch (Exception e) {
                runOnUiThread(() -> textView.setText(host + " : Error"));
            }
        }).start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRealtimePing();
    }


}
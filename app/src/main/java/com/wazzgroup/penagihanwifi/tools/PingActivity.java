package com.wazzgroup.penagihanwifi.tools;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
public class PingActivity extends AppCompatActivity {

    private EditText editHost;
    private Button buttonPing, buttonStop;
    private TextView textPingResult;

    private Handler handler = new Handler();
    private Runnable pingRunnable;
    private boolean isPinging = false;
    private TextView  textMeta,textTiktok,textYoutube,textCloudflare,textGoggle,textShopee,textx,textTokopedia;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ping);

        editHost = findViewById(R.id.editHost);
        buttonPing = findViewById(R.id.buttonPing);
        buttonStop = findViewById(R.id.buttonStop);
        textPingResult = findViewById(R.id.textPingResult);

        buttonPing.setOnClickListener(v -> startPing());
        buttonStop.setOnClickListener(v -> stopPing());

        textMeta = findViewById(R.id.textMeta);
        textTiktok = findViewById(R.id.textTiktok);
        textYoutube = findViewById(R.id.textYoutube);
        textCloudflare = findViewById(R.id.textCloudflare);
        textx = findViewById(R.id.textx);
        textShopee = findViewById(R.id.textShopee);
        textTokopedia = findViewById(R.id.textTokopedia);
        textGoggle = findViewById(R.id.textGoggle);
        buttonStop.setVisibility(View.GONE);

        ImageView kembali = findViewById(R.id.back);
        kembali.setOnClickListener(v -> kembaliKeList());

        startRealtimePing();
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, TeknisiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void startPing() {
        String host = editHost.getText().toString().trim();
        if (host.isEmpty()) {
            textPingResult.setText("Host tidak boleh kosong!");
            return;
        }

        isPinging = true;
        buttonPing.setEnabled(false);
        buttonPing.setVisibility(View.GONE);
        buttonStop.setVisibility(View.VISIBLE);
        buttonStop.setEnabled(true);

        pingRunnable = new Runnable() {
            @Override
            public void run() {
                pingHost(host);

                if (isPinging) {
                    handler.postDelayed(this, 1000); // ping tiap 1 detik
                }
            }
        };

        handler.post(pingRunnable);
    }

    private void stopPing() {
        isPinging = false;
        buttonPing.setEnabled(true);
        buttonStop.setEnabled(false);

        buttonPing.setVisibility(View.VISIBLE);
        buttonStop.setVisibility(View.GONE);
    }

    private void pingHost(String host) {
        new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -W 1 " + host);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder result = new StringBuilder();

                String line;
                String time = "timeout";

                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                    if (line.contains("time=")) {
                        int idx = line.indexOf("time=");
                        time = line.substring(idx + 5, line.indexOf(" ms", idx)) + " ms";
                    }
                }

                process.waitFor();

                String output = "ping to "+ host + " : " + time;

                runOnUiThread(() -> textPingResult.setText(output));
            } catch (Exception e) {
                runOnUiThread(() -> textPingResult.setText(host + " : Error"));
            }
        }).start();
    }
    private void startRealtimePing() {
        pingRunnable = new Runnable() {
            @Override
            public void run() {
                pingServer("facebook.com", textMeta);
                pingServer("tiktok.com", textTiktok);
                pingServer("youtube.com", textYoutube);
                pingServer("1.1.1.1", textCloudflare);
                pingServer("x.com", textx);
                pingServer("shopee.co.id", textShopee);
                pingServer("www.tokopedia.com", textTokopedia);
                pingServer("8.8.8.8", textGoggle);

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
        stopPing();
    }
}

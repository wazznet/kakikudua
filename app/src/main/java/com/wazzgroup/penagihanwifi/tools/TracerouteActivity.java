

package com.wazzgroup.penagihanwifi.tools;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.SocketTimeoutException;
public class TracerouteActivity extends AppCompatActivity {

    private EditText etTarget;
    private Button btnTrace;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_traceroute);

        etTarget = findViewById(R.id.etTarget);
        btnTrace = findViewById(R.id.btnTrace);
        tvResult = findViewById(R.id.tvResult);
        ImageView kembali = findViewById(R.id.back);
        kembali.setOnClickListener(v -> kembaliKeList());
        btnTrace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String target = etTarget.getText().toString().trim();
                if (target.isEmpty()) {
                    Toast.makeText(TracerouteActivity.this, "Masukkan IP atau hostname", Toast.LENGTH_SHORT).show();
                    return;
                }

                new TracerouteTask().execute(target);
            }
        });
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, TeknisiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private class TracerouteTask extends AsyncTask<String, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvResult.setText("Memulai traceroute...\n");
            btnTrace.setEnabled(false);
        }

        @Override
        protected Void doInBackground(String... params) {
            String target = params[0];
            try {
                InetAddress address = InetAddress.getByName(target);
                String ip = address.getHostAddress();
                publishProgress("\nTraceroute ke: " + target + " (" + ip + ")\nMax 30 hops\n\n");

                for (int ttl = 1; ttl <= 30; ttl++) {
                    String command = "/system/bin/ping -c 1 -t " + ttl + " " + ip;
                    long startTime = System.currentTimeMillis();

                    Process process = Runtime.getRuntime().exec(command);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    boolean found = false;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("From") || line.contains("ttl") || line.contains("icmp_seq")) {
                            String hopIp = extractIpFromLine(line);
                            String latency = extractTimeFromLine(line);

                            // Jika time tidak ditemukan, hitung manual
                            if (latency.equals("-")) {
                                long endTime = System.currentTimeMillis();
                                latency = (endTime - startTime) + " ms";
                            }

                            if (hopIp != null) {
                                String dnsName = hopIp;
                                try {
                                    InetAddress addr = InetAddress.getByName(hopIp);
                                    String resolvedName = addr.getHostName();
                                    if (!resolvedName.equals(hopIp)) {
                                        dnsName = resolvedName + " [" + hopIp + "]";
                                    }
                                } catch (UnknownHostException e) {
                                    // Jika gagal resolve DNS, tetap pakai IP
                                }

                                publishProgress(ttl + ": " + dnsName + " (" + latency + ")\n");
                                found = true;

                                if (line.contains("icmp_seq") && !line.contains("Time to live exceeded")) {
                                    publishProgress("Target tercapai!\n");
                                    return null;
                                }
                                break;
                            }
                        }
                    }

                    if (!found) {
                        publishProgress(ttl + ": * (timeout)\n");
                    }

                    process.destroy();
                    Thread.sleep(100);
                }

            } catch (UnknownHostException e) {
                publishProgress("Error: Host tidak dikenal\n");
                e.printStackTrace();
            } catch (IOException e) {
                publishProgress("Error: Gagal melakukan traceroute\n");
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String extractIpFromLine(String line) {
            String ipPattern = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
            Pattern pattern = Pattern.compile(ipPattern);
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                return matcher.group();
            }
            return null;
        }

        private String extractTimeFromLine(String line) {
            Pattern pattern = Pattern.compile("time=([0-9.]+)\\s?ms");
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                return matcher.group(1) + " ms";
            }
            return "-";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            tvResult.append(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            btnTrace.setEnabled(true);
            tvResult.append("\nTraceroute selesai.\n");
        }
    }
}

package com.wazzgroup.penagihanwifi.halamanpelanggan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.halamanpelanggan.komplen.PelangganKomplenActivity;
import com.wazzgroup.penagihanwifi.halamanpelanggan.layanan.PelangganLayananActivity;
import com.wazzgroup.penagihanwifi.halamanpelanggan.mywifi.PelangganHostspotActivity;
import com.wazzgroup.penagihanwifi.halamanpelanggan.paket.PelangganPaketActivity;
import com.wazzgroup.penagihanwifi.halamanpelanggan.shop.PelangganPembelianActivity;
import com.wazzgroup.penagihanwifi.halamanpelanggan.shop.PelangganRiwayatActivity;
import com.wazzgroup.penagihanwifi.halamanpelanggan.tagihan.PelangganTagihanActivity;
import com.wazzgroup.penagihanwifi.halamanpelanggan.tv.PelangganTvActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PelangganActivity extends AppCompatActivity {
    private Handler handler = new Handler();
    private TextView  textMeta,textTiktok,textYoutube,textCloudflare,namapelanggannya,saldo;
    private Runnable pingRunnable;
    String url = GlobalHelper.BASE_URL; // Pastikan ini endpoint yang sesuai
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pelanggan2);
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        String nama_pelanggan = prefs.getString("nama_pelanggan", "");
        String id = prefs.getString("idpelannggan", "");

        //Button btnLogout = findViewById(R.id.btnLogout);
        textMeta = findViewById(R.id.textMeta);
        textTiktok = findViewById(R.id.textTiktok);
        textYoutube = findViewById(R.id.textYoutube);
        textCloudflare = findViewById(R.id.textCloudflare);
        namapelanggannya = findViewById(R.id.namapelanggannya);
        //saldo = findViewById(R.id.textView11);
        namapelanggannya.setText(nama_pelanggan);
//        ImageView fotoLogout = findViewById(R.id.profile);
//        // Jika foto di klik, langsung pindah ke LogoutActivity
//        fotoLogout.setOnClickListener(v -> {
//            Intent intent = new Intent(PelangganActivity.this, LogoutActivity.class);
//            startActivity(intent);
//        });
//        ceksaldo(id);
        startRealtimePing();

    }
    private void ceksaldo(String id) {
        RequestBody formBody = new FormBody.Builder()
                .add("api", "ceksaldo") // Pastikan API PHP mendukung login dengan ID
                .add("id", id)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PelangganActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getString("status").equals("success")) {
                            String saldopelanggan = obj.getString("saldopelanggan");
                            saldo.setText("Rp. "+ saldopelanggan);

                        } else {
                            Toast.makeText(PelangganActivity.this, "Login gagal: " + obj.getString("message"), Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(PelangganActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    public void onLinearLayoutClick(View view) {
        int id = view.getId();

        if (id == R.id.penagihan) {
            startActivity(new Intent(this, PelangganTagihanActivity.class));
        } else if (id == R.id.pembelian) {
            startActivity(new Intent(this, PelangganPembelianActivity.class));
        }else if (id == R.id.riwayat) {
            startActivity(new Intent(this, PelangganRiwayatActivity.class));
        }else if (id == R.id.paket) {
            startActivity(new Intent(this, PelangganPaketActivity.class));
        }else if (id == R.id.tv) {
            startActivity(new Intent(this, PelangganTvActivity.class));
        }else if (id == R.id.hostpot) {
            startActivity(new Intent(this, PelangganHostspotActivity.class));
        }else if (id == R.id.layanan) {
            startActivity(new Intent(this, PelangganLayananActivity.class));
        }else if (id == R.id.komplain) {
            startActivity(new Intent(this, PelangganKomplenActivity.class));
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
                String timeMs = "timeout";

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
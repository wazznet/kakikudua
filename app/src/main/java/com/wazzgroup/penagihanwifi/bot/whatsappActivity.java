package com.wazzgroup.penagihanwifi.bot;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Looper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import okhttp3.FormBody;
import okhttp3.Response;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
public class whatsappActivity extends AppCompatActivity {
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    Button btnSend,btnRequest,hapusRequest;
    private int interval = 5000; // interval cek tiap 5 detik

    private Handler handler;
    private Runnable runnable;
    String url = GlobalHelper.BASE_URL;
    String urlwa = GlobalHelper.BASE_URL_wa;
    ImageView qrImageView;
    private int failedCount = 0;
    String iduserr ;
    TextView statuswa,statuskoneksi;
    private static final int MAX_FAILED = 20;
    static final int INTERVAL = 5000; // 5 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_whatsapp);
        qrImageView = findViewById(R.id.imageView14);
        statuswa = findViewById(R.id.statuswa);
        statuskoneksi = findViewById(R.id.statuskoneksi);
        ImageView kewa = findViewById(R.id.back2);
        kewa.setOnClickListener(v -> {
            Intent intent = new Intent(whatsappActivity.this, BotPenagihanActivity.class);
            startActivity(intent);
        });
        btnRequest = findViewById(R.id.qr);
        hapusRequest = findViewById(R.id.hapus);
        iduserr = GlobalHelper.getIdUser(this);
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestakun(iduserr);
                btnRequest.setEnabled(false);

            }
        });
        hapusRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hapusakun(iduserr);
                hapusRequest.setEnabled(false);

            }
        });
        iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "cekwapi")
                .add("user", iduserr)
                .build();

        Request httpRequest = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(httpRequest).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(whatsappActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        int key = Integer.parseInt(json.getString("key"));
                        if (key == 0) {
                              btnRequest.setVisibility(View.VISIBLE);
                            hapusRequest.setVisibility(View.GONE);
                            statuswa.setText("Silakan Buat Sessions Baru");
                        } else {

                            statuswa.setText("Sessions Telah Di buat ");
                            btnRequest.setVisibility(View.GONE);

                            hapusRequest.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(whatsappActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Aksi ketika tombol ditekan
        startAutoCheck();
    }
    private void startAutoCheck() {
        iduserr = GlobalHelper.getIdUser(this);
        String keynya = iduserr + "11";

        handler = new Handler(Looper.getMainLooper());

        runnable = new Runnable() {
            @Override
            public void run() {

                String url = urlwa + "/session/" + keynya + "/status";

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() ->
                                statuskoneksi.setText("❌ Gagal koneksi ke server")
                        );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (!response.isSuccessful()) return;

                        try {
                            JSONObject obj = new JSONObject(response.body().string());

                            if ("success".equalsIgnoreCase(obj.optString("status"))) {

                                String state = obj.optString("state", "unknown");

                                runOnUiThread(() ->
                                        statuskoneksi.setText("📡 Status session: " + state)
                                );

                            } else {
                                runOnUiThread(() ->
                                        statuskoneksi.setText("⚠️ Session tidak ditemukan")
                                );
                            }

                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    statuskoneksi.setText("⚠️ Error parsing data")
                            );
                        }
                    }
                });

                handler.postDelayed(this, INTERVAL); // ulangi tiap 5 detik
            }
        };

        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable); // stop saat keluar halaman
        }
    }
    private void requestakun(String apiKey) {
        try {
            OkHttpClient client1 = new OkHttpClient.Builder()
                    .connectTimeout(0, TimeUnit.MILLISECONDS)
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .writeTimeout(0, TimeUnit.MILLISECONDS)
                    .build();

            // Buat JSON untuk dikirim ke API
            JSONObject json = new JSONObject();
            String keynya = apiKey + "11";
            json.put("id", keynya);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            // Ganti URL dengan alamat API Node.js kamu
            Request request = new Request.Builder()
                    .url(urlwa + "/session")
                    .post(body)
                    .build();

            client1.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(whatsappActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resStr = response.body().string();
                    Log.d(TAG, "Response API request: " + resStr);

                    try {
                        JSONObject json = new JSONObject(resStr);
                        String status = json.optString("status");
                        String message = json.optString("message", "Tidak ada pesan");

                        runOnUiThread(() -> {
                            if (status.equals("success")) {
                                Toast.makeText(whatsappActivity.this, "Berhasil: " + message, Toast.LENGTH_LONG).show();

                                // 1. Ambil QR code
                                getQrCode(keynya);

                                // 2. Mulai auto cek status session
                                handler = new Handler();
                                runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        checkSessionStatus(keynya); // <--- passing apiKey (session_id)
                                        handler.postDelayed(this, interval);
                                    }
                                };
                                handler.post(runnable);

                            } else {
                                Toast.makeText(whatsappActivity.this, "Gagal: " + message, Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(whatsappActivity.this, "Error parsing JSON", Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkSessionStatus(String sessionId) {
        String url = urlwa + "/session/" + sessionId + "/status";
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                failedCount++;
                Log.e(TAG, "Request gagal: " + e.getMessage() + " | Gagal ke-" + failedCount);

                runOnUiThread(() ->
                        Toast.makeText(whatsappActivity.this,
                                "⚠️ Request gagal (" + failedCount + "/" + MAX_FAILED + ")",
                                Toast.LENGTH_SHORT).show()
                );

                // Stop kalau sudah 20 kali gagal
                if (failedCount >= MAX_FAILED) {
                    if (handler != null && runnable != null) {
                        handler.removeCallbacks(runnable);
                    }
                    Log.e(TAG, "❌ Stop cek session, sudah gagal " + MAX_FAILED + " kali.");
                    runOnUiThread(() ->
                            Toast.makeText(whatsappActivity.this,
                                    "❌ Stop cek session, sudah gagal " + MAX_FAILED + " kali.",
                                    Toast.LENGTH_LONG).show()
                    );
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject obj = new JSONObject(jsonData);

                        String status = obj.optString("status", "error");
                        final String sessionStatus;

                        if ("success".equalsIgnoreCase(status)) {
                            sessionStatus = obj.optString("state", "unknown");
                        } else {
                            sessionStatus = obj.optString("message", "Session tidak ditemukan");
                        }

                        runOnUiThread(() -> {
                            if ("success".equalsIgnoreCase(status)) {
                                Toast.makeText(whatsappActivity.this,
                                        "✅ Session aktif: " + sessionStatus,
                                        Toast.LENGTH_SHORT).show();

                                if ("ready".equalsIgnoreCase(sessionStatus)) {
                                    if (handler != null && runnable != null) {
                                        handler.removeCallbacks(runnable);
                                        Log.d(TAG, "Auto cek session dihentikan (sudah connected).");
                                    }
                                    simpan(sessionId);
                                }
                            } else {
                                Toast.makeText(whatsappActivity.this,
                                        "❌ " + sessionStatus,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Reset hitungan gagal kalau berhasil response
                        failedCount = 0;

                        Log.d(TAG, "Status session: " + status + " / " + sessionStatus);

                    } catch (Exception e) {
                        Log.e(TAG, "Parsing error: " + e.getMessage());
                        runOnUiThread(() ->
                                Toast.makeText(whatsappActivity.this,
                                        "⚠️ Parsing error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }
 
    private void getQrCode(String sessionId) {
        String url2 = urlwa+"/session/" + sessionId+"/qrcode";

        Request request = new Request.Builder()
                .url(url2)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API error: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(whatsappActivity.this, "Gagal koneksi ke server", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unexpected code " + response);
                    return;
                }

                String resStr = response.body().string();
                Log.d(TAG, "Respon API: " + resStr);

                try {
                    JSONObject json = new JSONObject(resStr);

                        String qrBase64 = json.getString("qrcode");

                        // Hilangkan prefix data:image/png;base64, jika ada
                        if (qrBase64.startsWith("data:image")) {
                            qrBase64 = qrBase64.substring(qrBase64.indexOf(",") + 1);
                        }
                        Log.d(TAG, "Respon API: " + qrBase64);

                        byte[] decodedBytes = Base64.decode(qrBase64, Base64.DEFAULT);
                        Bitmap qrBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                        runOnUiThread(() -> {
                            qrImageView.setImageBitmap(qrBitmap);

                        });


                } catch (JSONException e) {
                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                }
            }
        });
    }
    private void simpan(String sesion) {
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "update_waapi")
                .add("user", iduserr)
                .add("sesion", sesion)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(whatsappActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        if (json.optString("status").equals("success")) {
                            // sukses update
                            Toast.makeText(whatsappActivity.this, "koneksi sukses", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(whatsappActivity.this, whatsappActivity.class);
                            startActivity(intent);
                        } else {

                            //String status =obj.optString("message");
                            Toast.makeText(whatsappActivity.this, " gagal", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(whatsappActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void hapusakun(String id) {
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "hapus_waapi")
                .add("user", iduserr)
                .add("sesion", id)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(whatsappActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        if (json.optString("status").equals("success")) {
                            // sukses update
                            Toast.makeText(whatsappActivity.this, "hapus sesion sukses", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(whatsappActivity.this, whatsappActivity.class);
                            startActivity(intent);
                        } else {

                            //String status =obj.optString("message");
                            Toast.makeText(whatsappActivity.this, " gagal", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(whatsappActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


}
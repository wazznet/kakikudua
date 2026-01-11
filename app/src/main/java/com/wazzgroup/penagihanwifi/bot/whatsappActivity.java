package com.wazzgroup.penagihanwifi.bot;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
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
    Button btnSend,btnRequest;
    private int interval = 5000; // interval cek tiap 5 detik

    private Handler handler;
    private Runnable runnable;
    String url = GlobalHelper.BASE_URL;
    String urlwa = GlobalHelper.BASE_URL_wa;
    ImageView qrImageView;
    private int failedCount = 0;
    String iduserr ;
    TextView statuswa;
    private static final int MAX_FAILED = 20;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_whatsapp);
        qrImageView = findViewById(R.id.imageView14);
         statuswa = findViewById(R.id.statuswa);
        ImageView kewa = findViewById(R.id.back2);
        kewa.setOnClickListener(v -> {
            Intent intent = new Intent(whatsappActivity.this, BotPenagihanActivity.class);
            startActivity(intent);
        });
        btnSend = findViewById(R.id.tes);
        btnRequest = findViewById(R.id.qr);
        iduserr = GlobalHelper.getIdUser(this);
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestakun(iduserr);
                btnRequest.setEnabled(false);

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

                            statuswa.setText("Silakan Buat Sessions Baru");
                        } else {

                            statuswa.setText("Sessions Telah Di buat ");
                            btnRequest.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(whatsappActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Aksi ketika tombol ditekan
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kirimPesan(iduserr+"11");
            }
        });
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

    /**
     * Fungsi untuk cek status session dari server
     */
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

//    private void checkSessionStatus(String sessionId) {
//        String url = urlwa + "/session-status/" + sessionId;
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e(TAG, "Request gagal: " + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful() && response.body() != null) {
//                    try {
//                        String jsonData = response.body().string();
//                        JSONObject obj = new JSONObject(jsonData);
//
//                        boolean status = obj.optBoolean("status", false);
//                        final String sessionStatus;
//
//                        if (status) {
//                            // Kalau status true
//                            sessionStatus = obj.optString("session_status", "unknown");
//                        } else {
//                            // Kalau status false → ambil message
//                            sessionStatus = obj.optString("message", "Session tidak ditemukan");
//                        }
//
//                        runOnUiThread(() -> {
//                            if (status) {
//                                Toast.makeText(whatsappActivity.this, "✅ Session aktif: " + sessionStatus, Toast.LENGTH_SHORT).show();
//                                if ("connected".equalsIgnoreCase(sessionStatus)) {
//                                    if (handler != null && runnable != null) {
//                                        handler.removeCallbacks(runnable);
//                                        Log.d(TAG, "Auto cek session dihentikan (sudah connected).");
//                                    }
//                                }
//                            } else {
//                                Toast.makeText(whatsappActivity.this, "❌ " + sessionStatus, Toast.LENGTH_SHORT).show();
//                            }
//                        });
//
//                        Log.d(TAG, "Status session: " + (status ? sessionStatus : "false / " + sessionStatus));
//
//                    } catch (Exception e) {
//                        Log.e(TAG, "Parsing error: " + e.getMessage());
//                        runOnUiThread(() ->
//                                Toast.makeText(whatsappActivity.this, "⚠️ Parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                        );
//                    }
//
//                }
//            }
//        });
//    }

    //    private void requestakun(String apiKey) {
//        try {
//            OkHttpClient client1 = new OkHttpClient.Builder()
//                    .connectTimeout(0, TimeUnit.MILLISECONDS)
//                    .readTimeout(0, TimeUnit.MILLISECONDS)
//                    .writeTimeout(0, TimeUnit.MILLISECONDS)
//                    .build();
//            // Buat JSON untuk dikirim ke API
//            JSONObject json = new JSONObject();
//            String keynya = apiKey+"11";
//            json.put("session_id", apiKey);
//            json.put("api_key", keynya);
//            json.put("name", apiKey);
//
//            RequestBody body = RequestBody.create(json.toString(), JSON);
//
//            // Ganti URL dengan alamat API Node.js kamu
//            Request request = new Request.Builder()
//                    .url(urlwa+"/start-session") // contoh lokal
//                    .post(body)
//                    .build();
//
//            client1.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    runOnUiThread(() ->
//                            Toast.makeText(whatsappActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                    );
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    String resStr = response.body().string();
//                    Log.d(TAG, "Response API: " + resStr); // <---- Tambah log response
//
//                    try {
//                        JSONObject json = new JSONObject(resStr);
//                        boolean status = json.optBoolean("status", false);
//                        String message = json.optString("message", "Tidak ada pesan");
//
//                        runOnUiThread(() -> {
//                            if (status) {
//                                Toast.makeText(whatsappActivity.this, "Berhasil: " + message, Toast.LENGTH_LONG).show();
//                                getQrCode(apiKey);
//
//                            } else {
//                                Toast.makeText(whatsappActivity.this, "Gagal: " + message, Toast.LENGTH_LONG).show();
//                            }
//                        });
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        runOnUiThread(() ->
//                                Toast.makeText(whatsappActivity.this, "Error parsing JSON", Toast.LENGTH_LONG).show()
//                        );
//                    }
//                }
//            });
//
//        } catch (Exception e) {
//            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
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
    private void kirimPesan(String apiKey) {
        try {
            String nomer = GlobalHelper.getno(this);

            // Pastikan nomor dimulai dengan "08" → ubah ke "62"
            if (nomer.startsWith("0")) {
                nomer = "62" + nomer.substring(1);
            }

            // Buat JSON untuk dikirim ke API
            JSONObject json = new JSONObject();
            json.put("to", nomer);
            json.put("message", "tessssss dari wazzapp");

            RequestBody body = RequestBody.create(json.toString(), JSON);

            // Kirim request ke Node.js API
            Request request = new Request.Builder()
                    .url(urlwa + "/session/"+apiKey+"/send")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(whatsappActivity.this,
                                    "❌ Gagal koneksi: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resStr = response.body().string();

                    try {
                        JSONObject obj = new JSONObject(resStr);
                        String status = obj.optString("status");
                        String message = obj.optString("message", "Tidak ada pesan");

                        runOnUiThread(() -> {
                            if (status.equals("success")) {
                                Toast.makeText(whatsappActivity.this,
                                        "✅ pesan telah di kirim ",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(whatsappActivity.this,
                                        "❌ Gagal: " + message,
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                        Log.d(TAG, "Respon kirimPesan: " + resStr);

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(whatsappActivity.this,
                                        "⚠️ Parsing error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this,
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }


}
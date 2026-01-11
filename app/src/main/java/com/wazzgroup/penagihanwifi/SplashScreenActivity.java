package com.wazzgroup.penagihanwifi;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import android.content.SharedPreferences;

import com.wazzgroup.penagihanwifi.halamanpelanggan.PelangganActivity;
import com.wazzgroup.penagihanwifi.halamanpelanggan.shop.PelangganRiwayatActivity;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

public class SplashScreenActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    String url = GlobalHelper.BASE_URL; // Pastikan ini endpoint yang sesuai
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedInMember", false);



        Uri data = getIntent().getData();
        if (data != null) {
            handleDeepLink(data);
        } else {
            if (isLoggedIn) {
                gotoPelangganActivity();
                return;
            }else{
                requestAllPermissions();
            }

        }
    }

    private void handleDeepLink(Uri data) {
        List<String> segments = data.getPathSegments();
        String id = "";
        String action = segments.get(0); // ambil "openapp" atau "pay"

        if ("pay".equals(action)) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

            boolean isLoggedIn = prefs.getBoolean("isLoggedInMember", false);

            if (isLoggedIn) {
                gotoriwayat();

            }else{
                Toast.makeText(SplashScreenActivity.this, "Anda belum login", Toast.LENGTH_SHORT).show();
                finish();
            }
        }else {
            if (segments.size() >= 2) {
                // contoh: /openapp/12345 atau /pay/67890
                id = segments.get(1);

                if ("openapp".equals(action)) {
                    doLoginWithId(id);
                }

            }

//        else if (data.getQueryParameter("id") != null) {
//            id = data.getQueryParameter("id");
//
//            // cek query param pay
//            if ("pay".equals(data.getQueryParameter("type"))) {
//                gotoriwayat();
//            } else {
//                doLoginWithId(id);
//            }
//
//        }
        else {
            Toast.makeText(this, "ID tidak ditemukan di link", Toast.LENGTH_SHORT).show();
            finish();
        }
        }
//        List<String> segments = data.getPathSegments();
//        String id = "";
//
//        if (segments.size() >= 2) { // contoh: /openapp/12345
//            id = segments.get(1);
//        } else if (data.getQueryParameter("id") != null) {
//            id = data.getQueryParameter("id");
//        }
//
//        if (!id.isEmpty()) {
//            doLoginWithId(id);
//        } else {
//            Toast.makeText(this, "ID tidak ditemukan di link", Toast.LENGTH_SHORT).show();
//            finish();
//        }
    }

    private void doLoginWithId(String id) {
        RequestBody formBody = new FormBody.Builder()
                .add("api", "login_id") // Pastikan API PHP mendukung login dengan ID
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
                        Toast.makeText(SplashScreenActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getString("status").equals("success")) {
                            String nama = obj.getString("nama");
                            String idLogin = obj.getString("id");
                            String nama_area = obj.getString("nama_area");
                            String nama_paket = obj.getString("nama_paket");
                            String wifiid = obj.getString("id_user");

                            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("isLoggedInMember", true);
                            editor.putString("nama_pelanggan", nama);
                            editor.putString("idlogin", idLogin);
                            editor.putString("idpelannggan", idLogin);
                            editor.putString("nama_area", nama_area);
                            editor.putString("nama_paket", nama_paket);
                            editor.putString("wifiid", wifiid);
                            editor.apply();

                            gotoPelangganActivity();
                        } else {
                            Toast.makeText(SplashScreenActivity.this, "Login gagal: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SplashScreenActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void gotoPelangganActivity() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreenActivity.this, PelangganActivity.class));
            finish();
        }, 1500);
    }
    private void gotoriwayat() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreenActivity.this, PelangganRiwayatActivity.class));
            finish();
        }, 1500);
    }

    private void requestAllPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        String[] requiredPermissions = {
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.CAMERA
        };

        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != 0) {
                permissionsNeeded.add(permission);
            }
        }

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != 0) {
            permissionsNeeded.add(android.Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            proceedToLogin();
        }
    }

    private void proceedToLogin() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            finish();
        }, 1500);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != 0) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                proceedToLogin();
            } else {
                Toast.makeText(this, "Izin tidak lengkap, aplikasi tidak bisa berjalan", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}

package com.wazzgroup.penagihanwifi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.wazzgroup.penagihanwifi.acs.AcskuActivity;
import com.wazzgroup.penagihanwifi.bot.BotPenagihanActivity;
import com.wazzgroup.penagihanwifi.client.ClientActivity;
import com.wazzgroup.penagihanwifi.helper.TokenAuthenticator;
import com.wazzgroup.penagihanwifi.helper.TokenInterceptor;
import com.wazzgroup.penagihanwifi.komplen.KomplainActivity;
import com.wazzgroup.penagihanwifi.map.LapanganActivity;
import com.wazzgroup.penagihanwifi.map.MapActivity;
import com.wazzgroup.penagihanwifi.mikrotik.TambahMikrotik;
import com.wazzgroup.penagihanwifi.pembayaran.MenuPembayaranActivity;
import com.wazzgroup.penagihanwifi.pembukuan.PembukuanActivity;
import com.wazzgroup.penagihanwifi.psb.PsbActivity;
import com.wazzgroup.penagihanwifi.settings.SettingsActivity;
import com.wazzgroup.penagihanwifi.tools.PingActivity;
import com.wazzgroup.penagihanwifi.tools.TracerouteActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TeknisiActivity extends AppCompatActivity {



    private Handler handler = new Handler();
    private TextView pengeluaranIntn,pemasukann,nnunggak,tagihanku,telatku,nunggakku,nkomplen,textMeta,textTiktok,textYoutube,textCloudflare,forr;
    boolean isVisible = false;
    private ImageView mata1,mata2,mata3,mata4,mata5;
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
        mata1 = findViewById(R.id.mata1);
        mata2 = findViewById(R.id.mata2);
        mata3 = findViewById(R.id.mata3);
        mata4 = findViewById(R.id.mata4);
        mata5 = findViewById(R.id.mata5);

        forr = findViewById(R.id.forr);
        nnunggak = findViewById(R.id.nnunggak);
        tagihanku = findViewById(R.id.tagihanku);
        telatku = findViewById(R.id.telatku);
        nunggakku = findViewById(R.id.nunggakku);
        nkomplen = findViewById(R.id.nkomplen);
        pemasukann = findViewById(R.id.pemasukan);
        pengeluaranIntn = findViewById(R.id.pengeluaran);

        tagihanku.setTransformationMethod(PasswordTransformationMethod.getInstance());
        telatku.setTransformationMethod(PasswordTransformationMethod.getInstance());
        nunggakku.setTransformationMethod(PasswordTransformationMethod.getInstance());
        pemasukann.setTransformationMethod(PasswordTransformationMethod.getInstance());
        pengeluaranIntn.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mata1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVisible = !isVisible;
                if (isVisible) { tagihanku.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {tagihanku.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        mata2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVisible = !isVisible;
                if (isVisible) { telatku.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {telatku.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        mata3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVisible = !isVisible;
                if (isVisible) { nunggakku.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {nunggakku.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        mata4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVisible = !isVisible;
                if (isVisible) { pemasukann.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {pemasukann.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        mata5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVisible = !isVisible;
                if (isVisible) { pengeluaranIntn.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {pengeluaranIntn.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        //String iduserr = prefs.getString("iduser", "");
        String akses = prefs.getString("nama_wifi", "");

        forr.setText("for "+akses);

        // Jika foto di klik, langsung pindah ke LogoutActivity
        fotoLogout.setOnClickListener(v -> {
            Intent intent = new Intent(TeknisiActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        ConstraintLayout pembukuan = findViewById(R.id.pembukuan);
        LinearLayout pembukuan2 = findViewById(R.id.pembukuan2);
        LinearLayout pembukuan3 = findViewById(R.id.pembukuan3);
        pembukuan.setOnClickListener(v -> {
            Intent intent = new Intent(TeknisiActivity.this, PembukuanActivity.class);
            startActivity(intent);
        });
        pembukuan2.setOnClickListener(v -> {
            Intent intent = new Intent(TeknisiActivity.this, PembukuanActivity.class);
            startActivity(intent);
        });
        pembukuan3.setOnClickListener(v -> {
            Intent intent = new Intent(TeknisiActivity.this, PembukuanActivity.class);
            startActivity(intent);
        });
        ceksaldo();
        startRealtimePing();


    }

    // Fungsi dipindah ke luar onCreate()
    public void onLinearLayoutClick(View view) {
        int id = view.getId();

        if (id == R.id.penagihan) {
            startActivity(new Intent(this, MenuPembayaranActivity.class));
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
            startActivity(new Intent(this, LapanganActivity.class));
        }else if (id == R.id.komplain) {
            startActivity(new Intent(this, KomplainActivity.class));
        }else if (id == R.id.pembukuan4) {
            startActivity(new Intent(this, PembukuanActivity.class));
        }else if (id == R.id.acs) {
            startActivity(new Intent(this, AcskuActivity.class));
        }else if (id == R.id.bot) {
            startActivity(new Intent(this, BotPenagihanActivity.class));
        }else if (id == R.id.mikrotik) {
            startActivity(new Intent(this, TambahMikrotik.class));
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
    private void ceksaldo() {

        String url = GlobalHelper.BASE_URL_V2;
        String iduserr = GlobalHelper.getIdUser(this);

        // PAKAI CLIENT YANG SUDAH ADA TOKEN & AUTO REFRESH
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("api", "banyaknotif")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(TeknisiActivity.this,
                                "Gagal koneksi: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                int code = response.code();
                String bodyString = response.body() != null ? response.body().string() : "";

                // 🔥 LOG DEBUG
//                Log.d("API_DEBUG", "HTTP CODE: " + code);
//                Log.d("API_DEBUG", "BODY: " + bodyString);

                if (!response.isSuccessful()) {

                    runOnUiThread(() ->
                            Toast.makeText(TeknisiActivity.this,
                                    "Server error: " + code,
                                    Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                runOnUiThread(() -> {
                    try {

                        JSONObject obj = new JSONObject(bodyString);

                        if (obj.getString("status").equals("success")) {

                            String belumlunas = obj.optString("belumlunas", "0");
                            String komplen = obj.optString("komplen", "0");
                            String pemasukan = obj.optString("pemasukan", "0");
                            String pengeluarantotal = obj.optString("pengeluaran", "0");
                            String munggak = obj.optString("munggak", "0");
                            String telat = obj.optString("telat", "0");

                            NumberFormat formatRupiah =
                                    NumberFormat.getNumberInstance(new Locale("in", "ID"));

                            int pemasukanInt = Integer.parseInt(pemasukan);
                            pemasukann.setText("Rp. " + formatRupiah.format(pemasukanInt));

                            int pengeluaranInt = Integer.parseInt(pengeluarantotal);
                            pengeluaranIntn.setText("Rp. " + formatRupiah.format(pengeluaranInt));

                            int belumlunasInt = Integer.parseInt(belumlunas);
                            int telatInt = Integer.parseInt(telat);
                            int munggakInt = Integer.parseInt(munggak);
                            int komplenInt = Integer.parseInt(komplen);

                            tagihanku.setText(String.valueOf(belumlunasInt));
                            telatku.setText(String.valueOf(telatInt));
                            nunggakku.setText(String.valueOf(munggakInt));

                            if (belumlunasInt > 0) {
                                nnunggak.setText(String.valueOf(belumlunasInt));
                                nnunggak.setVisibility(View.VISIBLE);
                            } else {
                                nnunggak.setVisibility(View.GONE);
                            }

                            if (komplenInt > 0) {
                                nkomplen.setText(String.valueOf(komplenInt));
                                nkomplen.setVisibility(View.VISIBLE);
                            } else {
                                nkomplen.setVisibility(View.GONE);
                            }

                        } else {

                            Toast.makeText(TeknisiActivity.this,
                                    "Update gagal: " + obj.optString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(TeknisiActivity.this,
                                "Gagal parsing data",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }

    //    private void ceksaldo() {
//                String url = GlobalHelper.BASE_URL;
//                OkHttpClient client = new OkHttpClient();
//        String iduserr = GlobalHelper.getIdUser(this);
//        RequestBody formBody = new FormBody.Builder()
//                .add("api", "banyaknotif") // Pastikan API PHP mendukung login dengan ID
//                .add("user", iduserr)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(formBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(() ->
//                        Toast.makeText(TeknisiActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                );
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String json = response.body().string();
//
//                runOnUiThread(() -> {
//                    try {
//                        JSONObject obj = new JSONObject(json);
//                        if (obj.getString("status").equals("success")) {
//                            String belumlunas = obj.getString("belumlunas");
//                           // String totalpelanggan = obj.getString("totalpelanggan");
//                            String komplen = obj.getString("komplen");
//                            String pemasukan = obj.getString("pemasukan");
//                            String pengeluarantotal = obj.getString("pengeluaran");
//                            String munggak = obj.getString("munggak");
//                            String telat = obj.getString("telat");
//
//
//                            int pemasukanInt = Integer.parseInt(pemasukan); // Ubah ke int
//                            NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));
//                            String pemasukanku = "Rp. " + formatRupiah.format(pemasukanInt);
//                            pemasukann.setText(pemasukanku);
//
//                                int pengeluaranInt = Integer.parseInt(pengeluarantotal); // Ubah ke int
//                                 String pengeluaranIntku = "Rp. " + formatRupiah.format(pengeluaranInt);
//                            pengeluaranIntn.setText(pengeluaranIntku);
//    //
//                            int belumlunasInt = Integer.parseInt(belumlunas);
//                            int telatInt = Integer.parseInt(telat);
//                            int munggakInt = Integer.parseInt(munggak);
//                           // int totalpelangganInt = Integer.parseInt(totalpelanggan);
//                            int komplenInt = Integer.parseInt(komplen);
//
//                            tagihanku.setText(String.valueOf(belumlunasInt));
//                            telatku.setText(String.valueOf(telatInt));
//                            nunggakku.setText(String.valueOf(munggakInt));
//                            if (belumlunasInt > 0) {
//                                nnunggak.setText(String.valueOf(belumlunasInt));
//                                nnunggak.setVisibility(View.VISIBLE);
//                            } else {
//                                nnunggak.setVisibility(View.GONE);
//                            }
//
//
//
//                            if (komplenInt > 0) {
//                                nkomplen.setText(String.valueOf(komplenInt));
//                                nkomplen.setVisibility(View.VISIBLE);
//                            } else {
//                                nkomplen.setVisibility(View.GONE);
//                            }
//
//                        } else {
//                            Toast.makeText(TeknisiActivity.this, "Update gagal: ", Toast.LENGTH_SHORT).show();
//
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Toast.makeText(TeknisiActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
//    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRealtimePing();
    }


}
//    private Handler handler = new Handler();
//    private TextView  textMeta,textTiktok,textYoutube,textCloudflare,forr;
//    private Runnable pingRunnable;
//    String url = GlobalHelper.BASE_URL;
//    private OkHttpClient client = new OkHttpClient();
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//
//
//        setContentView(R.layout.activity_teknisi);
//        textMeta = findViewById(R.id.textMeta);
//        textTiktok = findViewById(R.id.textTiktok);
//        textYoutube = findViewById(R.id.textYoutube);
//        textCloudflare = findViewById(R.id.textCloudflare);
//        ImageView fotoLogout = findViewById(R.id.profile);
//        ConstraintLayout pembukuan = findViewById(R.id.pembukuan);
//        LinearLayout pembukuan2 = findViewById(R.id.pembukuan2);
//        LinearLayout pembukuan3 = findViewById(R.id.pembukuan3);
//        pembukuan.setOnClickListener(v -> {
//            Intent intent = new Intent(TeknisiActivity.this, PembukuanActivity.class);
//            startActivity(intent);
//        });
//        pembukuan2.setOnClickListener(v -> {
//            Intent intent = new Intent(TeknisiActivity.this, PembukuanActivity.class);
//            startActivity(intent);
//        });
//        pembukuan3.setOnClickListener(v -> {
//            Intent intent = new Intent(TeknisiActivity.this, PembukuanActivity.class);
//            startActivity(intent);
//        });
//        forr = findViewById(R.id.forr);
//
//        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
//        //String iduserr = prefs.getString("iduser", "");
//        String akses = prefs.getString("nama_wifi", "");
//
//        forr.setText("for "+akses);
//
//        // Jika foto di klik, langsung pindah ke LogoutActivity
//        fotoLogout.setOnClickListener(v -> {
//            Intent intent = new Intent(TeknisiActivity.this, SettingsActivity.class);
//            startActivity(intent);
//        });
//
//        startRealtimePing();
//        ceksaldo();
//
//    }
//
//    // Fungsi dipindah ke luar onCreate()
//    public void onLinearLayoutClick(View view) {
//        int id = view.getId();
//
//        if (id == R.id.penagihan) {
//            startActivity(new Intent(this, ManajemenPenagihan.class));
//        } else if (id == R.id.psb) {
//            startActivity(new Intent(this, PsbActivity.class));
//        }else if (id == R.id.client) {
//            startActivity(new Intent(this, ClientActivity.class));
//        }else if (id == R.id.map) {
//            startActivity(new Intent(this, MapActivity.class));
//        }else if (id == R.id.ping) {
//            startActivity(new Intent(this, PingActivity.class));
//        }else if (id == R.id.traceroute) {
//            startActivity(new Intent(this, TracerouteActivity.class));
//        }else if (id == R.id.ACS) {
//            startActivity(new Intent(this, OdpActivity.class));
//        }else if (id == R.id.komplain) {
//            startActivity(new Intent(this, KomplainActivity.class));
//        }
//    }
//
//    private void ceksaldo() {
//        String iduserr = GlobalHelper.getIdUser(this);
//        RequestBody formBody = new FormBody.Builder()
//                .add("api", "banyaknotif") // Pastikan API PHP mendukung login dengan ID
//                .add("user", iduserr)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(formBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(() ->
//                        Toast.makeText(TeknisiActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                );
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String json = response.body().string();
//
//                runOnUiThread(() -> {
//                    try {
//                        JSONObject obj = new JSONObject(json);
//                        if (obj.getString("status").equals("success")) {
//                            String belumlunas = obj.getString("belumlunas");
//                            String totalpelanggan = obj.getString("totalpelanggan");
//                            String komplen = obj.getString("komplen");
//                            String pemasukan = obj.getString("pemasukan");
//
//                            TextView nnunggak = findViewById(R.id.nnunggak);
//                            TextView npelanggan = findViewById(R.id.npelanggan);
//                            TextView nkomplen = findViewById(R.id.nkomplen);
//                            int pemasukanInt = Integer.parseInt(pemasukan); // Ubah ke int
//                            NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));
//                            String pemasukanku = "Rp. " + formatRupiah.format(pemasukanInt);
//                            TextView pemasukann = findViewById(R.id.pemasukan);
//                            pemasukann.setText(pemasukanku);
//// Ubah ke integer
////                            int pemasukanInt = Integer.parseInt(pemasukan); // Ubah ke int
////                            NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));
////                            String pemasukanku = "Rp. " + formatRupiah.format(pemasukanInt);
////                            TextView pemasukann = findViewById(R.id.pemasukan);
////                            pemasukann.setText(pemasukanku);
////
//                            int belumlunasInt = Integer.parseInt(belumlunas);
//                            int totalpelangganInt = Integer.parseInt(totalpelanggan);
//                            int komplenInt = Integer.parseInt(komplen);
//
//                            if (belumlunasInt > 0) {
//                                nnunggak.setText(String.valueOf(belumlunasInt));
//                                nnunggak.setVisibility(View.VISIBLE);
//                            } else {
//                                nnunggak.setVisibility(View.GONE);
//                            }
//
//                            if (totalpelangganInt > 0) {
//                                npelanggan.setText(String.valueOf(totalpelangganInt));
//                                npelanggan.setVisibility(View.VISIBLE);
//                            } else {
//                                npelanggan.setVisibility(View.GONE);
//                            }
//
//                            if (komplenInt > 0) {
//                                nkomplen.setText(String.valueOf(komplenInt));
//                                nkomplen.setVisibility(View.VISIBLE);
//                            } else {
//                                nkomplen.setVisibility(View.GONE);
//                            }
//
//                        } else {
//                            Toast.makeText(TeknisiActivity.this, "Update gagal: ", Toast.LENGTH_SHORT).show();
//
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Toast.makeText(TeknisiActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
//    }
//
//
//    private void startRealtimePing() {
//        pingRunnable = new Runnable() {
//            @Override
//            public void run() {
//                pingServer("facebook.com", textMeta);
//                pingServer("tiktok.com", textTiktok);
//                pingServer("youtube.com", textYoutube);
//                pingServer("1.1.1.1", textCloudflare);
//
//                handler.postDelayed(this, 1000); // Ulang setiap 5 detik
//            }
//        };
//        handler.post(pingRunnable);
//    }
//
//    private void stopRealtimePing() {
//        handler.removeCallbacks(pingRunnable);
//    }
//    private void pingServer(String host, TextView textView) {
//        new Thread(() -> {
//            try {
//                Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 " + host);
//                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//                String line;
//                String timeMs = "N/A";
//
//                while ((line = reader.readLine()) != null) {
//                    if (line.contains("time=")) {
//                        int index = line.indexOf("time=");
//                        timeMs = line.substring(index + 5, line.indexOf(" ms", index)) + " ms";
//                    }
//                }
//
//                process.waitFor();
//
//                final String result =  timeMs;
//
//                runOnUiThread(() -> textView.setText(result));
//
//            } catch (Exception e) {
//                runOnUiThread(() -> textView.setText(host + " : Error"));
//            }
//        }).start();
//    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        stopRealtimePing();
//    }
//
//
//}

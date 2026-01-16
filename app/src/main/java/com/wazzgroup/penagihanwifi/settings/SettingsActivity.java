package com.wazzgroup.penagihanwifi.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.LoginActivity;
import com.wazzgroup.penagihanwifi.LogoutActivity;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.client.ClientActivity;
import com.wazzgroup.penagihanwifi.komplen.KomplainActivity;
import com.wazzgroup.penagihanwifi.map.MapActivity;
import com.wazzgroup.penagihanwifi.map.OdpActivity;
import com.wazzgroup.penagihanwifi.pembayaran.ManajemenPenagihan;
import com.wazzgroup.penagihanwifi.psb.PsbActivity;
import com.wazzgroup.penagihanwifi.settings.printer.PrinterActivity;
import com.wazzgroup.penagihanwifi.settings.printer.PrinterSettingActivity;
import com.wazzgroup.penagihanwifi.settings.printer.PrinterTestActivity;
import com.wazzgroup.penagihanwifi.tools.PingActivity;
import com.wazzgroup.penagihanwifi.tools.TracerouteActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        TextView forr = findViewById(R.id.namawifi);
        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        //String iduserr = prefs.getString("iduser", "");
        String akses = prefs.getString("nama_wifi", "");

        forr.setText(akses);
        TextView btnLogout = findViewById(R.id.textView20);

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Konfirmasi Logout")
                    .setMessage("Apakah Anda yakin ingin keluar?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        // Hapus semua data SharedPreferences

                        logout();
                        SharedPreferences prefss = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefss.edit();
                        editor.clear();  // Menghapus semua data
                        editor.apply();

                        // Arahkan ke LoginActivity dan hapus history stack
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Batal", (dialog, which) -> {
                        dialog.dismiss(); // Tutup dialog
                    })
                    .show();
        }

        );

    }
    private void logout() {
        String url = GlobalHelper.BASE_URL;
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String version = Build.VERSION.RELEASE; // Versi Android
        String brand = Build.BRAND; // Brand HP

        String deviceInfo = brand + " " +manufacturer + " " + model + " (Android " + version + ")";
        String androidId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        RequestBody formBody = new FormBody.Builder()
                .add("api", "logout")
                .add("user", iduserr)
                .add("deviceInfo", deviceInfo)
                .add("androidId", androidId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(SettingsActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getString("status").equals("success")) {
                            Toast.makeText(SettingsActivity.this, "logout sukses: ", Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SettingsActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    public void onTextViewClick(View view) {
        int id = view.getId();

        if (id == R.id.tombolarea) {
            startActivity(new Intent(this, SettingAreaActivity.class));
        }else if (id == R.id.tombolsub) {
            startActivity(new Intent(this, SettingSubActivity.class));
        }else if (id == R.id.tombollain) {
            startActivity(new Intent(this, SettingLainActivity.class));
        }else if (id == R.id.tomboldev) {
            startActivity(new Intent(this, SettingInfoActivity.class));
        }else if (id == R.id.paket) {
            startActivity(new Intent(this, SettingPaketActivity.class));
        }else if (id == R.id.printer) {
            startActivity(new Intent(this, PrinterSettingActivity.class));
        }
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, TeknisiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
package com.wazzgroup.penagihanwifi.acs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.settings.SettingSubActivity;
import com.wazzgroup.penagihanwifi.settings.SettingsActivity;

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

public class AcskuActivity extends AppCompatActivity {
    TextView textToCopy;
    Button btnCopy;
    Button cari;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acsku);
        textToCopy = findViewById(R.id.textToCopy);
        btnCopy = findViewById(R.id.btnCopy);
        cari = findViewById(R.id.cari);


        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> {
            Intent intent = new Intent(AcskuActivity.this, TeknisiActivity.class);
            startActivity(intent);
        });
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ambil teks
                String text = textToCopy.getText().toString();

                // Simpan ke clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", text);
                clipboard.setPrimaryClip(clip);

                // Tampilkan pesan sukses
                Toast.makeText(AcskuActivity.this, "Teks berhasil disalin!", Toast.LENGTH_SHORT).show();
            }
        });
        cari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText snk = findViewById(R.id.snku);
                String sn = snk.getText().toString();
                cek(sn);
            }
        });
    }
    private void cek(String sn) {
        String url = GlobalHelper.BASE_URL;
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "carisn") // Pastikan API PHP mendukung login dengan ID
                .add("user", iduserr) // Pastikan API PHP mendukung login dengan ID
                .add("sn", sn)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AcskuActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.optString("status").equals("success")) {
                            String redaman = obj.optString("redaman");
                            String perangkat = obj.optString("perangkat");
                            String typealat = obj.optString("type-alat");
                            String SSID = obj.optString("SSID");
                            String jumlah_terhubung = obj.optString("jumlah_terhubung");

                            TextView tperangkat = findViewById(R.id.tperangkat);
                            TextView tredaman = findViewById(R.id.redaman);
                            TextView ttypealat = findViewById(R.id.typealat);
                            TextView tssid = findViewById(R.id.ssid);
                            TextView tterhubung = findViewById(R.id.terhubung);
                            LinearLayout hasil = findViewById(R.id.hasil);

                            tperangkat.setText(String.valueOf(perangkat));
                            tredaman.setText(String.valueOf(redaman));
                            ttypealat.setText(String.valueOf(typealat));
                            tssid.setText(String.valueOf(SSID));
                            tterhubung.setText(String.valueOf(jumlah_terhubung));
                            hasil.setVisibility(View.VISIBLE);


                        } else {

                            LinearLayout hasil = findViewById(R.id.hasil);
                            Toast.makeText(AcskuActivity.this, "Update gagal: ", Toast.LENGTH_SHORT).show();
                            hasil.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(AcskuActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
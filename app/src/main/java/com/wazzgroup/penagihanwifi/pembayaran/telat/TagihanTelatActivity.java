package com.wazzgroup.penagihanwifi.pembayaran.telat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.KangtagihActivity;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.helper.TokenAuthenticator;
import com.wazzgroup.penagihanwifi.helper.TokenInterceptor;
import com.wazzgroup.penagihanwifi.pembayaran.MenuPembayaranActivity;
import com.wazzgroup.penagihanwifi.pembayaran.PembayaranActivity;
import com.wazzgroup.penagihanwifi.pembayaran.tagihan.ManajemenPenagihan;
import com.wazzgroup.penagihanwifi.pembayaran.tagihan.TagihanAdapter;
import com.wazzgroup.penagihanwifi.pembayaran.tagihan.TagihanBelumLunas;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TagihanTelatActivity extends AppCompatActivity {
    String url = GlobalHelper.BASE_URL_V2;

    private ListView listView;
    private EditText searchBox;
    private ArrayList<TagihanBelumLunas> listData = new ArrayList<>();
    private TagihanAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tagihan_telat);



        listView = findViewById(R.id.listTagihan);
        searchBox = findViewById(R.id.searchBox);


        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());

        adapter = new TagihanAdapter(this, listData);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            TagihanBelumLunas data = listData.get(position);
            tampilkanDialogPilihan(data);
            return true; // Supaya event long click tidak lanjut ke click biasa
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            TagihanBelumLunas data = listData.get(position);

            Intent intent = new Intent(TagihanTelatActivity.this, PembayaranActivity.class);
            intent.putExtra("id_pelanggan", data.idPelanggan);
            intent.putExtra("darihalaman", "telat");

            startActivity(intent);
        });

        ambilDataTagihan();

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String keyword = s.toString();
                if (keyword.length() >= 2) { // biar nggak spam ke server
                    cariTagihan(keyword);
                } else if (keyword.isEmpty()) {
                    ambilDataTagihan(); // Kembali ke semua data
                }
            }
        });

    }
    private void tampilkanDialogPilihan(TagihanBelumLunas p) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_penagihan_pelanggan, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.show();

        Button btnChat = dialogView.findViewById(R.id.btnChat);
        Button btnLokasi = dialogView.findViewById(R.id.btnLokasi);

        btnLokasi.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "lokasi langganan " + p.nama, Toast.LENGTH_SHORT).show();
            String latitude = p.latitude;
            String longitude = p.longitude;

            if (latitude == null || longitude == null || latitude.isEmpty() || longitude.isEmpty()) {
                Toast.makeText(this, "lokasi belum di setting" + p.nama, Toast.LENGTH_SHORT).show();
                return;
            }

            String geoUri = "geo:0,0?q=" + latitude + "," + longitude + "(Lokasi Pelanggan)";
            Intent geoIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(geoUri));

            try {
                startActivity(geoIntent);
            } catch (Exception e) {
                String webUri = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
                Intent webIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(webUri));

                try {
                    startActivity(webIntent);
                } catch (Exception ex) {
                    Toast.makeText(this, "Tidak bisa membuka lokasi di Maps atau Browser", Toast.LENGTH_SHORT).show();

                }
            }
        });
        btnChat.setOnClickListener(v -> {
            dialog.dismiss();

            String nomorHP = p.hp;

            if (nomorHP == null || nomorHP.isEmpty()) {
                Toast.makeText(this, "Nomor HP tidak tersedia", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pastikan format nomor internasional, contoh: 628xxxxx
            if (nomorHP.startsWith("0")) {
                nomorHP = "62" + nomorHP.substring(1);
            }

            String pesan = "Halo " + p.nama + ", ini dari tim penagihan. Mohon konfirmasi tagihan Anda ya.";
            String url = "https://wa.me/" + nomorHP + "?text=" + Uri.encode(pesan);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp tidak terpasang", Toast.LENGTH_SHORT).show();
            }
        });



    }
    private void cariTagihan(String keyword) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "search_telat_bayar")
                .add("user", iduserr)
                .add("keyword", keyword)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TagihanTelatActivity.this, "Gagal cari data", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        JSONArray data = json.getJSONArray("data");

                        listData.clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            TagihanBelumLunas t = new TagihanBelumLunas();
                            t.idPelanggan = obj.getString("id_pelanggan");
                            t.nama = obj.getString("nama");
                            t.hp = obj.getString("hp");
                            t.paket = obj.getString("nama_paket");
                            t.totalTagihan = obj.getString("total_tagihan");
                            t.rincianBulan = obj.getString("rincian_bulan_tagihan");
                            t.nama_area = obj.getString("nama_area");
                            t.tanggalpenagihan = obj.getString("tanggal_dibuat");
                            t.latitude = obj.getString("latitude");
                            t.longitude = obj.getString("longitude");

                            listData.add(t);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(TagihanTelatActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void ambilDataTagihan() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "search_telat_bayar")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TagihanTelatActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();
                //Log.d("API_DEBUG", "BODY: " + hasil);
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        JSONArray data = json.getJSONArray("data");

                        listData.clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            TagihanBelumLunas t = new TagihanBelumLunas();
                            t.idPelanggan = obj.getString("id_pelanggan");
                            t.nama = obj.getString("nama");
                            t.hp = obj.getString("hp");
                            t.paket = obj.getString("nama_paket");
                            t.totalTagihan = obj.getString("total_tagihan");
                            t.rincianBulan = obj.getString("rincian_bulan_tagihan");
                            t.nama_area = obj.getString("nama_area");
                            t.tanggalpenagihan = obj.getString("tanggal_dibuat");
                            t.latitude = obj.getString("latitude");
                            t.longitude = obj.getString("longitude");

                            listData.add(t);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(TagihanTelatActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void kembaliKeList() {
        Intent intent = getIntent();
        String darihalaman = intent.getStringExtra("darihalaman");
        if(Objects.equals(darihalaman, "kangtagih")){
            Intent intent1 = new Intent(this, KangtagihActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
            finish();
        }else{
            Intent intent1 = new Intent(this, MenuPembayaranActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
            finish();
        }

    }
}

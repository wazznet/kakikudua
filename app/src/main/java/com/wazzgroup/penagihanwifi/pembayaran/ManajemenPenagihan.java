package com.wazzgroup.penagihanwifi.pembayaran;



import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.*;

public class ManajemenPenagihan extends AppCompatActivity {
    String url = GlobalHelper.BASE_URL;

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
        setContentView(R.layout.activity_manajemen_penagihan);



        ConstraintLayout btnPilihLokasiOdp = findViewById(R.id.tombolrfid);

        btnPilihLokasiOdp.setOnClickListener(v ->  startActivity(new Intent(this, PembayaranActivity.class)));

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

            Intent intent = new Intent(ManajemenPenagihan.this, PembayaranActivity.class);
            intent.putExtra("id_pelanggan", data.idPelanggan);
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
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "search_belum_bayar")
                .add("user", iduserr)
                .add("keyword", keyword)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ManajemenPenagihan.this, "Gagal cari data", Toast.LENGTH_SHORT).show());
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
                        Toast.makeText(ManajemenPenagihan.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void ambilDataTagihan() {
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "belum_bayar")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ManajemenPenagihan.this, "Gagal ambil data", Toast.LENGTH_SHORT).show());
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
                        Toast.makeText(ManajemenPenagihan.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }private void kembaliKeList() {
        Intent intent = new Intent(this, TeknisiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

package com.wazzgroup.penagihanwifi.map;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.client.DetailPelangganActivity;
import com.wazzgroup.penagihanwifi.client.EditPelangganActivity;
import com.wazzgroup.penagihanwifi.client.Pelanggan;

import org.json.JSONArray;
import org.json.JSONObject;

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

public class ListOdpActivity extends AppCompatActivity {


    private ListView listView;
    private EditText searchBox;
    private ArrayList<lapangan> LapanganList = new ArrayList<>();
    private LapanganAdapter adapter;
    private TextView textView7;
    private static final int LIMIT = 20;
    private int currentStart = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isSearching = false;
    String url = GlobalHelper.BASE_URL;
    lapangan o;
    String nama;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        EdgeToEdge.enable(this);

        // WAJIB DI AWAL
        setContentView(R.layout.activity_list_odp);

        // AMBIL INTENT SEKALI
        Intent intent = getIntent();

        // TERIMA DATA JSON
        String json = intent.getStringExtra("odp");
        if (json != null) {
            o = new Gson().fromJson(json, lapangan.class);
        }

        ImageView kembali = findViewById(R.id.back2);
        kembali.setOnClickListener(v -> kembaliKeList());

        Button tambahodp = findViewById(R.id.tambahodp);
        Button map = findViewById(R.id.map);

        listView = findViewById(R.id.listPelanggan);
        searchBox = findViewById(R.id.searchPelanggan);
        textView7 = findViewById(R.id.textView7);

        // TOMBOL TAMBAH ODP
        tambahodp.setOnClickListener(v -> {
            String idOdp = intent.getStringExtra("id");
            String nama = intent.getStringExtra("nama");


            if (idOdp != null && !idOdp.trim().isEmpty()) {
                Intent intent2 = new Intent(ListOdpActivity.this, TambahOdpActivity.class);
                intent2.putExtra("idjalur", idOdp);
                intent2.putExtra("nama", nama);
                Log.d("ada", "idjalur: " + idOdp + ", nama: " + nama);

                startActivity(intent2);

            }else if (o != null && o.id != null) {
                Intent intent2 = new Intent(ListOdpActivity.this, TambahOdpActivity.class);
                intent2.putExtra("idjalur", o.id);
                intent2.putExtra("nama", o.nama);
                Log.d("tidak ada", "idjalur: " + o.id + ", nama: " + o.nama);

                startActivity(intent2);
            } else {
                Toast.makeText(this, "ID ODP tidak ditemukan!", Toast.LENGTH_SHORT).show();
            }

        });

        // TOMBOL MAP
        map.setOnClickListener(v -> {
            Intent mapIntent = new Intent(ListOdpActivity.this, MapJalurActivity.class);
            mapIntent.putExtra("map", new Gson().toJson(o));
            startActivity(mapIntent);
        });

        adapter = new LapanganAdapter(this, LapanganList);
        listView.setAdapter(adapter);

        // LOGIC DATA
        String idOdp = intent.getStringExtra("id");

        if (idOdp != null && !idOdp.trim().isEmpty()) {
            String nama = intent.getStringExtra("nama");
            textView7.setText(nama);
            ambilDataPelanggan(currentStart, idOdp);

        } else if (o != null && o.id != null) {
            textView7.setText(o.nama);
            ambilDataPelanggan(currentStart, o.id);

        } else {
            Toast.makeText(this, "ID ODP tidak ditemukan!", Toast.LENGTH_SHORT).show();
        }



        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                LapanganList.clear();
                adapter.notifyDataSetChanged();
                currentStart = 0;

                if (s.toString().isEmpty()) {
                    isSearching = false;
                    isLastPage = false;
                    ambilDataPelanggan(currentStart,o.id);
                } else {
                    isSearching = true;
                    cariPelanggan(s.toString(),o.id);
                }
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            lapangan p = adapter.getItem(position);
            tampilkanDialogPilihan(p);
            return true; // Supaya event long click tidak lanjut ke click biasa
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            lapangan p = adapter.getItem(position);
            Intent i = new Intent(this, DetailOdpActivity.class);
            i.putExtra("detail", new Gson().toJson(p));

            if (idOdp != null && !idOdp.trim().isEmpty()) {
                String nama = intent.getStringExtra("nama");

                i.putExtra("idjalur", idOdp);
                i.putExtra("namajalur", nama);
            } else if (o != null && o.id != null) {
                i.putExtra("idjalur", o.id);
                i.putExtra("namajalur", o.nama);
            }

            startActivity(i);
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!isLoading && !isLastPage && !isSearching && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    ambilDataPelanggan(currentStart,o.id);
                }
            }
        });

    }
    private void tampilkanDialogPilihan(lapangan p) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_jalur, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.show();

        Button btnEdit = dialogView.findViewById(R.id.btnEdit);
        Button btnlokasi = dialogView.findViewById(R.id.btnLokasi);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        btnlokasi.setVisibility(View.VISIBLE);
//
//        btnEdit.setOnClickListener(v -> {
//            dialog.dismiss();
//            Intent intent = new Intent(ListOdpActivity.this, EditPelangganActivity.class);
//            intent.putExtra("data_pelanggan", new Gson().toJson(p));
//            startActivity(intent);
//        });

        btnlokasi.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "lokasi ODP/ODC " + p.nama, Toast.LENGTH_SHORT).show();
            String latitude = p.latitude;
            String longitude = p.longitude;

            if (latitude == null || longitude == null || latitude.isEmpty() || longitude.isEmpty()) {
                Toast.makeText(this, "lokasi belum di setting" + p.nama, Toast.LENGTH_SHORT).show();
                return;
            }

            String geoUri = "geo:0,0?q=" + latitude + "," + longitude + "(Lokasi ODP/ODC)";
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

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            // TODO: Kirim API untuk hapus pelanggan
            Toast.makeText(this, "Hapus " + p.nama, Toast.LENGTH_SHORT).show();
        });
    }

    private void ambilDataPelanggan(int start,String id) {
        isLoading = true;

        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "listodp")
                .add("user", iduserr)
                .add("jalur", id)
                .add("start", String.valueOf(start))
                .add("limit", String.valueOf(LIMIT))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    Toast.makeText(ListOdpActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        JSONArray data = json.getJSONArray("data");

                        if (data.length() < LIMIT) {
                            isLastPage = true; // Tidak ada data lagi
                        }

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            lapangan p = new lapangan();
                            p.id = obj.getString("id");
                            p.nama = obj.getString("nama");
                            p.id_jalur = obj.getString("id_jalur");
                            p.type = obj.getString("type");
                            p.port = obj.getString("port");
                            p.latitude = obj.getString("latitude");
                            p.longitude = obj.getString("longitude");
                            p.nama_terhubung = obj.getString("nama_terhubung");
                            p.banyak_clinet = obj.getString("banyak_clinet");
                            LapanganList.add(p);
                        }


                        adapter.notifyDataSetChanged();
                        currentStart += LIMIT; // Naikkan start untuk load berikutnya
                        isLoading = false;

                    } catch (Exception e) {
                        isLoading = false;
                        Toast.makeText(ListOdpActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void cariPelanggan(String keyword,String id) {
        isLoading = true;

        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);
        String url = GlobalHelper.BASE_URL;

        RequestBody formBody = new FormBody.Builder()
                .add("api", "listodp")
                .add("user", iduserr)
                .add("jalur", id)
                .add("keyword", keyword)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    Toast.makeText(ListOdpActivity.this, "Gagal mencari data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        JSONArray data = json.getJSONArray("data");

                        LapanganList.clear();



                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            lapangan p = new lapangan();
                            p.id = obj.getString("id");
                            p.nama = obj.getString("nama");
                            p.id_jalur = obj.getString("id_jalur");
                            p.type = obj.getString("type");
                            p.port = obj.getString("port");
                            p.latitude = obj.getString("latitude");
                            p.longitude = obj.getString("longitude");
                            p.nama_terhubung = obj.getString("nama_terhubung");
                            p.banyak_clinet = obj.getString("banyak_clinet");
                            LapanganList.add(p);
                        }

                        adapter.notifyDataSetChanged();
                        isLoading = false;

                    } catch (Exception e) {
                        isLoading = false;
                        Toast.makeText(ListOdpActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, LapanganActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void tambahodpnya() {
        Intent intent = new Intent(this, TambahOdpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
}
package com.wazzgroup.penagihanwifi.client;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.mikrotik.TambahMikrotik;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.*;

public class ClientActivity extends AppCompatActivity {

    private ListView listView;
    private EditText searchBox;
    private ArrayList<Pelanggan> pelangganList = new ArrayList<>();
    private PelangganAdapter adapter;

    private static final int LIMIT = 20;
    private int currentStart = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isSearching = false;
    String url = GlobalHelper.BASE_URL;

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

        setContentView(R.layout.activity_client);
        ImageView kembali = findViewById(R.id.back2);
        kembali.setOnClickListener(v -> kembaliKeList());
        listView = findViewById(R.id.listPelanggan);
        searchBox = findViewById(R.id.searchPelanggan);

        adapter = new PelangganAdapter(this, pelangganList);
        listView.setAdapter(adapter);

        ambilDataPelanggan(currentStart);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                pelangganList.clear();
                adapter.notifyDataSetChanged();
                currentStart = 0;

                if (s.toString().isEmpty()) {
                    isSearching = false;
                    isLastPage = false;
                    ambilDataPelanggan(currentStart);
                } else {
                    isSearching = true;
                    cariPelanggan(s.toString());
                }
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Pelanggan p = adapter.getItem(position);
            tampilkanDialogPilihan(p);
            return true; // Supaya event long click tidak lanjut ke click biasa
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Pelanggan p = adapter.getItem(position);
            Intent i = new Intent(this, DetailPelangganActivity.class);
            i.putExtra("pelanggan", new Gson().toJson(p));
            startActivity(i);
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!isLoading && !isLastPage && !isSearching && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    ambilDataPelanggan(currentStart);
                }
            }
        });

    }
    private void tampilkanDialogPilihan(Pelanggan p) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pelanggan, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.show();

        Button btnEdit = dialogView.findViewById(R.id.btnEdit);
        Button btnStop = dialogView.findViewById(R.id.btnStop);
        Button btnStart = dialogView.findViewById(R.id.btnstart);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(ClientActivity.this, EditPelangganActivity.class);
            intent.putExtra("data_pelanggan", new Gson().toJson(p));
            startActivity(intent);
        });


        btnStop.setOnClickListener(v -> {
            dialog.dismiss();
            OkHttpClient client = new OkHttpClient();
            String iduserr = GlobalHelper.getIdUser(this);

            RequestBody formBody = new FormBody.Builder()
                    .add("api", "cuti")
                    .add("user", iduserr)
                    .add("id", p.id)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        isLoading = false;
                        Toast.makeText(ClientActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    String hasil = response.body().string();

                    runOnUiThread(() -> {
                        try {
                            JSONObject json = new JSONObject(hasil);

                            String status = json.getString("status");
                            if(status.equals("success")){
                                Toast.makeText(ClientActivity.this, "Berhenti langganan " + p.nama, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ClientActivity.this, ClientActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(ClientActivity.this, "gagal", Toast.LENGTH_SHORT).show();

                            }

                        } catch (Exception e) {
                            isLoading = false;
                            Toast.makeText(ClientActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        });
        btnStart.setOnClickListener(v -> {
            dialog.dismiss();
            OkHttpClient client = new OkHttpClient();
            String iduserr = GlobalHelper.getIdUser(this);

            RequestBody formBody = new FormBody.Builder()
                    .add("api", "aktif")
                    .add("user", iduserr)
                    .add("id", p.id)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        isLoading = false;
                        Toast.makeText(ClientActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    String hasil = response.body().string();

                    runOnUiThread(() -> {
                        try {
                            JSONObject json = new JSONObject(hasil);

                            String status = json.getString("status");
                            if(status.equals("success")){
                                Toast.makeText(ClientActivity.this, " langganan aktif kembali" + p.nama, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ClientActivity.this, ClientActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(ClientActivity.this, "gagal", Toast.LENGTH_SHORT).show();

                            }

                        } catch (Exception e) {
                            isLoading = false;
                            Toast.makeText(ClientActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        });

        btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(ClientActivity.this)
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Yakin ingin menghapus pelanggan " + p.nama + "?\n\nSemua tagihan pelanggan ini juga akan terhapus.")
                    .setCancelable(false)
                    .setPositiveButton("Hapus", (dialogConfirm, which) -> {

                        // 👉 EKSEKUSI HAPUS DI SINI
                        dialog.dismiss();

                        OkHttpClient client = new OkHttpClient();
                        String iduserr = GlobalHelper.getIdUser(this);

                        RequestBody formBody = new FormBody.Builder()
                                .add("api", "hapus")
                                .add("user", iduserr)
                                .add("id", p.id)
                                .build();

                        Request request = new Request.Builder()
                                .url(url)
                                .post(formBody)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(() -> {
                                    isLoading = false;
                                    Toast.makeText(ClientActivity.this,
                                            "Gagal menghapus data",
                                            Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String hasil = response.body().string();

                                runOnUiThread(() -> {
                                    try {
                                        JSONObject json = new JSONObject(hasil);
                                        String status = json.getString("status");

                                        if ("success".equals(status)) {
                                            Toast.makeText(ClientActivity.this,
                                                    "Pelanggan " + p.nama + " berhasil dihapus",
                                                    Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(ClientActivity.this, ClientActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(ClientActivity.this,
                                                    "Gagal menghapus pelanggan",
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    } catch (Exception e) {
                                        Toast.makeText(ClientActivity.this,
                                                "Format respon salah",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                    })
                    .setNegativeButton("Batal", (dialogConfirm, which) -> dialogConfirm.dismiss())
                    .show();
        });

    }

    private void ambilDataPelanggan(int start) {
        isLoading = true;

        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "allpelanggan")
                .add("user", iduserr)
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
                    Toast.makeText(ClientActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
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
                            Pelanggan p = new Pelanggan();
                            p.id = obj.getString("id");
                            p.nama = obj.getString("nama");
                            p.hp = obj.getString("hp");
                            p.paket = obj.getString("paket");
                            p.id_paket = obj.getString("id_paket");
                            p.tanggal = obj.getString("tanggal");
                            p.rfid = obj.getString("rfid");
                            p.terhubung_ke = obj.getString("terhubung_ke");
                            p.latitude = obj.getString("latitude");
                            p.longitude = obj.getString("longitude");
                            p.secreet = obj.getString("secreet");
                            p.created_at = obj.getString("created_at");
                            p.area = obj.getString("area");
                            p.nama_area = obj.getString("nama_area");
                            p.tanggal_ori = obj.getString("tanggal_ori");
                            p.status = obj.getString("status");

                            // Ambil ODP
                            JSONObject odp = obj.getJSONObject("odp");
                            p.odp_id = odp.getString("id");
                            p.odp_nama = odp.getString("nama");
                            p.odp_type = odp.getString("type");
                            p.odp_port = odp.getString("port");
                            p.odp_latitude = odp.getString("latitude");
                            p.odp_longitude = odp.getString("longitude");
                            p.sn = obj.getString("sn");
                            pelangganList.add(p);
                        }


                        adapter.notifyDataSetChanged();
                        currentStart += LIMIT; // Naikkan start untuk load berikutnya
                        isLoading = false;

                    } catch (Exception e) {
                        isLoading = false;
                        Toast.makeText(ClientActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void cariPelanggan(String keyword) {
        isLoading = true;

        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);
        String url = GlobalHelper.BASE_URL;

        RequestBody formBody = new FormBody.Builder()
                .add("api", "search_pelanggan")
                .add("user", iduserr)
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
                    Toast.makeText(ClientActivity.this, "Gagal mencari data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        JSONArray data = json.getJSONArray("data");

                        pelangganList.clear();



                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            Pelanggan p = new Pelanggan();
                            p.id = obj.getString("id");
                            p.nama = obj.getString("nama");
                            p.hp = obj.getString("hp");
                            p.paket = obj.getString("paket");
                            p.id_paket = obj.getString("id_paket");
                            p.tanggal = obj.getString("tanggal");
                            p.rfid = obj.getString("rfid");
                            p.terhubung_ke = obj.getString("terhubung_ke");
                            p.latitude = obj.getString("latitude");
                            p.longitude = obj.getString("longitude");
                            p.secreet = obj.getString("secreet");
                            p.created_at = obj.getString("created_at");
                            p.area = obj.getString("area");
                            p.nama_area = obj.getString("nama_area");

                            p.status = obj.getString("status");
                            p.tanggal_ori = obj.getString("tanggal_ori");
                            // Ambil ODP
                            JSONObject odp = obj.getJSONObject("odp");
                            p.odp_id = odp.getString("id");
                            p.odp_nama = odp.getString("nama");
                            p.odp_type = odp.getString("type");
                            p.odp_port = odp.getString("port");
                            p.odp_latitude = odp.getString("latitude");
                            p.odp_longitude = odp.getString("longitude");

                            p.sn = obj.getString("sn");
                            pelangganList.add(p);
                        }

                        adapter.notifyDataSetChanged();
                        isLoading = false;

                    } catch (Exception e) {
                        isLoading = false;
                        Toast.makeText(ClientActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, TeknisiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

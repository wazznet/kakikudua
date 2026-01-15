package com.wazzgroup.penagihanwifi.map;

import android.annotation.SuppressLint;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.client.DetailPelangganActivity;
import com.wazzgroup.penagihanwifi.client.EditPelangganActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LapanganActivity extends AppCompatActivity {


    private ListView listView;
    private EditText searchBox;
    private ArrayList<lapangan> LapanganList = new ArrayList<>();
    private LapanganAdapter adapter;

    private static final int LIMIT = 20;
    private int currentStart = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isSearching = false;
    String url = GlobalHelper.BASE_URL;
    private AlertDialog dialogTambahJalur;

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

        setContentView(R.layout.activity_lapangan);
        ImageView kembali = findViewById(R.id.back2);
        kembali.setOnClickListener(v -> kembaliKeList());
        Button map = findViewById(R.id.button3);

        Button btnTambahJalur = findViewById(R.id.bttambahjalur);

        btnTambahJalur.setOnClickListener(v -> {
            showDialogTambahJalur();
        });

        map.setOnClickListener(v -> {
            Intent intent = new Intent(LapanganActivity.this, MapActivity.class);
            startActivity(intent);
        });
        listView = findViewById(R.id.listPelanggan);
        searchBox = findViewById(R.id.searchPelanggan);

        adapter = new LapanganAdapter(this, LapanganList);
        listView.setAdapter(adapter);

        ambilDataPelanggan(currentStart);

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
                    ambilDataPelanggan(currentStart);
                } else {
                    isSearching = true;
                    cariPelanggan(s.toString());
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
            Intent i = new Intent(this, ListOdpActivity.class);
            i.putExtra("odp", new Gson().toJson(p));
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


    private void showDialogTambahJalur() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_tambah_jalur, null);
        builder.setView(view);
        builder.setCancelable(false);

        dialogTambahJalur = builder.create();

        // 🔥 WAJIB: set animasi di WINDOW
        if (dialogTambahJalur.getWindow() != null) {
            dialogTambahJalur.getWindow()
                    .setWindowAnimations(R.style.DialogTopAnimation);
            dialogTambahJalur.getWindow()
                    .setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText edtNamaJalur = view.findViewById(R.id.edtNamaJalur);
        Button btnSimpan = view.findViewById(R.id.btnSimpan);
        Button btnBatal = view.findViewById(R.id.btnBatal);

        btnBatal.setOnClickListener(v -> dialogTambahJalur.dismiss());

        btnSimpan.setOnClickListener(v -> {
            String namaJalur = edtNamaJalur.getText().toString().trim();

            if (namaJalur.isEmpty()) {
                edtNamaJalur.setError("Nama jalur tidak boleh kosong");
                return;
            }

            btnSimpan.setEnabled(false);
            kirimJalurBaru(namaJalur);
        });

        dialogTambahJalur.show();
    }

    private void kirimJalurBaru(String namaJalur) {
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "tambah_jalur")
                .add("user", iduserr) // ganti dengan user login jika perlu
                .add("nama_jalur", namaJalur)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LapanganActivity.this, "Gagal tambah jalur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String status = obj.getString("status");
                        String message = obj.getString("message");

                        if (status.equalsIgnoreCase("success")) {
                            Toast.makeText(LapanganActivity.this, "Jalur berhasil ditambah", Toast.LENGTH_SHORT).show();

                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        } else {
                            Toast.makeText(LapanganActivity.this, "Gagal: " + message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(LapanganActivity.this, "Respon tidak valid", Toast.LENGTH_SHORT).show();
                    }
                });
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
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        Button map = dialogView.findViewById(R.id.btnmap);
        map.setVisibility(View.VISIBLE);
//        btnEdit.setOnClickListener(v -> {
//            dialog.dismiss();
//            Intent intent = new Intent(LapanganActivity.this, EditPelangganActivity.class);
//            intent.putExtra("jalur", new Gson().toJson(p));
//            startActivity(intent);
//        });


        map.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(LapanganActivity.this, MapJalurActivity.class);
            intent.putExtra("map", new Gson().toJson(p));
            startActivity(intent);
        });
        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            // TODO: Kirim API untuk hapus pelanggan
            Toast.makeText(this, "Hapus " + p.nama, Toast.LENGTH_SHORT).show();
        });
    }

    private void ambilDataPelanggan(int start) {
        isLoading = true;

        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "alljalur")
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
                    Toast.makeText(LapanganActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
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
                            p.nama = obj.getString("nama_jalur");
                            p.banyak_odp = obj.getString("banyak_odp");
                            LapanganList.add(p);
                        }


                        adapter.notifyDataSetChanged();
                        currentStart += LIMIT; // Naikkan start untuk load berikutnya
                        isLoading = false;

                    } catch (Exception e) {
                        isLoading = false;
                        Toast.makeText(LapanganActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
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
                .add("api", "alljalur")
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
                    Toast.makeText(LapanganActivity.this, "Gagal mencari data", Toast.LENGTH_SHORT).show();
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
                            p.nama = obj.getString("nama_jalur");
                            p.banyak_odp = obj.getString("banyak_odp");
                            LapanganList.add(p);
                        }

                        adapter.notifyDataSetChanged();
                        isLoading = false;

                    } catch (Exception e) {
                        isLoading = false;
                        Toast.makeText(LapanganActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
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
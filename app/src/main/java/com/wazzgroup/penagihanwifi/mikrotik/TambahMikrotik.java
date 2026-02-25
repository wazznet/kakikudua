package com.wazzgroup.penagihanwifi.mikrotik;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.bot.BotAdapter;
import com.wazzgroup.penagihanwifi.bot.BotPenagihanActivity;
import com.wazzgroup.penagihanwifi.bot.bot;
import com.wazzgroup.penagihanwifi.bot.whatsappActivity;
import com.wazzgroup.penagihanwifi.client.EditPelangganActivity;
import com.wazzgroup.penagihanwifi.helper.TokenAuthenticator;
import com.wazzgroup.penagihanwifi.helper.TokenInterceptor;

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

public class TambahMikrotik extends AppCompatActivity {

    private ListView listView;
    private EditText searchBox;
    private ArrayList<mikrotik> pelangganList = new ArrayList<>();
    private MikrotikAdapter adapter;

    private static final int LIMIT = 20;
    private int currentStart = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isSearching = false;
    String url = GlobalHelper.BASE_URL_V2;

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

        setContentView(R.layout.activity_tambah_mikrotik);
        ImageView kembali = findViewById(R.id.back2);
        kembali.setOnClickListener(v -> kembaliKeList());
        Button add = findViewById(R.id.button2);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Inflate layout custom
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_add_mikrotik, null);

                // Inisialisasi field input
                EditText edtIp = dialogView.findViewById(R.id.edtIp);
                EditText edtUsername = dialogView.findViewById(R.id.edtUsername);
                EditText edtPassword = dialogView.findViewById(R.id.edtPassword);
                EditText namaaa = dialogView.findViewById(R.id.nama);

                // Buat dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(TambahMikrotik.this);
                builder.setTitle("Tambah Mikrotik");
                builder.setView(dialogView);

                // Tombol Simpan
                builder.setPositiveButton("Simpan", (dialog, which) -> {
                    String ip = edtIp.getText().toString().trim();
                    String user = edtUsername.getText().toString().trim();
                    String pass = edtPassword.getText().toString().trim();
                    String nama = namaaa.getText().toString().trim();

                    if (ip.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                        Toast.makeText(TambahMikrotik.this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                    } else {
                        OkHttpClient client = new OkHttpClient();
                        String iduserr = GlobalHelper.getIdUser(TambahMikrotik.this);

                        RequestBody formBody = new FormBody.Builder()
                                .add("api", "addmikrotik")
                                .add("iduser", iduserr)
                                .add("ip", ip)
                                .add("user", user)
                                .add("pass", pass)
                                .add("nama", nama)
                                .build();

                        Request request = new Request.Builder()
                                .url(url)
                                .post(formBody)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override public void onFailure(Call call, IOException e) {
                                runOnUiThread(() -> {
                                    isLoading = false;
                                    Toast.makeText(TambahMikrotik.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override public void onResponse(Call call, Response response) throws IOException {
                                String hasil = response.body().string();
                                runOnUiThread(() -> {
                                    try {
                                        JSONObject json = new JSONObject(hasil);
                                        String status = json.getString("status");

                                        if (status.equals("success")) {
                                            Toast.makeText(TambahMikrotik.this, "Berhasil menambahkan data", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(TambahMikrotik.this, TambahMikrotik.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(TambahMikrotik.this, "Gagal menambahkan data", Toast.LENGTH_SHORT).show();
                                        }

                                    } catch (Exception e) {
                                        Toast.makeText(TambahMikrotik.this, "Respon server tidak valid", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });

                // Tombol Batal
                builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

                // Tampilkan dialog
                builder.create().show();
            }
        });
        listView = findViewById(R.id.listPelanggan);

        adapter = new MikrotikAdapter(this, pelangganList);
        listView.setAdapter(adapter);

        ambilDataPelanggan(currentStart);



        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            mikrotik p = adapter.getItem(position);
            tampilkanDialogPilihan(p);
            return true; // Supaya event long click tidak lanjut ke click biasa
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            mikrotik p = adapter.getItem(position);
            Intent i = new Intent(this, Detail_Mikrotik_Activity.class);
            i.putExtra("mikrotik", new Gson().toJson(p));
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
    private void tampilkanDialogPilihan(mikrotik p) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_list_mikrotik, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.show();

        Button btnDelete = dialogView.findViewById(R.id.btnDelete);



        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            OkHttpClient client = new OkHttpClient();
            String iduserr = GlobalHelper.getIdUser(this);

            RequestBody formBody = new FormBody.Builder()
                    .add("api", "dellmikrotik")
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
                        Toast.makeText(TambahMikrotik.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    String hasil = response.body().string();

                    runOnUiThread(() -> {
                        try {
                            JSONObject json = new JSONObject(hasil);

                            String status = json.getString("status");
                            if(status.equals("success")){
                                Toast.makeText(TambahMikrotik.this, "berhasil hapus data", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(TambahMikrotik.this, TambahMikrotik.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(TambahMikrotik.this, "gagal hapus data", Toast.LENGTH_SHORT).show();

                            }

                        } catch (Exception e) {
                            isLoading = false;
                            Toast.makeText(TambahMikrotik.this, "Format data salah", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }

    private void ambilDataPelanggan(int start) {
        isLoading = true;

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "mikrotik")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    Toast.makeText(TambahMikrotik.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
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
                            mikrotik p = new mikrotik();
                            p.id = obj.getString("id");
                            p.nama = obj.getString("nama");
                            pelangganList.add(p);
                        }


                        adapter.notifyDataSetChanged();
                        currentStart += LIMIT; // Naikkan start untuk load berikutnya
                        isLoading = false;

                    } catch (Exception e) {
                        isLoading = false;
                        Toast.makeText(TambahMikrotik.this, "Format data salah", Toast.LENGTH_SHORT).show();
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
package com.wazzgroup.penagihanwifi.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingPaketActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Adapter adapter;
    private String url = GlobalHelper.BASE_URL;
    private List<Class> areaList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting_paket);
        recyclerView = findViewById(R.id.recyclerlog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(areaList);
        recyclerView.setAdapter(adapter);
        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> {
            Intent intent = new Intent(SettingPaketActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        Button btnTambahArea = findViewById(R.id.button);
        btnTambahArea.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Tambah Paket Baru");

            // Layout untuk menampung 2 input
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 10);

            final EditText inputNama = new EditText(this);
            inputNama.setHint("Nama paket");
            inputNama.setInputType(InputType.TYPE_CLASS_TEXT);
            layout.addView(inputNama);

            final EditText inputHarga = new EditText(this);
            inputHarga.setHint("Harga");
            inputHarga.setInputType(InputType.TYPE_CLASS_NUMBER);
            layout.addView(inputHarga);

            builder.setView(layout);

            builder.setPositiveButton("Simpan", (dialog, which) -> {
                String namaPaket = inputNama.getText().toString().trim();
                String hargaStr = inputHarga.getText().toString().trim();

                if (namaPaket.isEmpty()) {
                    Toast.makeText(this, "Nama paket tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (hargaStr.isEmpty()) {
                    Toast.makeText(this, "Harga tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                int harga = Integer.parseInt(hargaStr);
                if (harga <= 0) {
                    Toast.makeText(this, "Harga harus lebih dari 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Panggil fungsi simpan ke API
                tambahPaketKeApi(namaPaket, harga);
            });

            builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

            builder.show();
        });
        fetchArea();
    }
    private void tambahPaketKeApi(String namaArea,int harga) {
        OkHttpClient client = new OkHttpClient();

        String iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "tambah_paket")
                .add("id_user", iduserr)
                .add("nama", namaArea)
                .add("harga", String.valueOf(harga))
                .build();

        Request request = new Request.Builder()
                .url(url) // Ganti dengan URL API kamu
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(resp);
                        if (json.getString("status").equals("success")) {
                            Toast.makeText(getApplicationContext(), "Area berhasil ditambahkan", Toast.LENGTH_SHORT).show();

                            fetchArea();
                        } else {
                            Toast.makeText(getApplicationContext(), "Gagal tambah area", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void fetchArea() {
        OkHttpClient client = new OkHttpClient();

        String iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "paket")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Request gagal: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray dataArray = jsonObject.getJSONArray("data");

                            areaList.clear();
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject obj = dataArray.getJSONObject(i);
                                int id = obj.getInt("ids");
                                String nama = obj.getString("paket");
                                int jumlah = obj.getInt("jumlah");
                                int harga = obj.getInt("harga");
                                areaList.add(new Class(id, nama, jumlah, harga));
                            }

                            runOnUiThread(() -> adapter.notifyDataSetChanged());
                        }

                    } catch (Exception e) {
                        Log.e("API_ERROR", "Parsing error: " + e.getMessage());
                    }
                }
            }
        });
    }
}
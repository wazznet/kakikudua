package com.wazzgroup.penagihanwifi.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;

import java.util.ArrayList;
import java.util.List;

import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class SettingAreaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Adapter adapter;
    private List<Class> areaList = new ArrayList<>();
    private String url = GlobalHelper.BASE_URL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting_area);
        recyclerView = findViewById(R.id.recyclerlog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(areaList);
        recyclerView.setAdapter(adapter);
        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> {
            Intent intent = new Intent(SettingAreaActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        Button btnTambahArea = findViewById(R.id.button);
        btnTambahArea.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Tambah Area Baru");

            // Input field
            final EditText input = new EditText(this);
            input.setHint("Nama Area");
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Simpan", (dialog, which) -> {
                String namaArea = input.getText().toString().trim();

                if (namaArea.isEmpty()) {
                    Toast.makeText(this, "Nama area tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Panggil fungsi simpan ke API
                tambahAreaKeApi(namaArea); // id_user contoh "123"
            });

            builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

            builder.show();
        });
        fetchArea();
    }
    private void tambahAreaKeApi(String namaArea) {
        OkHttpClient client = new OkHttpClient();

        String iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "tambah_area")
                .add("id_user", iduserr)
                .add("nama", namaArea)
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
                .add("api", "area")
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
                                int id = obj.getInt("id");
                                String nama = obj.getString("nama");
                                int jumlah = obj.getInt("jumlah");
                                int harga = 0;
                                areaList.add(new Class(id, nama, jumlah,harga));
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
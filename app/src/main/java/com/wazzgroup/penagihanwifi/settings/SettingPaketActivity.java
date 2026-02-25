package com.wazzgroup.penagihanwifi.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
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
import com.wazzgroup.penagihanwifi.helper.TokenAuthenticator;
import com.wazzgroup.penagihanwifi.helper.TokenInterceptor;

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
    private String url = GlobalHelper.BASE_URL_V2;
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

            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_tambah_paket);
            dialog.setCancelable(true);

            EditText etNamaPaket = dialog.findViewById(R.id.etNamaPaket);
            EditText etHarga = dialog.findViewById(R.id.etHarga);
            Button btnSimpan = dialog.findViewById(R.id.btnSimpan);
            Button btnBatal = dialog.findViewById(R.id.btnBatal);

            btnSimpan.setOnClickListener(view -> {
                String namaPaket = etNamaPaket.getText().toString().trim();
                String hargaStr = etHarga.getText().toString().trim();

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

                tambahPaketKeApi(namaPaket, harga);
                dialog.dismiss();
            });

            btnBatal.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT)
                );

                dialog.getWindow().setGravity(Gravity.CENTER);
            }

        });

        fetchArea();
    }
    private void tambahPaketKeApi(String namaArea,int harga) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();


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
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();


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
package com.wazzgroup.penagihanwifi.halamanpelanggan.tagihan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.halamanpelanggan.PelangganActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PelangganTagihanActivity extends AppCompatActivity {
    String url = GlobalHelper.BASE_URL;

    private ListView listView;
    private ArrayList<TagihanPelanggan> listData = new ArrayList<>();
    private TagihanPelangganAdapter adapter;
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
        setContentView(R.layout.activity_pelanggan_tagihan);



        listView = findViewById(R.id.listTagihan);


        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());

        adapter = new TagihanPelangganAdapter(this, listData);
        listView.setAdapter(adapter);

//        listView.setOnItemClickListener((parent, view, position, id) -> {
//            TagihanPelanggan data = listData.get(position);
//
//            Intent intent = new Intent(PelangganTagihanActivity.this, PembayaranActivity.class);
//            intent.putExtra("id_pelanggan", data.idPelanggan);
//            startActivity(intent);
//        });

        ambilDataTagihan();



    }


    private void ambilDataTagihan() {
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdPelanggan(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "tagihan_pelanggan")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PelangganTagihanActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show());
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
                            TagihanPelanggan t = new TagihanPelanggan();
                            t.idPelanggan = obj.getString("id_pelanggan");
                            t.nama = obj.getString("nama");
                            t.hp = obj.getString("hp");
                            t.paket = obj.getString("nama_paket");
                            t.nama_area = obj.getString("nama_area");
                            t.tanggalpenagihan = obj.getString("tanggal_dibuat");
                            t.bulan = obj.getString("bulan");
                            t.jumlah = obj.getString("jumlah");
                            t.status = obj.getString("status");
                            listData.add(t);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(PelangganTagihanActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }private void kembaliKeList() {
        Intent intent = new Intent(this, PelangganActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

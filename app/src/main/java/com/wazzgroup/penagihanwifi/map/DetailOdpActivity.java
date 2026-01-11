package com.wazzgroup.penagihanwifi.map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;

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

public class DetailOdpActivity extends AppCompatActivity {

    private ListView listView, listView2;
    private ArrayList<lapangan> pelangganList = new ArrayList<>();
    private ArrayList<lapangan> odpList = new ArrayList<>();
    private LapanganAdapter pelangganAdapter;
    private LapanganAdapter odpAdapter;

    private TextView textView7;

    private static final int LIMIT = 20;

    // Pisahkan pagination masing-masing list
    private int startPelanggan = 0;
    private int startOdp = 0;

    private boolean isLoadingPelanggan = false;
    private boolean isLoadingOdp = false;

    private boolean isLastPelanggan = false;
    private boolean isLastOdp = false;

    String url = GlobalHelper.BASE_URL;
    lapangan o;

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

        String json = getIntent().getStringExtra("detail");
        o = new Gson().fromJson(json, lapangan.class);

        setContentView(R.layout.activity_detail_odp);
        ImageView kembali = findViewById(R.id.back2);
        Intent intent = getIntent();
        String idjalur = intent.getStringExtra("idjalur");
        String namajalur = intent.getStringExtra("namajalur");
        kembali.setOnClickListener(v -> kembaliKeList(idjalur,namajalur));

        textView7 = findViewById(R.id.textView7);
        textView7.setText("Detail ODP/ODC " + o.nama);

        pelangganAdapter = new LapanganAdapter(this, pelangganList);
        odpAdapter = new LapanganAdapter(this, odpList);

        TextView nama = findViewById(R.id.nama);
        TextView jalur = findViewById(R.id.jalur);
        TextView type = findViewById(R.id.type);
        TextView port = findViewById(R.id.port);
        TextView lokasi = findViewById(R.id.lokasi);

        nama.setText(o.nama);
        jalur.setText(o.id_jalur);
        type.setText(o.type);
        port.setText(o.port);
        lokasi.setText(o.longitude + "," + o.latitude);

        // Pelanggan list
        listView = findViewById(R.id.listPelanggan);
        listView.setAdapter(pelangganAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}
            @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!isLoadingPelanggan && !isLastPelanggan && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    ambilDataPelanggan(startPelanggan, o.id);
                }
            }
        });

        // ODP list
        listView2 = findViewById(R.id.listodp);
        listView2.setAdapter(odpAdapter);
        listView2.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}
            @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!isLoadingOdp && !isLastOdp && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    ambilDataOdp(startOdp, o.id);
                }
            }
        });
//        listView2.setOnItemClickListener((parent, view, position, id) -> {
//            lapangan p = odpAdapter.getItem(position);
//            Intent i = new Intent(this, DetailOdpActivity.class);
//            i.putExtra("detail", new Gson().toJson(p));
//            startActivity(i);
//        });
        // Load awal
    }

    private void ambilDataOdp(int start, String id) {
        isLoadingOdp = true;

        OkHttpClient client = new OkHttpClient();
        String idUser = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "odpterhubung")
                .add("user", idUser)
                .add("odp", id)
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
                    isLoadingOdp = false;
                    Toast.makeText(DetailOdpActivity.this, "Gagal ambil data ODP", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        JSONArray data = json.getJSONArray("data");

                        if (data.length() < LIMIT) isLastOdp = true;

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            lapangan p = new lapangan();
                            p.id = obj.getString("id");
                            p.nama = obj.getString("nama");
                            p.pan = "pan";
                            odpList.add(p);
                        }

                        odpAdapter.notifyDataSetChanged();
                        startOdp += LIMIT;
                        isLoadingOdp = false;

                    } catch (Exception e) {
                        isLoadingOdp = false;
                        Toast.makeText(DetailOdpActivity.this, "Format data salah (ODP)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void ambilDataPelanggan(int start, String id) {
        isLoadingPelanggan = true;

        OkHttpClient client = new OkHttpClient();
        String idUser = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "pelangganperodp")
                .add("user", idUser)
                .add("odp", id)
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
                    isLoadingPelanggan = false;
                    Toast.makeText(DetailOdpActivity.this, "Gagal ambil data pelanggan", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        JSONArray data = json.getJSONArray("data");

                        if (data.length() < LIMIT) isLastPelanggan = true;

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            lapangan p = new lapangan();
                            p.id = obj.getString("id");
                            p.nama = obj.getString("nama");
                            p.pan = "pan";

                            pelangganList.add(p);
                        }

                        pelangganAdapter.notifyDataSetChanged();
                        startPelanggan += LIMIT;
                        isLoadingPelanggan = false;

                    } catch (Exception e) {
                        isLoadingPelanggan = false;
                        Toast.makeText(DetailOdpActivity.this, "Format data salah (pelanggan)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void kembaliKeList(String idjalur,String namajalur) {
        Intent intent = new Intent(this, ListOdpActivity.class);
        intent.putExtra("id", idjalur);
        intent.putExtra("nama", namajalur);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}

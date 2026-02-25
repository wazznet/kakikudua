package com.wazzgroup.penagihanwifi.mikrotik;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.wazzgroup.penagihanwifi.bot.whatsappActivity;
import com.wazzgroup.penagihanwifi.client.Pelanggan;
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

public class Detail_Mikrotik_Activity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<mikrotik> pelangganList = new ArrayList<>();
    private MikrotikAdapter adapter;
    private static final int LIMIT = 20;
    private int currentStart = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isSearching = false;
    TextView namaText,uptime,cpu, ram , board,totalppp, pppon, pppoff;
    String url = GlobalHelper.BASE_URL_V2;
    private Handler handler = new Handler();
    private Runnable updater;
    private String id; // simpan id biar bisa dipakai di updater
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
        setContentView(R.layout.activity_detail_mikrotik);
        ImageView back = findViewById(R.id.back2);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(this, TambahMikrotik.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        namaText = findViewById(R.id.textView7);
        cpu = findViewById(R.id.cpu);
        uptime = findViewById(R.id.uptime);
        ram = findViewById(R.id.ram);
        board = findViewById(R.id.board);
        totalppp = findViewById(R.id.totals);
        pppon = findViewById(R.id.online);
        pppoff = findViewById(R.id.ofline);
        String json = getIntent().getStringExtra("mikrotik");
        mikrotik d = new Gson().fromJson(json, mikrotik.class);

        namaText.setText("Mikrotik " + d.nama);
         id = d.id;
        updater = new Runnable() {
            @Override
            public void run() {
                datamikrotik(id);
                handler.postDelayed(this, 1000); // refresh tiap 5 detik
            }
        };
        handler.post(updater);
        listView = findViewById(R.id.listppp);

        adapter = new MikrotikAdapter(this, pelangganList);
        listView.setAdapter(adapter);

        ambilDataPelanggan();



        listView.setOnItemClickListener((parent, view, position, id) -> {
//            mikrotik p = adapter.getItem(position);
//            Intent i = new Intent(this, Detail_Mikrotik_Activity.class);
//            i.putExtra("mikrotik", new Gson().toJson(p));
//            startActivity(i);
        });
        LinearLayout add = findViewById(R.id.offline);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ambilDataDariApiDanTampilkan();
            }
        });

    }
    private void ambilDataDariApiDanTampilkan() {
        isLoading = true;

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "apimikrotik")
                .add("user", iduserr)
                .add("ids", id)
                .add("interface", "offline")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    Toast.makeText(Detail_Mikrotik_Activity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        if (json.getString("status").equals("success")) {

                            JSONArray dataArray = json.getJSONArray("data");
                            ArrayList<String> listItems = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject item = dataArray.getJSONObject(i);
                                String name = item.getString("name");
                                String service = item.getString("service");

                                listItems.add(name + " (" + service + ")");
                            }

                            // Buat dialog list
                            AlertDialog.Builder builder = new AlertDialog.Builder(Detail_Mikrotik_Activity.this);
                            builder.setTitle("Daftar User Mikrotik");

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    Detail_Mikrotik_Activity.this,
                                    android.R.layout.simple_list_item_1,
                                    listItems
                            );

                            builder.setAdapter(adapter, (dialog, which) -> {
                                 });

                            builder.setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss());
                            builder.show();

                        } else {
                            Toast.makeText(Detail_Mikrotik_Activity.this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(Detail_Mikrotik_Activity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void datamikrotik(String id) {
        isLoading = true;

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "apimikrotik")
                .add("user", iduserr)
                .add("ids", id)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    Toast.makeText(Detail_Mikrotik_Activity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(hasil);
                        String uptime2 = obj.getString("uptime");
                        String cpu2 = obj.getString("cpu");
                        String free_ram2 = obj.getString("free_ram");
                        String board2 = obj.getString("board");
                        String total_secret = obj.getString("total_secret");
                        String total_online = obj.getString("total_online");
                        String total_offline = obj.getString("total_offline");

                        uptime.setText("Uptime " + uptime2);
                        cpu.setText("CPU " + cpu2+"%");
                        ram.setText("Free RAM " + free_ram2);
                        board.setText("BOARD " + board2);
                        totalppp.setText(total_secret);
                        pppon.setText(total_online);
                        pppoff.setText(total_offline);



                        isLoading = false;

                    } catch (Exception e) {
                        isLoading = false;
                        Toast.makeText(Detail_Mikrotik_Activity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void ambilDataPelanggan() {
        isLoading = true;

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "apimikrotik")
                .add("user", iduserr)
                .add("ids", id)
                .add("interface", "y")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    Toast.makeText(Detail_Mikrotik_Activity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(Detail_Mikrotik_Activity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // stop loop kalau activity ditutup
        handler.removeCallbacks(updater);
    }
}
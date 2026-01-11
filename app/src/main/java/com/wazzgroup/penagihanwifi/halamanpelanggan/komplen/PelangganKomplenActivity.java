package com.wazzgroup.penagihanwifi.halamanpelanggan.komplen;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.halamanpelanggan.PelangganActivity;

import org.json.JSONArray;
import org.json.JSONException;
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

public class PelangganKomplenActivity extends AppCompatActivity {
    String url = GlobalHelper.BASE_URL;


    private OkHttpClient client = new OkHttpClient();
    private ListView listView;
    private ArrayList<komplenpelangganku> listData = new ArrayList<>();
    private KomplenPelangganAdapter adapter;
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
        setContentView(R.layout.activity_pelanggan_komplen);




        listView = findViewById(R.id.listTagihan);


        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());
        ImageView btnInput = findViewById(R.id.btnInput);
        btnInput.setOnClickListener(v -> showInputDialog());
        adapter = new KomplenPelangganAdapter(this, listData);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            komplenpelangganku data = listData.get(position);
            tampilkanDialogPilihan(data);
        });
        ambilDataKomplen();



    }
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input Data");

        // Buat layout form
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);


        final EditText inputKeterangan = new EditText(this);
        inputKeterangan.setHint("Keterangan");
        layout.addView(inputKeterangan);

        builder.setView(layout);

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            String keterangan = inputKeterangan.getText().toString().trim();

            if ( keterangan.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            kirimDataKeAPI(keterangan);
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void kirimDataKeAPI( String keterangan) {
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdPelanggan(this);
        String wifiid = GlobalHelper.getwifiid(this);
        Log.d("ttt ", "-" + client+"-" + iduserr+"-" + wifiid+"-" + keterangan);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "input_komplen")
                .add("user", iduserr)
                .add("wifiid", wifiid)
                .add("keterangan", keterangan)
                .build();

        Request request = new Request.Builder()
                .url(url) // Ganti URL sesuai API kamu
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PelangganKomplenActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Log.d("API_RESPONSE", "Respon dari server: " + json);

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getString("status").equals("success")) {
                            Toast.makeText(PelangganKomplenActivity.this, "Berhasil dikirim!", Toast.LENGTH_SHORT).show();
                            ambilDataKomplen();
                        } else {
                            Toast.makeText(PelangganKomplenActivity.this, "Gagal: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {

                        e.printStackTrace();
                        Toast.makeText(PelangganKomplenActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void tampilkanDialogPilihan(komplenpelangganku p) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_komplen_pelanggan_kita, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.show();
        TextView isikomplen = dialogView.findViewById(R.id.isikomplen);
        isikomplen.setText(p.komplen);
        TextView status = dialogView.findViewById(R.id.status);
        status.setText("status : "+ p.status);
        TextView tanggal_dibuat = dialogView.findViewById(R.id.tanggal_dibuat);
        tanggal_dibuat.setText("tanggal dibuat : "+ p.tanggal_dibuat);
        TextView tanggal_selesai = dialogView.findViewById(R.id.tanggal_selesai);
        tanggal_selesai.setText(
                (p.tanggal_selesai == null || p.tanggal_selesai.isEmpty() || p.tanggal_selesai.equalsIgnoreCase("null"))
                        ? "tanggal selesai :"
                        : "tanggal selesai : " + p.tanggal_selesai
        );



    }
    private void ambilDataKomplen() {
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdPelanggan(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "komplenpelanggan")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PelangganKomplenActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show());
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
                            komplenpelangganku t = new komplenpelangganku();
                            t.idPelanggan = obj.getString("id_pelanggan");
                            t.nama = obj.getString("nama");
                            t.hp = obj.getString("hp");
                            t.paket = obj.getString("nama_paket");
                            t.nama_area = obj.getString("nama_area");
                            t.tanggal_dibuat = obj.getString("tanggal_dibuat");
                            t.tanggal_selesai = obj.getString("tanggal_selesai");
                            t.status = obj.getString("status");
                            t.latitude = obj.getString("latitude");
                            t.longitude = obj.getString("longitude");
                            t.komplen = obj.getString("komplen");
                            t.id_komplen = obj.getString("id");
                            listData.add(t);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(PelangganKomplenActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
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
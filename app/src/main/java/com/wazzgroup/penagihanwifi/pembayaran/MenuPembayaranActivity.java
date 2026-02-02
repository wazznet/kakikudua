package com.wazzgroup.penagihanwifi.pembayaran;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.pembayaran.lunas.TagihanLunasActivity;
import com.wazzgroup.penagihanwifi.pembayaran.nunggak.TagihanNunggakActivity;
import com.wazzgroup.penagihanwifi.pembayaran.tagihan.ManajemenPenagihan;
import com.wazzgroup.penagihanwifi.pembayaran.tagihan.TagihanAdapter;
import com.wazzgroup.penagihanwifi.pembayaran.tagihan.TagihanBelumLunas;
import com.wazzgroup.penagihanwifi.pembayaran.telat.TagihanTelatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MenuPembayaranActivity extends AppCompatActivity {
    TextView pemasukan,telatpemasukan,tagihan,lunas,nunggakk,telatt;
    String url = GlobalHelper.BASE_URL;

    private ListView listView;
    private EditText searchBox;
    private ArrayList<TagihanBelumLunas> listData = new ArrayList<>();
    private TagihanAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_pembayaran);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        pemasukan = findViewById(R.id.pemasukan);
        telatpemasukan = findViewById(R.id.telatpemasukan);
        tagihan = findViewById(R.id.tagihan);
        lunas = findViewById(R.id.lunas);
        telatt = findViewById(R.id.telat);
        nunggakk = findViewById(R.id.nunggak);
        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());
        listView = findViewById(R.id.listTagihan);

        adapter = new TagihanAdapter(this, listData);
        listView.setAdapter(adapter);
        ceksaldo();
        findViewById(R.id.menu1).setOnClickListener(v ->
                startActivity(new Intent(this, ManajemenPenagihan.class))
        );

        findViewById(R.id.menu2).setOnClickListener(v ->
                startActivity(new Intent(this, TagihanLunasActivity.class))
        );
//
        findViewById(R.id.menu3).setOnClickListener(v ->
                startActivity(new Intent(this, TagihanTelatActivity.class))
        );

        findViewById(R.id.menu4).setOnClickListener(v ->
                startActivity(new Intent(this, TagihanNunggakActivity.class))
        );
        cariTagihan();
    }

    private void ceksaldo() {
        String url = GlobalHelper.BASE_URL;
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "banyaknotif") // Pastikan API PHP mendukung login dengan ID
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MenuPembayaranActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getString("status").equals("success")) {

                            String pemasukann = obj.getString("pemasukan");
                            String belummasuk = obj.getString("uangbelummasuk");
                            String belumlunas = obj.getString("belumlunas");
                            String totallunas = obj.getString("total_pelanggan_lunas");
                            String telat = obj.getString("telat");
                            String munggak = obj.getString("munggak");


                            int pemasukanInt = Integer.parseInt(pemasukann); // Ubah ke int
                            NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));
                            String pemasukanku = "Rp. " + formatRupiah.format(pemasukanInt);
                            pemasukan.setText(pemasukanku);

                            int pengeluaranInt = Integer.parseInt(belummasuk); // Ubah ke int
                            String pengeluaranIntku = "Rp. " + formatRupiah.format(pengeluaranInt);
                            telatpemasukan.setText(pengeluaranIntku);

                            int belumlunasInt = Integer.parseInt(belumlunas);
                            tagihan.setText(String.valueOf(belumlunasInt));

                            int lunasInt = Integer.parseInt(totallunas);
                            lunas.setText(String.valueOf(lunasInt));

                            int telatsInt = Integer.parseInt(telat);
                            telatt.setText(String.valueOf(telatsInt));

                            int munggakInt = Integer.parseInt(munggak);
                            nunggakk.setText(String.valueOf(munggakInt));



                        } else {
                            Toast.makeText(MenuPembayaranActivity.this, "Update gagal: ", Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MenuPembayaranActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void cariTagihan() {
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "search_belum_bayar")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MenuPembayaranActivity.this, "Gagal cari data", Toast.LENGTH_SHORT).show());
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
                            TagihanBelumLunas t = new TagihanBelumLunas();
                            t.idPelanggan = obj.getString("id_pelanggan");
                            t.nama = obj.getString("nama");
                            t.hp = obj.getString("hp");
                            t.paket = obj.getString("nama_paket");
                            t.totalTagihan = obj.getString("total_tagihan");
                            t.rincianBulan = obj.getString("rincian_bulan_tagihan");
                            t.nama_area = obj.getString("nama_area");
                            t.tanggalpenagihan = obj.getString("tanggal_dibuat");
                            t.latitude = obj.getString("latitude");
                            t.longitude = obj.getString("longitude");

                            listData.add(t);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(MenuPembayaranActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
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
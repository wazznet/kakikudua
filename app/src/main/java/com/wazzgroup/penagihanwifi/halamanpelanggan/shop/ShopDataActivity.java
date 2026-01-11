package com.wazzgroup.penagihanwifi.halamanpelanggan.shop;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.halamanpelanggan.pay.PayOnline;

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

public class ShopDataActivity extends AppCompatActivity {


    String url = GlobalHelper.BASE_URL;

    private ListView listView;
    private EditText nohp;
    private ArrayList<ClashShop> listData = new ArrayList<>();
    private ShopAdapter adapter;
    private Handler handler = new Handler();
    private Runnable workRunnable;
    String keyword = ""; // <- variabel global

    private OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_data);
        listView = findViewById(R.id.listTagihan);
        nohp = findViewById(R.id.nohp);


        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());

        adapter = new ShopAdapter(this, listData);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ClashShop data = listData.get(position);
            tampilkanDialogPilihan(data);
        });


        nohp.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                keyword = s.toString(); // <- akses variabel global

                if (workRunnable != null) {
                    handler.removeCallbacks(workRunnable);
                }

                workRunnable = () -> {
                    if (keyword.length() >= 2) {
                        carinomer(keyword);
                    } else if (keyword.isEmpty()) {
                        Toast.makeText(ShopDataActivity.this, "Operator tidak ditemukan", Toast.LENGTH_SHORT).show();

                    }
                };

                handler.postDelayed(workRunnable, 500);
            }
        });





    }

    private void tampilkanDialogPilihan(ClashShop p) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_shop_prabayar, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        ImageView btnTutup = dialogView.findViewById(R.id.backtolist);
        btnTutup.setOnClickListener(v -> dialog.dismiss());
        TextView nama_produk = dialogView.findViewById(R.id.nama_produk);
        TextView tujuan = dialogView.findViewById(R.id.tujuan);
        TextView harga_produk = dialogView.findViewById(R.id.harga_produk);
        RadioButton BC = dialogView.findViewById(R.id.BC);
        RadioButton I1 = dialogView.findViewById(R.id.I1);
        RadioButton M2 = dialogView.findViewById(R.id.M2);
        RadioButton BR = dialogView.findViewById(R.id.BR);

        String nama = p.service_name;
        String id_produk = p.id;
        nama_produk.setText(nama);
        String tujuann = keyword;

        tujuan.setText("No Tujuan : " + tujuann);
        int hargaInt = Integer.parseInt(p.price); // Ubah ke int
        NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));
        String formatted = "Rp. " + formatRupiah.format(hargaInt);
        try {
            int hargaInt2 = Integer.parseInt(p.price.trim()); // Trim untuk aman
            if (hargaInt2 < 10000) {
                BC.setVisibility(View.GONE);
                I1.setVisibility(View.GONE);
                M2.setVisibility(View.GONE);
                BR.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // Bisa kasih Toast atau log jika harga tidak valid
        }
        harga_produk.setText(formatted);

        Button btnbeli = dialogView.findViewById(R.id.beli);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup); // Ambil RadioGroup

        btnbeli.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId != -1) {
                // Ambil objek RadioButton yang dipilih
                RadioButton selectedRadio = dialogView.findViewById(selectedId);

                // Ambil nama ID dari RadioButton yang dipilih, misalnya "BC"
                String namaId = getResources().getResourceEntryName(selectedId);
                String pilihan = selectedRadio.getText().toString();

                // Kirim ke fungsi beli
                // Kirim ke fungsi beli
                beli(id_produk, tujuann, namaId,pilihan);
                btnbeli.setEnabled(false);

                // Optional: Ganti teks tombol
                btnbeli.setText("Loading...");
                // atau selectedId kalau kamu ingin ID int-nya // atau selectedId kalau kamu ingin ID int-nya
            } else {
                Toast.makeText(this, "Pilih metode pembayaran terlebih dahulu!", Toast.LENGTH_SHORT).show();
            }
        });









        // Force dialog full width
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // agar background tidak mengganggu
        }
    }

    private void carinomer(String keyword) {

        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "cekno") // Pastikan API PHP mendukung login dengan ID
                .add("data", keyword)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ShopDataActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getString("status").equals("success")) {
                            String operator = obj.getString("operator");
                            layanan(operator);
                        } else {
                            Toast.makeText(ShopDataActivity.this, "operator tidak di temukan ", Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ShopDataActivity.this, "operator tidak di temukan", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void beli(String id_produk, String tujuann,String namaId,String pilihan) {
        String iduserr = GlobalHelper.getIdPelanggan(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "buy") // Pastikan API PHP mendukung login dengan ID
                .add("id_produk", id_produk)
                .add("me", namaId)
                .add("idpelanggan", iduserr)
                .add("target", tujuann)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ShopDataActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getString("status").equals("success")) {
                            String id_pembayaran = obj.getString("id_pembayaran");



                            Intent intent = new Intent(ShopDataActivity.this, PayOnline.class);
                            intent.putExtra("idtransaksi", id_pembayaran);
                            intent.putExtra("metode", pilihan);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ShopDataActivity.this, "Login gagal: " + obj.getString("message"), Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ShopDataActivity.this, "Gagal parsing data"+json+namaId, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void layanan(String operator) {
        OkHttpClient client = new OkHttpClient();
        String idpelanggan = GlobalHelper.getIdPelanggan(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "shop")
                .add("layanan", "2")
                .add("idpelanggan",idpelanggan)
                .add("operator", operator)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ShopDataActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show());
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
                            ClashShop t = new ClashShop();
                            t.id = obj.getString("id");
                            t.category_id = obj.getString("category_id");
                            t.brand = obj.getString("brand");
                            t.service_name = obj.getString("service_name");
                            t.type = obj.getString("type");
                            t.note = obj.getString("note");
                            t.price = obj.getString("price");
                            t.provider_service_id = obj.getString("provider_service_id");
                            t.start_cut_off = obj.getString("start_cut_off");
                            t.end_cut_off = obj.getString("end_cut_off");

                            listData.add(t);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(ShopDataActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, PelangganPembelianActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
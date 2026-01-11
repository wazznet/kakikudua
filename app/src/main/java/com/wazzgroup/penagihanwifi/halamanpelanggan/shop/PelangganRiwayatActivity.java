package com.wazzgroup.penagihanwifi.halamanpelanggan.shop;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
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
import com.wazzgroup.penagihanwifi.halamanpelanggan.PelangganActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PelangganRiwayatActivity extends AppCompatActivity {
    String url = GlobalHelper.BASE_URL;

    private ListView listView;
    private EditText nohp;
    private ArrayList<ClashRiwayat> listData = new ArrayList<>();
    private RiwayatAdapter adapter;
    private Handler handler = new Handler();
    private Runnable workRunnable;
    String keyword = ""; // <- variabel global
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pelanggan_riwayat);
        listView = findViewById(R.id.listTagihan);

        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());
        layanan();
        adapter = new RiwayatAdapter(this, listData);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ClashRiwayat data = listData.get(position);
            tampilkanDialogPilihan(data);
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void tampilkanDialogPilihan(ClashRiwayat p) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_status_transaksi, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        // tombol tutup
        ImageView btnTutup = dialogView.findViewById(R.id.backtolist);
        btnTutup.setOnClickListener(v -> dialog.dismiss());

        // ambil view
        TextView nama_produk = dialogView.findViewById(R.id.nama_produk);
        TextView tujuan = dialogView.findViewById(R.id.tujuan);
        TextView harga_produk = dialogView.findViewById(R.id.harga_produk);
        TextView statuspembayaran = dialogView.findViewById(R.id.statuspembayaran);
        TextView statuspesanan = dialogView.findViewById(R.id.statuspesanan);
        TextView sn = dialogView.findViewById(R.id.sn);
        Button bayar = dialogView.findViewById(R.id.beli2);
        if(Objects.equals(p.status_pembayaran, "sukses")){
            bayar.setVisibility(View.GONE);
        }else{
            Log.d("DEBUG_URL", "Payment URL: " + p.paymentUrl);

            bayar.setOnClickListener(v -> {
                try {
                    if (p.paymentUrl != null && !p.paymentUrl.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(p.paymentUrl));
                        startActivity(intent);
                    } else {
                        Toast.makeText(PelangganRiwayatActivity.this, "URL pembayaran kosong", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(PelangganRiwayatActivity.this, "Gagal membuka browser: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });



        }
        // isi data
        nama_produk.setText(p.service_name);
        tujuan.setText("No Tujuan : " + p.target);

        int hargaInt = Integer.parseInt(p.harga);
        NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));
        harga_produk.setText("Harga produk : Rp. " + formatRupiah.format(hargaInt));

        statuspembayaran.setText("Status Pembayaran : " + p.status_pembayaran);
        statuspesanan.setText("Status Pembelian : " + p.status);
        sn.setText("SN / KODE : " + p.sn);

        // Force dialog full screen
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void layanan() {
        OkHttpClient client = new OkHttpClient();
        String idpelanggan = GlobalHelper.getIdPelanggan(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "riwayattransaksi")
                .add("idpelanggan",idpelanggan)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PelangganRiwayatActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show());
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

                            ClashRiwayat t = new ClashRiwayat();
                            t.id = obj.optString("id");
                            t.id_produk = obj.optString("id_produk");
                            t.target = obj.optString("target");
                            t.harga = obj.optString("harga");
                            t.tanggal = obj.optString("tanggal");
                            t.status_pembayaran = obj.optString("status_pembayaran");
                            t.status = obj.optString("status");
                            t.id_pembayaran = obj.optString("id_pembayaran");
                            t.service_name = obj.optString("service_name");

                            // ambil log_pembelian sebagai string
                            String logStr = obj.optString("log_pembelian", null);
                            if (logStr != null && !logStr.isEmpty()) {
                                try {
                                    JSONObject logObj = new JSONObject(logStr); // parse string JSON
                                    JSONObject dataObj = logObj.optJSONObject("data");
                                    if (dataObj != null) {
                                        t.sn = dataObj.optString("sn", "");

                                    } else {
                                        t.sn = "";
                                    }
                                } catch (Exception e) {
                                    t.sn = ""; // jika parsing gagal
                                }
                            } else {
                                t.sn = "";
                            }
                            String logbayar = obj.optString("log_pembayaran", null);
                            if (logbayar != null && !logbayar.isEmpty()) {
                                try {
                                    JSONObject logObj2 = new JSONObject(logbayar); // parse string JSON
                                    t.paymentUrl = logObj2.optString("paymentUrl", ""); // langsung ambil di sini
                                } catch (Exception e) {
                                    t.paymentUrl = ""; // jika parsing gagal
                                }
                            } else {
                                t.paymentUrl = "";
                            }

                            listData.add(t);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(PelangganRiwayatActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }


                });
            }
        });
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, PelangganActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PelangganRiwayatActivity.this, PelangganActivity.class); // Ganti MainActivity sesuai target
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional, supaya activity sekarang ditutup
    }
}
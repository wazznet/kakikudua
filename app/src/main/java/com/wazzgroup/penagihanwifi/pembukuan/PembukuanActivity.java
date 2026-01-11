package com.wazzgroup.penagihanwifi.pembukuan;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PembukuanActivity extends AppCompatActivity {

    String url = GlobalHelper.BASE_URL;
    private OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pembukuan);
        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());
        Button lihatpembukuan = findViewById(R.id.pembukuantambah);
        lihatpembukuan.setOnClickListener(v -> {
            Intent intent = new Intent(PembukuanActivity.this, PembukuanTambahActivity.class);
            startActivity(intent);
        });
        ceksaldo();
    }
    private void ceksaldo() {
        String iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "pembukuan") // Pastikan API PHP mendukung login dengan ID
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
                        Toast.makeText(PembukuanActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getString("status").equals("success")) {
                            String pemasukan = obj.getString("total_pemasukan");
                            String total_pemasukancash = obj.getString("total_pemasukancash");
                            String total_pemasukanonline = obj.getString("total_pemasukanonline");
                            String total_pemasukanlain = obj.getString("total_pemasukan_lain");
                            String total_pengeluaran_gaji = obj.getString("total_pengeluaran_gaji");
                            String total_pengeluaran_bw = obj.getString("total_pengeluaran_bw");
                            String total_pengeluaran_lain = obj.getString("total_pengeluaran_lain");

                            int pemasukanInt = Integer.parseInt(pemasukan); // Ubah ke int
                            NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));
                            String pemasukanku = "Rp. " + formatRupiah.format(pemasukanInt);
                            TextView pemasukann = findViewById(R.id.pemasukan);
                            pemasukann.setText(pemasukanku);
                            TextView pemasukann2 = findViewById(R.id.pemasukan2);
                            pemasukann2.setText(pemasukanku);
                            TextView pembayarancash = findViewById(R.id.cash);
                            pembayarancash.setText("Rp. "+total_pemasukancash);
                            TextView pembayaranonline = findViewById(R.id.online);
                            pembayaranonline.setText("Rp. "+total_pemasukanonline);

                            TextView pemasukanlain = findViewById(R.id.plain);
                            pemasukanlain.setText("Rp. "+total_pemasukanlain);



                            String pengeluaran = obj.getString("total_pengeluaran");

                            int pengeluaranInt = Integer.parseInt(pengeluaran); // Ubah ke int


                            String pengeluaranku = "Rp. " + formatRupiah.format(pengeluaranInt);
                            String profitku = "Rp. " + formatRupiah.format(pemasukanInt-pengeluaranInt);
                            TextView pengeluarann = findViewById(R.id.pengeluaran);
                            pengeluarann.setText(pengeluaranku);
                            TextView pengeluarann2 = findViewById(R.id.pengeluaran2);
                            pengeluarann2.setText(pengeluaranku);
                            TextView profitn = findViewById(R.id.profit);
                            profitn.setText(profitku);
                            if(pengeluaranInt>pemasukanInt){

                                profitn.setTextColor(Color.parseColor("#FF0000")); // merah
                            }else{
                                profitn.setTextColor(Color.parseColor("#1048F2")); // merah

                            }

                            TextView total_pengeluaran_gajin = findViewById(R.id.gaji);
                            total_pengeluaran_gajin.setText("Rp. "+total_pengeluaran_gaji);

                            TextView total_pengeluaran_bwn = findViewById(R.id.bw);
                            total_pengeluaran_bwn.setText("Rp. "+total_pengeluaran_bw);

                            TextView total_pengeluaran_lainn = findViewById(R.id.plain2);
                            total_pengeluaran_lainn.setText("Rp. "+total_pengeluaran_lain);



                        } else {
                            Toast.makeText(PembukuanActivity.this, "Update gagal: ", Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(PembukuanActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
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
package com.wazzgroup.penagihanwifi.halamanpelanggan.pay;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.halamanpelanggan.shop.PelangganRiwayatActivity;
import com.wazzgroup.penagihanwifi.pembayaran.PembayaranActivity;

import org.json.JSONException;
import org.json.JSONObject;
import com.journeyapps.barcodescanner.BarcodeEncoder;

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

public class PayOnline extends AppCompatActivity {
    private ProgressBar progressBar;
    private OkHttpClient client = new OkHttpClient();
    private ConstraintLayout btnKirim;
    LinearLayout vapage;
    ImageView qrImage;
    Button tombolstatus;
    String url = GlobalHelper.BASE_URL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pay_online);
        progressBar = findViewById(R.id.progressBar);
        vapage = findViewById(R.id.vapage);
        progressBar = findViewById(R.id.progressBar);
        qrImage = findViewById(R.id.qrImage);

        tombolstatus = findViewById(R.id.cek);
        if (getIntent().hasExtra("idtransaksi")) {


            String idtransaksi = getIntent().getStringExtra("idtransaksi");
            String metode = getIntent().getStringExtra("metode");
            TextView total2 = findViewById(R.id.total2);
            total2.setText("metode pembayaran "+ metode);
            showLoading(true);
            qrcode(idtransaksi);
        }else{
            Intent intent = new Intent(PayOnline.this, PelangganRiwayatActivity.class); // Ganti MainActivity sesuai target
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Optional, supaya activity sekarang ditutup
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tombolstatus.setOnClickListener(v -> {
            String idtransaksi = getIntent().getStringExtra("idtransaksi");
            cektransaksi(idtransaksi);
        });
    }
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    private void qrcode(String idtransaksi){
        RequestBody formBody = new FormBody.Builder()
                .add("api", "get_tagihan_online")
                .add("idtransaksi", idtransaksi)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                showLoading(false);
                runOnUiThread(() -> Toast.makeText(PayOnline.this, "Koneksi gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                runOnUiThread(() -> {
                    showLoading(false);
                    try {
                        JSONObject obj = new JSONObject(result);
                        if (obj.getString("status").equals("success")) {
                            JSONObject log = obj.getJSONObject("log_pembayaran");

                            // Ambil nilai amount untuk ditampilkan sebagai total
                            String amount = log.getString("amount");
                            TextView total = findViewById(R.id.total);
                            int hargaInt = Integer.parseInt(amount);
                            NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));
                            String formatted = "Rp. " + formatRupiah.format(hargaInt);
                            total.setText(formatted);

                            try {
                                if (log.has("vaNumber") && !log.isNull("vaNumber")) {
                                    // Tampilkan vaNumber jika tersedia
                                    vapage.setVisibility(View.VISIBLE);
                                    String vaNumber = log.getString("vaNumber");
                                    TextView va = findViewById(R.id.va);

                                    va.setText(vaNumber);

                                    // Salin ke clipboard
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("label", vaNumber);
                                    clipboard.setPrimaryClip(clip);

                                    // Tampilkan pesan sukses
                                    Toast.makeText(PayOnline.this, "VA berhasil disalin!", Toast.LENGTH_SHORT).show();
                                } else if (log.has("qrString") && !log.isNull("qrString")) {
                                    // Jika tidak ada vaNumber, tampilkan QR dari qrString
                                    qrImage.setVisibility(View.VISIBLE);
                                    vapage.setVisibility(View.GONE  );

                                    String qrString = log.getString("qrString");
                                    try {
                                        BarcodeEncoder encoder = new BarcodeEncoder();
                                        Bitmap bitmap = encoder.encodeBitmap(qrString,
                                                com.google.zxing.BarcodeFormat.QR_CODE, 500, 500);
                                        qrImage.setImageBitmap(bitmap);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(PayOnline.this, "Data pembayaran tidak lengkap", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(PayOnline.this, "Terjadi kesalahan data", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(PayOnline.this, "Tagihan tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        Toast.makeText(PayOnline.this, "Format respon salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void cektransaksi(String idtransaksi){
        RequestBody formBody = new FormBody.Builder()
                .add("api", "cek_tagihan")
                .add("idtransaksi", idtransaksi)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                showLoading(false);
                runOnUiThread(() -> Toast.makeText(PayOnline.this, "Koneksi gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                runOnUiThread(() -> {
                    showLoading(false);
                    try {
                        JSONObject obj = new JSONObject(result);
                        if (obj.getString("status").equals("success")) {
                            String message = obj.getString("message");
                            Toast.makeText(PayOnline.this, "pembayaran berhasil", Toast.LENGTH_SHORT).show();
                            gass();

                        } else {
                            Toast.makeText(PayOnline.this, "Cek kembali, pembayaran belum berhasil", Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        Toast.makeText(PayOnline.this, "Format respon salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void gass(){
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(PayOnline.this, PelangganRiwayatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 2000);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PayOnline.this, PelangganRiwayatActivity.class); // Ganti MainActivity sesuai target
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional, supaya activity sekarang ditutup
    }

}
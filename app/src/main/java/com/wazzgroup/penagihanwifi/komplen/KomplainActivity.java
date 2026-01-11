package com.wazzgroup.penagihanwifi.komplen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.pembayaran.ManajemenPenagihan;
import com.wazzgroup.penagihanwifi.pembayaran.PembayaranActivity;
import com.wazzgroup.penagihanwifi.pembayaran.TagihanAdapter;
import com.wazzgroup.penagihanwifi.pembayaran.TagihanBelumLunas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.*;

public class KomplainActivity extends AppCompatActivity {
    String url = GlobalHelper.BASE_URL;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri photoUri;
    private KolmplenPelanggan selectedKomplen; // simpan komplain yang sedang diproses

    private OkHttpClient client = new OkHttpClient();
    private ListView listView;
    private ArrayList<KolmplenPelanggan> listData = new ArrayList<>();
    private KomplenAdapter adapter;
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
        setContentView(R.layout.activity_komplain);


        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        uploadBuktiKomplen(selectedKomplen, photoUri);
                    } else {
                        Toast.makeText(this, "Pengambilan foto dibatalkan", Toast.LENGTH_SHORT).show();
                    }
                }
        );


        listView = findViewById(R.id.listTagihan);


        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());

        adapter = new KomplenAdapter(this, listData);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            KolmplenPelanggan data = listData.get(position);
            tampilkanDialogPilihan(data);
        });

        ambilDataKomplen();



    }
    private void tampilkanDialogPilihan(KolmplenPelanggan p) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_komplen_pelanggan, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.show();
        TextView isikomplen = dialogView.findViewById(R.id.isikomplen);
        Button btnChat = dialogView.findViewById(R.id.btnChat);
        Button btnLokasi = dialogView.findViewById(R.id.btnLokasi);
        Button btnselesai = dialogView.findViewById(R.id.btnselesai);
        isikomplen.setText(p.komplen);
        btnLokasi.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "lokasi langganan " + p.nama, Toast.LENGTH_SHORT).show();
            String latitude = p.latitude;
            String longitude = p.longitude;

            if (latitude == null || longitude == null || latitude.isEmpty() || longitude.isEmpty()) {
                Toast.makeText(this, "lokasi belum di setting" + p.nama, Toast.LENGTH_SHORT).show();
                return;
            }

            String geoUri = "geo:0,0?q=" + latitude + "," + longitude + "(Lokasi Pelanggan)";
            Intent geoIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(geoUri));

            try {
                startActivity(geoIntent);
            } catch (Exception e) {
                String webUri = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
                Intent webIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(webUri));

                try {
                    startActivity(webIntent);
                } catch (Exception ex) {
                    Toast.makeText(this, "Tidak bisa membuka lokasi di Maps atau Browser", Toast.LENGTH_SHORT).show();

                }
            }
        });


        btnChat.setOnClickListener(v -> {
            dialog.dismiss();

            String nomorHP = p.hp;

            if (nomorHP == null || nomorHP.isEmpty()) {
                Toast.makeText(this, "Nomor HP tidak tersedia", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pastikan format nomor internasional, contoh: 628xxxxx
            if (nomorHP.startsWith("0")) {
                nomorHP = "62" + nomorHP.substring(1);
            }

            String pesan = "Halo " + p.nama + ", ini dari tim penagihan. Mohon konfirmasi tagihan Anda ya.";
            String url = "https://wa.me/" + nomorHP + "?text=" + Uri.encode(pesan);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp tidak terpasang", Toast.LENGTH_SHORT).show();
            }
        });
        btnselesai.setOnClickListener(v -> {
            dialog.dismiss(); // tutup dialog

            selectedKomplen = p; // simpan komplen yg akan diupload

            // buat file sementara untuk kamera
            File file = new File(getExternalFilesDir(null), "bukti_komplen_" + System.currentTimeMillis() + ".jpg");
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            takePictureLauncher.launch(photoUri);
        });



    }

    private void uploadBuktiKomplen(KolmplenPelanggan p, Uri uriFoto) {

        String iduserr = GlobalHelper.getIdlogin(this);
        try {
            File file = new File(uriFoto.getPath());

            // Konversi URI ke byte array jika butuh
            InputStream inputStream = getContentResolver().openInputStream(uriFoto);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] imageBytes = byteBuffer.toByteArray();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("api", "komplenselesai")
                    .addFormDataPart("id_komplen", p.id_komplen)
                    .addFormDataPart("user", iduserr)  // Tambahkan user di sini

                    .addFormDataPart("bukti", "bukti.jpg",
                            RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))

                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(KomplainActivity.this, "Gagal upload: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    String hasil = response.body().string();
                    runOnUiThread(() -> {
                        Log.d("RESPON_SERVER", hasil); // Tambahkan log respon di sini

                        Toast.makeText(KomplainActivity.this, hasil, Toast.LENGTH_SHORT).show();
                        ambilDataKomplen(); // refresh data setelah upload
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(KomplainActivity.this, "Gagal proses foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void ambilDataKomplen() {
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);
        String akses = GlobalHelper.getakses(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "komplen")
                .add("user", iduserr)
                .add("akses", akses)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(KomplainActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show());
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
                            KolmplenPelanggan t = new KolmplenPelanggan();
                            t.idPelanggan = obj.getString("id_pelanggan");
                            t.nama = obj.getString("nama");
                            t.hp = obj.getString("hp");
                            t.paket = obj.getString("nama_paket");
                            t.nama_area = obj.getString("nama_area");
                            t.tanggal_dibuat = obj.getString("tanggal_dibuat");
                            t.status = obj.getString("status");
                            t.latitude = obj.getString("latitude");
                            t.longitude = obj.getString("longitude");
                            t.komplen = obj.getString("komplen");
                            t.id_komplen = obj.getString("id_komplen");
                            listData.add(t);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(KomplainActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }private void kembaliKeList() {
        Intent intent = new Intent(this, TeknisiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
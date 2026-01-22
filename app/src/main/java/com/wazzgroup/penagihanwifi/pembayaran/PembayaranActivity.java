package com.wazzgroup.penagihanwifi.pembayaran;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.text.TextUtils;
import android.widget.*;

import android.view.View;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.lottie.LottieSuccessActivity;
import com.wazzgroup.penagihanwifi.pembayaran.lunas.TagihanLunasActivity;
import com.wazzgroup.penagihanwifi.pembayaran.nunggak.TagihanNunggakActivity;
import com.wazzgroup.penagihanwifi.pembayaran.tagihan.ManajemenPenagihan;
import com.wazzgroup.penagihanwifi.pembayaran.telat.TagihanTelatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

import org.json.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PembayaranActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private NfcAdapter nfcAdapter;
    private TextView textNama, cekrfid, textNo, textAlamat, textBulan, textJumlahtagihan, textpaket;
    LinearLayout checkboxContainer;
    private String rfidId = "";
    private OkHttpClient client = new OkHttpClient();
    private ConstraintLayout btnKirim;
    private boolean dariList = false;
    String iduserr;
    String idpenagihan;
    String namawifi;
    String nomerhpku;
    String namaku;
    String url = GlobalHelper.BASE_URL;
    String notanama,notaperiode,notaalamat,notapaket,notatotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pembayaran);
        iduserr = GlobalHelper.getIdUser(this);
        idpenagihan = GlobalHelper.getIdlogin(this);
        namawifi = GlobalHelper.getwifinama(this);
        nomerhpku = GlobalHelper.getno(this);
        namaku = GlobalHelper.getnamaku(this);
        textNama = findViewById(R.id.nama);
        cekrfid = findViewById(R.id.textView13);


        textNo = findViewById(R.id.nomerhp);
        textAlamat = findViewById(R.id.alamat);
        textBulan = findViewById(R.id.listbulan);
        textJumlahtagihan = findViewById(R.id.totaltagihan);
        textpaket = findViewById(R.id.namapaket);
        checkboxContainer = findViewById(R.id.checkboxContainer);


        progressBar = findViewById(R.id.progressBar);
        btnKirim = findViewById(R.id.bayar);
        btnKirim.setOnClickListener(v -> kirimData());
        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        cekrfid.setVisibility(View.VISIBLE);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC tidak tersedia di perangkat ini", Toast.LENGTH_LONG).show();
        }

        // Deteksi apakah activity dipanggil lewat Intent (dari list pelanggan)
        if (getIntent().hasExtra("id_pelanggan")) {

            dariList = true;  // Tandai datang dari list
            cekrfid.setVisibility(View.GONE);
            String idPelanggan = getIntent().getStringExtra("id_pelanggan");
            showLoading(true);
            getTagihanByIdPelanggan(idPelanggan);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_MUTABLE);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                byte[] tagId = tag.getId();
                StringBuilder sb = new StringBuilder();
                for (byte b : tagId) {
                    sb.append(String.format("%02X", b));
                }
                rfidId = sb.toString();
                showLoading(true);
                // Scan NFC langsung cek tagihan
                getTagihanByRfid(rfidId);
            }
        }
    }

    private void getTagihanByRfid(String rfid) {
        RequestBody formBody = new FormBody.Builder()
                .add("api", "pebayaran")
                .add("rfid", rfid)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {

                showLoading(false);
                runOnUiThread(() -> Toast.makeText(PembayaranActivity.this, "Koneksi gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                runOnUiThread(() -> {

                    showLoading(false);
                    try {
                        JSONObject obj = new JSONObject(result);
                        if (obj.getString("status").equals("success")) {
                            rfidId = rfid; // Simpan RFID untuk submit
                            isiDataTagihan(obj);

                        } else {
                            Toast.makeText(PembayaranActivity.this, "Tagihan tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(PembayaranActivity.this, "Format respon salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void getTagihanByIdPelanggan(String idPelanggan) {
        RequestBody formBody = new FormBody.Builder()
                .add("api", "get_tagihan_by_id_pelanggan")
                .add("id_pelanggan", idPelanggan)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                showLoading(false);
                runOnUiThread(() -> Toast.makeText(PembayaranActivity.this, "Koneksi gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                runOnUiThread(() -> {
                    showLoading(false);
                    try {
                        JSONObject obj = new JSONObject(result);
                        if (obj.getString("status").equals("success")) {
                            rfidId = obj.getString("rfid"); // Ambil rfid untuk submit nanti
                            isiDataTagihan(obj);
                        } else {
                            Toast.makeText(PembayaranActivity.this, "Tagihan tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(PembayaranActivity.this, "Format respon salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void isiDataTagihan(JSONObject obj) throws JSONException {
        textNama.setText(obj.getString("nama"));
        textAlamat.setText(obj.getString("area"));
        textNo.setText(obj.getString("nohp"));
        textBulan.setText("Tagihan bulan " + obj.getString("bulan_tahun_tunggakan"));
        textJumlahtagihan.setText("Total tagihan Rp. " + obj.getString("total_tagihan"));
        textpaket.setText("Paket: " + obj.getString("nama_paket"));

        String idTagihanString = obj.getString("id_tagihan");
        String bulanString = obj.getString("bulan");

        String[] idTagihanArrayRaw = idTagihanString.split(",");
        String[] bulanArrayRaw = bulanString.split(",");

// Hilangkan spasi tambahan
        List<String> idTagihanArray = new ArrayList<>();
        List<String> bulanArray = new ArrayList<>();

        for (String s : idTagihanArrayRaw) {
            idTagihanArray.add(s.trim());
        }
        for (String s : bulanArrayRaw) {
            bulanArray.add(s.trim());
        }
        checkboxContainer.removeAllViews();

        for (int i = 0; i < idTagihanArray.size(); i++) {
            String id = idTagihanArray.get(i);
            String bulan = bulanArray.get(i);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText("Tagihan bulan: " + bulan);
            checkBox.setTag(id);
            checkBox.setChecked(true);
            checkBox.setTextColor(Color.parseColor("#000000"));

            checkboxContainer.addView(checkBox);
        }
        notanama = textNama.getText().toString();
        notaperiode = textBulan.getText().toString();
        notatotal = textJumlahtagihan.getText().toString();
        notaalamat = textAlamat.getText().toString();
        notapaket = textpaket.getText().toString();
    }

    private void kirimData() {
        List<String> idTerpilih = new ArrayList<>();
        for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
            View view = checkboxContainer.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox cb = (CheckBox) view;
                if (cb.isChecked()) {
                    idTerpilih.add(cb.getTag().toString());
                }
            }
        }

        if (idTerpilih.isEmpty()) {
            Toast.makeText(this, "Pilih minimal 1 tagihan", Toast.LENGTH_SHORT).show();
            return;
        }

        String idTagihanString = TextUtils.join(",", idTerpilih);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "pebayaran")
                .add("post", "submit")
                .add("user", iduserr)
                .add("idpenagihan", idpenagihan)
                .add("rfid", rfidId)
                .add("idtagihan", idTagihanString)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PembayaranActivity.this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(hasil);
                        if (obj.getString("status").equals("success")) {
                            Toast.makeText(PembayaranActivity.this, "Pembayaran Berhasil", Toast.LENGTH_SHORT).show();
                            printTest();
                            if (dariList) {
                                LottieSuccessActivity.showLottie(findViewById(android.R.id.content));

                                // Jika dari list, balik ke ManajemenPenagihan
                                kembaliKeList();

                            } else {
                                LottieSuccessActivity.showLottie(findViewById(android.R.id.content));

                                // Jika dari scan, tetap di PembayaranActivity
                                kosongkanForm();
                            }
                        } else {
                            Toast.makeText(PembayaranActivity.this, "Gagal: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(PembayaranActivity.this, "Format respon salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void printTest() {
        try {
            SharedPreferences sp = getSharedPreferences("printer", MODE_PRIVATE);
            String mac = sp.getString("mac", null);

            if (mac == null) {
                Toast.makeText(this, "Printer belum diset", Toast.LENGTH_SHORT).show();
                return;
            }

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = adapter.getRemoteDevice(mac);

            BluetoothConnection connection = new BluetoothConnection(device);

            EscPosPrinter printer =
                    new EscPosPrinter(connection, 203, 48f, 32);
            String tanggalRealtime = new SimpleDateFormat(
                    "dd-MM-yyyy HH:mm",
                    Locale.getDefault()
            ).format(new Date());
            printer.printFormattedText(
                    "[C]<b><font size='big'>"+namawifi+"</font></b>\n" +
                            "[C]Internet Cepat & Stabil\n" +
                            "[C]WA : "+nomerhpku+"\n" +
                            "[C]--------------------------------\n" +

                            "[L]Nama      : "+notanama+"\n" +
                            "[L]Alamat    : "+notaalamat+"\n" +
                            "[L]paket     : "+notapaket+"\n" +
                            "[C]--------------------------------\n" +

                            "[L]"+notaperiode+"\n" +
                            "[C]--------------------------------\n" +

                            "[L]<b>"+notatotal+"</b>\n" +
                            "[L]STATUS    : <b>LUNAS</b>\n" +
                            "[C]--------------------------------\n" +

                            "[L]Tanggal   : " + tanggalRealtime +"\n" +
                            "[L]Kasir     : "+namaku+"\n" +
                            "[C]--------------------------------\n" +

                            "[C]Terima kasih atas pembayaran\n" +
                            "[C]Simpan nota ini sebagai\n" +
                            "[C]bukti resmi\n\n\n"
            );

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    private void kosongkanForm() {
        textNama.setText("");
        textNo.setText("");
        textAlamat.setText("");
        textBulan.setText("tagihan bulan");
        textJumlahtagihan.setText("total tagihan");
        textpaket.setText("paket");
        checkboxContainer.removeAllViews();
    }private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnKirim.setClickable(!show);         // Ganti setEnabled dengan setClickable
        btnKirim.setAlpha(show ? 0.5f : 1.0f); // Beri efek transparan saat loading
    }
    private void kembaliKeList() {
        String darihalaman = getIntent().getStringExtra("darihalaman");

        if ("telat".equals(darihalaman)) {

            Intent intent = new Intent(this, TagihanTelatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } else if ("nunggak".equals(darihalaman)) {

            Intent intent = new Intent(this, TagihanNunggakActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        }   else {

            Intent intent = new Intent(this, ManajemenPenagihan.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }


}

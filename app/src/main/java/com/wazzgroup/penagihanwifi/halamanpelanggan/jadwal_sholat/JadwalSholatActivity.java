package com.wazzgroup.penagihanwifi.halamanpelanggan.jadwal_sholat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.halamanpelanggan.PelangganActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JadwalSholatActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView txtImsak,txtWilayah,txtProvinsi,txtTanggal,txtSubuh,txtDzuhur,txtAshar,txtMaghrib,txtIsya;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_jadwal_sholat);

        ImageView kembali = findViewById(R.id.back2);
        kembali.setOnClickListener(v -> kembaliKeList());
        txtWilayah = findViewById(R.id.txtWilayah);
        txtProvinsi = findViewById(R.id.txtProvinsi);
        txtTanggal = findViewById(R.id.tanggal);
        txtSubuh = findViewById(R.id.txtSubuh);
        txtDzuhur = findViewById(R.id.txtDzuhur);
        txtAshar = findViewById(R.id.txtAshar);
        txtMaghrib = findViewById(R.id.txtMaghrib);
        txtIsya = findViewById(R.id.txtIsya);
        txtImsak = findViewById(R.id.txtImsak);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        cekPermission();
    }

    private void cekPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getLokasi();
        }
    }

    private void getLokasi() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        getAlamat(location);
                    } else {
                        txtWilayah.setText("Lokasi tidak ditemukan");
                    }
                });
    }

    private void getAlamat(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses =
                    geocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {

                Address address = addresses.get(0);

                // === KABUPATEN / KOTA ===
                String wilayah = address.getSubAdminArea();

                if (wilayah != null) {
                    wilayah = wilayah.trim();

                    if (wilayah.startsWith("Kabupaten ")) {
                        wilayah = wilayah.replaceFirst("Kabupaten ", "Kab. ");
                    }
                }

                // === PROVINSI ===
                String provinsi = address.getAdminArea();

                if (provinsi != null) {
                    provinsi = provinsi.trim();

                    if (provinsi.startsWith("Provinsi ")) {
                        provinsi = provinsi.replaceFirst("Provinsi ", "");
                    }
                }
                LocalDate now = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    now = LocalDate.now();
                }

                int bulan = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    bulan = now.getMonthValue();
                }
                int tahun = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tahun = now.getYear();
                }

                getJadwalSholat(provinsi, wilayah, bulan, tahun);
                txtWilayah.setText(wilayah);
                txtProvinsi.setText(provinsi);

            }

        } catch (IOException e) {
            e.printStackTrace();
            txtWilayah.setText("Gagal mendapatkan alamat");
        }
    }
    private void getJadwalSholat(String provinsi, String kabkota, int bulan, int tahun) {

        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("provinsi", provinsi);
            json.put("kabkota", kabkota);
            json.put("bulan", bulan);
            json.put("tahun", tahun);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url("https://equran.id/api/v2/shalat")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(JadwalSholatActivity.this,
                                    "Gagal koneksi", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (response.isSuccessful()) {

                        String responseData = response.body().string();

                        runOnUiThread(() -> {
                            try {
                                JSONObject obj = new JSONObject(responseData);

                                // Sesuaikan dengan struktur response API
                                JSONObject data = obj.getJSONObject("data");

                                JSONArray jadwalArray = data.getJSONArray("jadwal");

                                // Contoh ambil hari pertama
                                // Ambil tanggal hari ini
                                Calendar calendar = Calendar.getInstance();
                                int tanggalHariIni = calendar.get(Calendar.DAY_OF_MONTH);

                                int index = tanggalHariIni - 1;
                                if (index < jadwalArray.length()) {
                                    JSONObject hariIni = jadwalArray.getJSONObject(index);

                                    String subuh = hariIni.getString("subuh");
                                    String dzuhur = hariIni.getString("dzuhur");
                                    String ashar = hariIni.getString("ashar");
                                    String maghrib = hariIni.getString("maghrib");
                                    String isya = hariIni.getString("isya");
                                    String imsak = hariIni.getString("imsak");
                                    String tanggal = hariIni.getString("tanggal_lengkap");

                                    txtTanggal.setText("Tanggal " + tanggal);
                                    txtSubuh.setText(subuh);
                                    txtDzuhur.setText(dzuhur);
                                    txtAshar.setText(ashar);
                                    txtMaghrib.setText(maghrib);
                                    txtIsya.setText(isya);
                                    txtImsak.setText(imsak);
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });

                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getLokasi();
        }
    }

    private void kembaliKeList() {
        Intent intent = new Intent(this, PelangganActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
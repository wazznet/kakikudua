package com.wazzgroup.penagihanwifi.map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.*;

import org.json.*;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;
import com.wazzgroup.penagihanwifi.lottie.LottieSuccessActivity;
import com.wazzgroup.penagihanwifi.pembukuan.PembukuanTambahActivity;


import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.psb.PsbActivity;

public class TambahOdpActivity extends AppCompatActivity {
    private Spinner spinnerServer,spinnerPaket,spinnerodp;
    private EditText editNama, editHp, editno,editTanggal;
    private TextView textRfid,koordinatText;
    private Button btnKirim;
    private ConstraintLayout  btnPilihLokasi,btnPilihLokasiOdp;
    private NfcAdapter nfcAdapter;
    private String rfidId = "";
    private List<String> listId = new ArrayList<>();
    private List<String> listId2 = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();
    private TextView txtKoordinat,textOdpDipilih,textOdpidDipilih;
    private FusedLocationProviderClient fusedLocationClient;
    private GeoPoint lokasiTerpilih = null;
    private String idOdpDipilih = "";
    private String namaOdpDipilih = "";

    private JSONArray dataODP;
    private LocationManager locationManager;
    String iduserr;
    String url = GlobalHelper.BASE_URL;
    private String selectedPaketId = ""; // Untuk menyimpan ID paket yang dipilih
    private String paketTerpilihId = "";

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
        setContentView(R.layout.activity_psb);
        ImageView kembali = findViewById(R.id.back);
        kembali.setOnClickListener(v -> kembaliKeList());
        iduserr = GlobalHelper.getIdUser(this);
        editNama = findViewById(R.id.nama);
        editHp = findViewById(R.id.nomerhp);
        editno = findViewById(R.id.editno);
        textRfid = findViewById(R.id.textRfid);
        textOdpDipilih = findViewById(R.id.odp);
        textOdpidDipilih = findViewById(R.id.odpid);
        ConstraintLayout btnKirim = findViewById(R.id.simpan);
        spinnerServer = findViewById(R.id.arenaspinner);
        loadSpinnerData(spinnerServer); // Panggil fungsi tanpa perlu tulis URL
        spinnerPaket = findViewById(R.id.paketspinner);
        loadSpinnerPaket(spinnerPaket); // Panggil fungsi tanpa perlu tulis URL
//        spinnerodp = findViewById(R.id.spinnerodp);
//        loadSpinnerodp(spinnerodp); // Panggil fungsi tanpa perlu tulis URL
        editTanggal = findViewById(R.id.editTanggal);
        koordinatText = findViewById(R.id.koordinat);
        txtKoordinat = findViewById(R.id.koordinat);
        ConstraintLayout btnPilihLokasi = findViewById(R.id.Layoutlokasi);

        btnPilihLokasi.setOnClickListener(v -> bukaDialogPilihLokasi());

        ConstraintLayout  btnPilihLokasiOdp = findViewById(R.id.LayoutOdp);

        btnPilihLokasiOdp.setOnClickListener(v -> bukaDialogPilihLokasiODP());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }


        editTanggal.setOnClickListener(v -> {
            final Calendar kalender = Calendar.getInstance();
            int tahun = kalender.get(Calendar.YEAR);
            int bulan = kalender.get(Calendar.MONTH);
            int hari = kalender.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(TambahOdpActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        // Format: 2025-07-09
                        String tanggal = year + "-" + (month + 1) + "-" + dayOfMonth;
                        editTanggal.setText(tanggal);
                    }, tahun, bulan, hari);

            datePicker.show();
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            textRfid.setText("NFC tidak tersedia di perangkat ini.");
        }

        btnKirim.setOnClickListener(v -> kirimData());
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
                textRfid.setText("ID RFID: " + rfidId);
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private void bukaDialogPilihLokasi() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_map_psb);

        MapView mapView = dialog.findViewById(R.id.mapDialog);
        Button btnSimpan = dialog.findViewById(R.id.btnSimpan);
        Button btnBatal = dialog.findViewById(R.id.btnBatal);

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(17.0);

        final Marker[] marker = {new Marker(mapView)};

        // Ambil lokasi saat ini
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                lokasiTerpilih = new GeoPoint(lat, lon);
                mapView.getController().setCenter(lokasiTerpilih);

                marker[0].setPosition(lokasiTerpilih);
                marker[0].setTitle("Lokasi Anda Sekarang");
                marker[0].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker[0]);
                mapView.invalidate();
            } else {
                Toast.makeText(this, "Lokasi tidak ditemukan, pastikan GPS aktif!", Toast.LENGTH_SHORT).show();
            }
        });

        // Pilih manual jika disentuh
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                lokasiTerpilih = (GeoPoint) mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());

                mapView.getOverlays().remove(marker[0]);

                marker[0] = new Marker(mapView);
                marker[0].setPosition(lokasiTerpilih);
                marker[0].setTitle("Lat: " + lokasiTerpilih.getLatitude() + "\nLon: " + lokasiTerpilih.getLongitude());
                marker[0].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                mapView.getOverlays().add(marker[0]);
                mapView.invalidate();

                return true;
            }
            return false;
        });

        btnSimpan.setOnClickListener(v -> {
            if (lokasiTerpilih != null) {
                txtKoordinat.setText("Latitude: " + lokasiTerpilih.getLatitude() + "\nLongitude: " + lokasiTerpilih.getLongitude());
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Tunggu lokasi atau pilih manual!", Toast.LENGTH_SHORT).show();
            }
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void loadSpinnerData(Spinner spinner) {
        OkHttpClient client = new OkHttpClient();

        // URL server langsung di dalam fungsi
        RequestBody formBody = new FormBody.Builder()
                .add("api", "area")
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
                        Toast.makeText(TambahOdpActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    List<String> listNama = new ArrayList<>();
                    listId.clear();

                    try {
                        JSONObject obj = new JSONObject(json);
                        JSONArray data = obj.getJSONArray("data");

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            listNama.add(item.getString("nama"));
                            listId.add(item.getString("id"));
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(TambahOdpActivity.this,
                                    android.R.layout.simple_spinner_item, listNama) {

                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                                    tv.setTextColor(android.graphics.Color.BLACK); // Set warna hitam
                                    return view;
                                }

                                @Override
                                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getDropDownView(position, convertView, parent);
                                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                                    tv.setTextColor(android.graphics.Color.BLACK); // Set warna hitam juga di dropdown
                                    return view;
                                }
                            };

                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                        });


                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(TambahOdpActivity.this, "Format JSON salah", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(TambahOdpActivity.this, "Respon server error", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
    private void loadSpinnerPaket(Spinner spinner) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("api", "paket")
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
                        Toast.makeText(TambahOdpActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    List<String> listNama = new ArrayList<>();
                    listId2.clear();

                    try {
                        JSONObject obj = new JSONObject(json);
                        JSONArray data = obj.getJSONArray("data");

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            listNama.add(item.getString("paket"));
                            listId2.add(item.getString("ids"));
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(TambahOdpActivity.this,
                                    android.R.layout.simple_spinner_item, listNama) {

                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                                    tv.setTextColor(android.graphics.Color.BLACK);
                                    return view;
                                }

                                @Override
                                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getDropDownView(position, convertView, parent);
                                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                                    tv.setTextColor(android.graphics.Color.BLACK);
                                    return view;
                                }
                            };

                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);

                            // Ambil ID dari pilihan Spinner
                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    String selectedId = listId2.get(position);
                                    Log.d("SPINNER_ID", "ID yang dipilih: " + selectedId);
                                    // Simpan ke variabel global kalau perlu
                                    paketTerpilihId = selectedId;
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    // Kosongkan jika tidak dipilih
                                    paketTerpilihId = null;
                                }
                            });
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(TambahOdpActivity.this, "Format JSON salah", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(TambahOdpActivity.this, "Respon server error", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }


    private void bukaDialogPilihLokasiODP() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, lokasiSekarang -> {
            if (lokasiSekarang != null) {
                double lat = lokasiSekarang.getLatitude();
                double lon = lokasiSekarang.getLongitude();

                ambilDataODPdanTampilkanMap(lat, lon);
            } else {
                Toast.makeText(this, "Lokasi tidak ditemukan!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ambilDataODPdanTampilkanMap(double latUser, double lonUser) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("api", "odp")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TambahOdpActivity.this, "Gagal ambil data ODP: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();

                    try {
                        dataODP = new JSONArray(json);
                        runOnUiThread(() -> tampilkanDialogODP(latUser, lonUser));
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(TambahOdpActivity.this, "Format JSON salah: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void tampilkanDialogODP(double latUser, double lonUser) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_map_psb);

        MapView mapView = dialog.findViewById(R.id.mapDialog);
        Button btnTutup = dialog.findViewById(R.id.btnBatal);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(20.0);

        GeoPoint lokasiUser = new GeoPoint(latUser, lonUser);
        mapView.getController().setCenter(lokasiUser);

        // Ukuran icon dalam pixel
        int iconWidth = 30;  // Atur sesuai kebutuhan (misalnya 60 px)
        int iconHeight = 30;

        // ===== ICON LOKASI USER =====
        Drawable userDrawable = ContextCompat.getDrawable(this, R.drawable.placeholder);
        Bitmap userBitmap = ((BitmapDrawable) userDrawable).getBitmap();
        Bitmap userScaled = Bitmap.createScaledBitmap(userBitmap, iconWidth, iconHeight, false);

        Marker markerUser = new Marker(mapView);
        markerUser.setPosition(lokasiUser);
        markerUser.setTitle("Lokasi Anda");
        markerUser.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerUser.setIcon(new BitmapDrawable(getResources(), userScaled)); // Set icon yg sudah resize
        mapView.getOverlays().add(markerUser);

        // ===== RADIUS AREA =====
        Polygon circle = new Polygon();
        circle.setPoints(Polygon.pointsAsCircle(lokasiUser, 100)); // 500 meter radius
        circle.setStrokeColor(Color.BLUE);
        circle.setFillColor(Color.parseColor("#220000FF"));
        circle.setStrokeWidth(2f);
        mapView.getOverlays().add(circle);

        // ===== ICON ODP =====
        Drawable odpDrawable = ContextCompat.getDrawable(this, R.drawable.gps);
        Bitmap odpBitmap = ((BitmapDrawable) odpDrawable).getBitmap();
        Bitmap odpScaled = Bitmap.createScaledBitmap(odpBitmap, iconWidth, iconHeight, false);

        try {
            for (int i = 0; i < dataODP.length(); i++) {
                JSONObject item = dataODP.getJSONObject(i);
                double latODP = item.getDouble("latitude");
                double lonODP = item.getDouble("longitude");
                String namaODP = item.getString("nama");

                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(latODP, lonODP));
                marker.setTitle(namaODP);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setIcon(new BitmapDrawable(getResources(), odpScaled));

                marker.setOnMarkerClickListener((m, map) -> {
                    tampilkanDialogSimpanODP(item, dialog);
                    return true;
                });

                mapView.getOverlays().add(marker);
            }

        } catch (JSONException e) {
            Toast.makeText(this, "Error JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        btnTutup.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void tampilkanDialogSimpanODP(JSONObject odpData, Dialog parentDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        try {
            String nama = odpData.getString("nama");
            String id = odpData.getString("id");
            String port = odpData.getString("port");

            builder.setTitle("Pilih ODP");
            builder.setMessage("Nama: " + nama + "\nID: " + id + "\nPort: " + port);
            builder.setPositiveButton("Simpan", (dialog, which) -> {
                textOdpDipilih.setText(nama + "-" + id);
                textOdpidDipilih.setText(id);

                // Simpan ke variabel global
                idOdpDipilih = id;
                namaOdpDipilih = nama;

                parentDialog.dismiss();
            });
            builder.setNegativeButton("Batal", null);
            builder.show();

        } catch (JSONException e) {
            Toast.makeText(this, "Error JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private void kirimData() {
        String nama = editNama.getText().toString().trim();
        String no = editno.getText().toString().trim();
        String hp = editHp.getText().toString().trim();
        int posisiarea = spinnerServer.getSelectedItemPosition();

        String area = listId.get(posisiarea);
        int posisiPaket = spinnerPaket.getSelectedItemPosition();
        String paketId = listId2.get(posisiPaket);
        String tanggalDipilih = editTanggal.getText().toString().trim();

        if (nama.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lokasiTerpilih == null) {
            Toast.makeText(this, "Silakan pilih lokasi di peta terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idOdpDipilih.isEmpty()) {
            Toast.makeText(this, "Silakan pilih ODP terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (area.equals("Pilih Area")) {
            Toast.makeText(this, "Silakan pilih area", Toast.LENGTH_SHORT).show();
            return;
        }
        if (paketId.equals("Pilih paket")) {
            Toast.makeText(this, "Silakan pilih paket", Toast.LENGTH_SHORT).show();
            return;
        }

        String latitude = String.valueOf(lokasiTerpilih.getLatitude());
        String longitude = String.valueOf(lokasiTerpilih.getLongitude());

        Log.d("DATA_INPUT", "nama: " + nama + ", hp: " + hp + ", tanggal: " + tanggalDipilih + ", lat: " + latitude + ", lon: " + longitude + ", ODP: " + idOdpDipilih);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "tambahpelanggan")
                .add("user", iduserr)
                .add("hp", hp)
                .add("secreet", no)
                .add("nama", nama)
                .add("area", area)
                .add("paket", paketId)
                .add("tanggaldipilih", tanggalDipilih)
                .add("rfid", rfidId)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .add("idodp", idOdpDipilih)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TambahOdpActivity.this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();
                Log.d("respon",  hasil);

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        String status = json.optString("status", "");
                        String message = json.optString("message", "Tidak ada pesan");
                        if (status.equalsIgnoreCase("success")) {
                            // Kosongkan semua field setelah sukses input
                            editNama.setText("");
                            editno.setText("");
                            editHp.setText("");
                            editTanggal.setText("");
                            textRfid.setText("");
                            rfidId = ""; // Jika ada field RFID direset juga

                            // Reset Spinner ke posisi awal (misalnya posisi 0 = "Pilih Paket" / "Pilih Area")
                            spinnerServer.setSelection(0);
                            spinnerPaket.setSelection(0);

                            // Kosongkan koordinat dan marker
                            lokasiTerpilih = null;
                            koordinatText.setText("Belum dipilih");

                            // Reset ODP
                            idOdpDipilih = "";
                            textOdpDipilih.setText("Belum dipilih");
                            textOdpidDipilih.setText("");

                            // Fokus kembali ke nama agar siap input baru
                            editNama.requestFocus();
                        } else {
                            Toast.makeText(TambahOdpActivity.this, "Gagal: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(TambahOdpActivity.this, "Respon tidak valid dari server", Toast.LENGTH_LONG).show();
                    }
                    LottieSuccessActivity.showLottie(findViewById(android.R.id.content));


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
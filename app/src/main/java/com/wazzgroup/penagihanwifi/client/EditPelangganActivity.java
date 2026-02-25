package com.wazzgroup.penagihanwifi.client;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.helper.TokenAuthenticator;
import com.wazzgroup.penagihanwifi.helper.TokenInterceptor;

import org.json.JSONObject;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditPelangganActivity extends AppCompatActivity {
    private ConstraintLayout btnPilihLokasi, btnPilihLokasiOdp;
    private String latitudeBaru = "";
    private String longitudeBaru = "";

    private EditText editNama, editHp, editNo, editTanggal;
    private Spinner spinnerPaket, spinnerArea;
    private TextView textRfid, txtKoordinat, txtOdpDipilih, txtOdpIdDipilih;
    private ConstraintLayout btnUpdate;
    private FusedLocationProviderClient fusedLocationClient;
    private Pelanggan pelangganLama;
    private List<String> listIdPaket = new ArrayList<>();
    private List<String> listIdArea = new ArrayList<>();
    // Untuk lokasi pelanggan
    private JSONArray dataODP;
    // Untuk ODP
    private String idODPBaru = "";
    private String namaODPBaru = "";
    private String latitudeODPBaru = "";
    private String longitudeODPBaru = "";

    // Untuk tampilan text
    private TextView textODP;
    private NfcAdapter nfcAdapter;
    private String rfidId = ""; // RFID yang discan
    private String idOdp = "";
    // Data lama
    String url = GlobalHelper.BASE_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pelanggan);

        EdgeToEdge.enable(this);
        // Ambil data pelanggan dari intent
        String json = getIntent().getStringExtra("data_pelanggan");
        pelangganLama = new Gson().fromJson(json, Pelanggan.class);
        btnPilihLokasi = findViewById(R.id.Layoutlokasi);
        btnPilihLokasiOdp = findViewById(R.id.LayoutOdp);

        btnPilihLokasi.setOnClickListener(v -> bukaDialogPilihLokasiEdit());
        btnPilihLokasiOdp.setOnClickListener(v -> bukaDialogPilihLokasiODPEdit());


        initUI();
        isiDataLama();
        setListeners();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Perangkat ini tidak mendukung NFC", Toast.LENGTH_SHORT).show();
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
                textRfid.setText(rfidId);
                Toast.makeText(this, "RFID berhasil discan: " + rfidId, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initUI() {
        editNama = findViewById(R.id.nama);
        editHp = findViewById(R.id.nomerhp);
        editNo = findViewById(R.id.editno);
        editTanggal = findViewById(R.id.editTanggal);
        textRfid = findViewById(R.id.textRfid);
        txtKoordinat = findViewById(R.id.koordinat);
        txtOdpDipilih = findViewById(R.id.odp);
        txtOdpIdDipilih = findViewById(R.id.odpid);
        spinnerPaket = findViewById(R.id.paketspinner);
        spinnerArea = findViewById(R.id.arenaspinner);
        btnUpdate = findViewById(R.id.simpan);
        textODP = findViewById(R.id.odp);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Load spinner
        loadSpinnerPaket();
        loadSpinnerArea();
    }

    private void isiDataLama() {
        editNama.setText(pelangganLama.nama);
        editHp.setText(pelangganLama.hp);
        editNo.setText(pelangganLama.secreet);
        editTanggal.setText(pelangganLama.tanggal_ori);
        textRfid.setText(pelangganLama.rfid.isEmpty() ? "Belum ditambahkan" : pelangganLama.rfid);
        txtKoordinat.setText("Lat: " + pelangganLama.latitude + ", Lon: " + pelangganLama.longitude);
        txtOdpDipilih.setText(pelangganLama.odp_nama + "-" + pelangganLama.odp_id);
        txtOdpIdDipilih.setText(pelangganLama.odp_id);
        idOdp = pelangganLama.odp_id;
    }

    private void setListeners() {
        editTanggal.setOnClickListener(v -> {
            Calendar kalender = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String tanggal = year + "-" + (month + 1) + "-" + dayOfMonth;
                editTanggal.setText(tanggal);
            }, kalender.get(Calendar.YEAR), kalender.get(Calendar.MONTH), kalender.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnUpdate.setOnClickListener(v -> updateData());
    }

    private void loadSpinnerPaket() {
        String iduserr = GlobalHelper.getIdUser(this);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("api", "paket")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                List<String> listNama = new ArrayList<>();
                listIdPaket.clear();

                try {
                    JSONObject obj = new JSONObject(json);
                    JSONArray dataArray = obj.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject item = dataArray.getJSONObject(i);
                        listNama.add(item.getString("paket"));
                        listIdPaket.add(item.getString("ids"));
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditPelangganActivity.this, android.R.layout.simple_spinner_item, listNama) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                ((TextView) view).setTextColor(Color.BLACK);
                                return view;
                            }

                            @Override
                            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                ((TextView) view).setTextColor(Color.BLACK);
                                return view;
                            }
                        };

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPaket.setAdapter(adapter);

                        // Pilih paket lama sesuai id
                        int index = listIdPaket.indexOf(pelangganLama.paket);
                        if (index != -1) spinnerPaket.setSelection(index);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void loadSpinnerArea() {
        String iduserr = GlobalHelper.getIdUser(this);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("api", "area")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                List<String> listNama = new ArrayList<>();
                listIdArea.clear();

                try {
                    JSONObject obj = new JSONObject(json);
                    JSONArray dataArray = obj.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject item = dataArray.getJSONObject(i);
                        listNama.add(item.getString("nama"));
                        listIdArea.add(item.getString("id"));
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditPelangganActivity.this, android.R.layout.simple_spinner_item, listNama) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                ((TextView) view).setTextColor(Color.BLACK);
                                return view;
                            }

                            @Override
                            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                ((TextView) view).setTextColor(Color.BLACK);
                                return view;
                            }
                        };

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerArea.setAdapter(adapter);

                        int index = listIdArea.indexOf(pelangganLama.area);
                        if (index != -1) spinnerArea.setSelection(index);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void bukaDialogPilihLokasiEdit() {
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

        // Tampilkan lokasi lama saat pertama kali buka
        double latAwal = Double.parseDouble(pelangganLama.latitude);
        double lonAwal = Double.parseDouble(pelangganLama.longitude);

        final GeoPoint[] lokasiTerpilih = {new GeoPoint(latAwal, lonAwal)};
        mapView.getController().setCenter(lokasiTerpilih[0]);

        marker[0].setPosition(lokasiTerpilih[0]);
        marker[0].setTitle("Lokasi Lama");
        marker[0].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker[0]);
        mapView.invalidate();

        // Pilih manual dengan sentuh peta
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                lokasiTerpilih[0] = (GeoPoint) mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());

                mapView.getOverlays().remove(marker[0]);

                marker[0] = new Marker(mapView);
                marker[0].setPosition(lokasiTerpilih[0]);
                marker[0].setTitle("Lokasi Baru\nLat: " + lokasiTerpilih[0].getLatitude() + "\nLon: " + lokasiTerpilih[0].getLongitude());
                marker[0].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                mapView.getOverlays().add(marker[0]);
                mapView.invalidate();

                return true;
            }
            return false;
        });

        btnSimpan.setOnClickListener(v -> {
            if (lokasiTerpilih[0] != null) {
                latitudeBaru = String.valueOf(lokasiTerpilih[0].getLatitude());
                longitudeBaru = String.valueOf(lokasiTerpilih[0].getLongitude());
                txtKoordinat.setText("Lat: " + latitudeBaru + "\nLon: " + longitudeBaru);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Tunggu lokasi atau pilih manual!", Toast.LENGTH_SHORT).show();
            }
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void updateData() {
        String nama = editNama.getText().toString().trim();
        String hp = editHp.getText().toString().trim();
        String secreet = editNo.getText().toString().trim();
        String tanggal = editTanggal.getText().toString().trim();
        String paket = listIdPaket.get(spinnerPaket.getSelectedItemPosition());
        String area = listIdArea.get(spinnerArea.getSelectedItemPosition());

        // RFID: jika belum discan, pakai data lama
        String rfidKirim = rfidId.isEmpty() ? pelangganLama.rfid : rfidId;

        String latitude = latitudeBaru.isEmpty() ? pelangganLama.latitude : latitudeBaru;
        String longitude = longitudeBaru.isEmpty() ? pelangganLama.longitude : longitudeBaru;

        String idODP = idODPBaru.isEmpty() ? pelangganLama.odp_id : idODPBaru;

        if (nama.isEmpty() || hp.isEmpty() || secreet.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data!", Toast.LENGTH_SHORT).show();
            return;
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("api", "updatepelanggan")
                .add("id", pelangganLama.id)
                .add("nama", nama)
                .add("hp", hp)
                .add("secreet", secreet)
                .add("tanggal", tanggal)
                .add("paket", paket)
                .add("area", area)
                .add("rfid", rfidKirim)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .add("idodp", idODP)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(EditPelangganActivity.this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.d(TAG, "Raw JSON: " + res);

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(res);
                        String status = obj.getString("status");

                        if (status.equalsIgnoreCase("success")) {
                            Toast.makeText(EditPelangganActivity.this, "Berhasil update data", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String pesan = obj.has("message") ? obj.getString("message") : "Gagal update data";
                            Toast.makeText(EditPelangganActivity.this, pesan, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(EditPelangganActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void bukaDialogPilihLokasiODPEdit() {
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

                ambilDataODPEdit(lat, lon);
            } else {
                Toast.makeText(this, "Lokasi tidak ditemukan!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void ambilDataODPEdit(double latUser, double lonUser) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();
        String iduserr = GlobalHelper.getIdUser(this);

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
                runOnUiThread(() -> Toast.makeText(EditPelangganActivity.this, "Gagal ambil data ODP: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();

                    try {
                        dataODP = new JSONArray(json);
                        runOnUiThread(() -> tampilkanDialogODPEdit(latUser, lonUser));
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(EditPelangganActivity.this, "Format JSON salah: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }
    private void tampilkanDialogODPEdit(double latUser, double lonUser) {
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

        // ===== Tampilkan ODP lama =====
        double latODPLama = Double.parseDouble(pelangganLama.odp_latitude);
        double lonODPLama = Double.parseDouble(pelangganLama.odp_longitude);


        Drawable odpDrawable = ContextCompat.getDrawable(this, R.drawable.gps);
        Bitmap odpBitmap = ((BitmapDrawable) odpDrawable).getBitmap();
        Bitmap odpScaled = Bitmap.createScaledBitmap(odpBitmap, 40, 40, false);

        Marker markerODPLama = new Marker(mapView);
        markerODPLama.setPosition(new GeoPoint(latODPLama, lonODPLama));
        markerODPLama.setTitle("ODP Lama: " + pelangganLama.odp_nama);
        markerODPLama.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerODPLama.setIcon(new BitmapDrawable(getResources(), odpScaled));
        mapView.getOverlays().add(markerODPLama);

        // ===== Tampilkan semua ODP =====
        try {
            for (int i = 0; i < dataODP.length(); i++) {
                JSONObject item = dataODP.getJSONObject(i);
                double latODP = item.getDouble("latitude");
                double lonODP = item.getDouble("longitude");
                String namaODP = item.getString("nama");
                String idODP = item.getString("id");
                String port = item.getString("port");

                Marker markerUser = new Marker(mapView);
                markerUser.setPosition(new GeoPoint(latUser, lonUser));
                markerUser.setTitle("Lokasi Anda");
                markerUser.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                markerUser.setIcon(ContextCompat.getDrawable(this, R.drawable.baseline_person_pin_circle_24)); // atau drawable lain
                mapView.getOverlays().add(markerUser);

                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(latODP, lonODP));
                marker.setTitle(namaODP + "\nPort: " + port);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setIcon(new BitmapDrawable(getResources(), odpScaled));

                marker.setOnMarkerClickListener((m, map) -> {
                    tampilkanDialogPilihODPBaru(item, dialog);
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
    private void tampilkanDialogPilihODPBaru(JSONObject odpData, Dialog parentDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        try {
            String nama = odpData.getString("nama");
            String id = odpData.getString("id");
            String port = odpData.getString("port");
            String lat = odpData.getString("latitude");
            String lon = odpData.getString("longitude");

            builder.setTitle("Pilih ODP");
            builder.setMessage("Nama: " + nama + "\nID: " + id + "\nPort: " + port);
            builder.setPositiveButton("Simpan", (dialog, which) -> {
                idODPBaru = id; // Variabel global untuk ID ODP baru
                namaODPBaru = nama;
                latitudeODPBaru = lat;
                longitudeODPBaru = lon;

                textODP.setText(nama + "-" + id);
                parentDialog.dismiss();
            });
            builder.setNegativeButton("Batal", null);
            builder.show();

        } catch (JSONException e) {
            Toast.makeText(this, "Error JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
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

}

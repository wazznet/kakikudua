package com.wazzgroup.penagihanwifi.map;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;

import android.content.Intent;
import android.app.Activity;
import android.location.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView koordinatText;
    private JSONArray dataODP;
    private HashMap<String, GeoPoint> odpPoints = new HashMap<>();
    private OkHttpClient client = new OkHttpClient();
    private FusedLocationProviderClient fusedLocationClient;
    private boolean modeUpdatePelanggan = false;
    private GeoPoint lokasiPelangganBaru;
    String iduserr;
    String url = GlobalHelper.BASE_URL;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map);
        iduserr = GlobalHelper.getIdUser(this);
        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());
        mapView = findViewById(R.id.mapView);
        koordinatText = findViewById(R.id.koordinatText);

        Button btnUpdatePelanggan = findViewById(R.id.updatelokasi);

        btnUpdatePelanggan.setOnClickListener(v -> {
            modeUpdatePelanggan = true;
            Toast.makeText(this, "Klik lokasi baru pelanggan di peta", Toast.LENGTH_SHORT).show();
        });

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(17.0);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getCurrentLocationAndCenterMap();
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {

                if (modeUpdatePelanggan) {
                    lokasiPelangganBaru = (GeoPoint) mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
                    bukaDialogUpdatePelanggan();
                    modeUpdatePelanggan = false;
                    return true;
                }

            }
            return false;
        });
        tampilkanODP();
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, TeknisiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    @SuppressLint("MissingPermission")
    private void getCurrentLocationAndCenterMap() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        client.checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> {
                    // Jika GPS aktif
                    ambilLokasiSekarang(locationRequest);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ((ResolvableApiException) e).startResolutionForResult(MapActivity.this, 101);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        fallbackKeDefault();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void ambilLokasiSekarang(LocationRequest locationRequest) {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                GeoPoint currentPoint = new GeoPoint(lat, lon);
                mapView.getController().setCenter(currentPoint);
            } else {
                // Jika last location null, pakai requestLocationUpdates
                fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            fallbackKeDefault();
                            return;
                        }
                        Location lokasiBaru = locationResult.getLastLocation();
                        double lat = lokasiBaru.getLatitude();
                        double lon = lokasiBaru.getLongitude();

                        GeoPoint currentPoint = new GeoPoint(lat, lon);
                        mapView.getController().setCenter(currentPoint);

                        // Stop setelah dapat lokasi
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }, getMainLooper());
            }
        });
    }

    private void fallbackKeDefault() {
        GeoPoint defaultPoint = new GeoPoint(-8.288095, 113.514681);
        mapView.getController().setCenter(defaultPoint);
        Toast.makeText(this, "Lokasi tidak ditemukan, pakai default", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                // Jika user aktifkan GPS, coba lagi
                getCurrentLocationAndCenterMap();
            } else {
                fallbackKeDefault();
            }
        }
    }

    private void bukaDialogUpdatePelanggan() {
        View view = getLayoutInflater().inflate(R.layout.dialog_update_pelanggan, null);

        Spinner spinnerPelanggan = view.findViewById(R.id.spinnerPelanggan);
        Spinner spinnerODP = view.findViewById(R.id.spinnerODP);

        ArrayList<String> listIdPelanggan = new ArrayList<>();
        ArrayList<String> listNamaPelanggan = new ArrayList<>();

        ArrayList<String> listIdODP = new ArrayList<>();
        ArrayList<String> listNamaODP = new ArrayList<>();

        // Ambil data pelanggan & ODP dari dataODP (yang sudah ada di map)
        try {
            for (int i = 0; i < dataODP.length(); i++) {
                JSONObject obj = dataODP.getJSONObject(i);
                String id = obj.getString("id");
                String nama = obj.getString("nama");
                String sumber = obj.getString("sumber");

                if (sumber.equalsIgnoreCase("pelanggan")) {
                    listIdPelanggan.add(id);
                    listNamaPelanggan.add(nama);
                } else {
                    listIdODP.add(id);
                    listNamaODP.add(nama);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        spinnerPelanggan.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listNamaPelanggan));
        ArrayList<String> listODPDisplay = new ArrayList<>();
        for (int i = 0; i < listIdODP.size(); i++) {
            String gabung = listIdODP.get(i) + " - " + listNamaODP.get(i);
            listODPDisplay.add(gabung);
        }

        spinnerODP.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listODPDisplay));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Update Lokasi Pelanggan")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {

                    String idPelanggan = listIdPelanggan.get(spinnerPelanggan.getSelectedItemPosition());
                    String idODP = listIdODP.get(spinnerODP.getSelectedItemPosition());

                    kirimUpdatePelanggan(idPelanggan, idODP, lokasiPelangganBaru.getLatitude(), lokasiPelangganBaru.getLongitude());

                })
                .setNegativeButton("Batal", null)
                .show();
    }
    private void kirimUpdatePelanggan(String idPelanggan, String idODP, double lat, double lon) {
        RequestBody formBody = new FormBody.Builder()
                .add("api", "update_lokasi_pelanggan")
                .add("id_pelanggan", idPelanggan)
                .add("terhubung_ke", idODP)
                .add("latitude", String.valueOf(lat))
                .add("longitude", String.valueOf(lon))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MapActivity.this, "Gagal update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    Toast.makeText(MapActivity.this, "Update berhasil", Toast.LENGTH_SHORT).show();
                    tampilkanODP(); // Refresh peta
                });
            }
        });
    }

    private void tampilkanODP() {

        RequestBody formBody = new FormBody.Builder()
                .add("api", "map") // atau ganti sesuai kebutuhan Anda
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MapActivity.this, "Gagal ambil ODP: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();

                runOnUiThread(() -> {
                    try {
                        mapView.getOverlays().clear();
                        odpPoints.clear();

                        dataODP = new JSONArray(result);
                        for (int i = 0; i < dataODP.length(); i++) {
                            JSONObject obj = dataODP.getJSONObject(i);
                            String id = obj.getString("id");
                            String nama = obj.getString("nama");
                            String type = obj.getString("type");
                            String port = obj.getString("port");
                            double lat = obj.getDouble("latitude");
                            double lon = obj.getDouble("longitude");
                            String sumber = obj.getString("sumber");
                            String secreet = obj.getString("secreet");
                            String perangkat = obj.getString("perangkat");
                            String typealat = obj.getString("type-alat");
                            String SSID = obj.getString("SSID");
                            String redaman = obj.getString("redaman");
                            GeoPoint point = new GeoPoint(lat, lon);
                            odpPoints.put(id, point);

                            tambahMarkerODP(lat, lon, nama, id, type, port, sumber, secreet
                                    , perangkat, typealat, SSID, redaman);
                        }

                        buatGarisODPtoODP();
                        buatGarisODPtoPelanggan();

                    } catch (Exception e) {
                        Toast.makeText(MapActivity.this, "Data ODP salah format", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void tambahMarkerODP(double lat, double lon, String nama, String id, String type, String port, String sumber, String secreet, String perangkat, String typealat, String SSID, String redaman) {
        GeoPoint point = new GeoPoint(lat, lon);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle("Nama: " + nama + "\nID: " + id);

        marker.setOnMarkerClickListener((m, mapView) -> {
            String info;
            if(Objects.equals(sumber, "pelanggan")){
                 info = "Nama: " + nama + " (" + id + ")\nsecreet: " + secreet +"\nperangkat: " +  perangkat+" ( "+redaman+" ) type: " +  typealat+"\nSSID: " +  SSID+"\nKoordinat: " + lat + ", " + lon ;

            }else{
                 info = "Nama: " + nama + " (" + id + ")\nType: " + type + "\nPort: " + port + "\nKoordinat: " + lat + ", " + lon + "\nSumber: " + sumber;

            }
            koordinatText.setText(info);
            return true;
        });

        // Atur gambar icon sesuai sumber
        int iconRes = sumber.equalsIgnoreCase("odp") ? R.drawable.gps : R.drawable.pin;

        // Ambil Drawable lalu ubah ukuran
        android.graphics.drawable.Drawable drawable = getResources().getDrawable(iconRes);
        android.graphics.Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();

        // Ukuran baru (misalnya 80x80 pixel, bisa disesuaikan)
        int width = 30;
        int height = 30;
        android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, false);

        marker.setIcon(new android.graphics.drawable.BitmapDrawable(getResources(), scaledBitmap));

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }



    private void buatGarisODPtoODP() {
        try {
            for (int i = 0; i < dataODP.length(); i++) {
                JSONObject obj = dataODP.getJSONObject(i);
                String id = obj.getString("id");
                String terhubungKe = obj.getString("terhubung_ke");
                String sumber = obj.getString("sumber");

                if (!terhubungKe.equals("0")) {
                    // Pastikan yang ini sumbernya ODP
                    if (sumber.equalsIgnoreCase("odp")) {

                        double fromLat = obj.getDouble("latitude");
                        double fromLon = obj.getDouble("longitude");
                        GeoPoint fromPoint = new GeoPoint(fromLat, fromLon);

                        // Cari titik tujuan
                        for (int j = 0; j < dataODP.length(); j++) {
                            JSONObject obj2 = dataODP.getJSONObject(j);
                            if (obj2.getString("id").equals(terhubungKe) && obj2.getString("sumber").equalsIgnoreCase("odp")) {
                                double toLat = obj2.getDouble("latitude");
                                double toLon = obj2.getDouble("longitude");
                                GeoPoint toPoint = new GeoPoint(toLat, toLon);

                                Polyline line = new Polyline();
                                line.setPoints(java.util.Arrays.asList(fromPoint, toPoint));
                                line.setWidth(5f);
                                line.setColor(Color.rgb(255, 165, 0)); // Oranye

                                mapView.getOverlays().add(line);
                                break;
                            }
                        }
                    }
                }
            }

            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void buatGarisODPtoPelanggan() {
        try {
            for (int i = 0; i < dataODP.length(); i++) {
                JSONObject obj = dataODP.getJSONObject(i);
                String id = obj.getString("id");
                String terhubungKe = obj.getString("terhubung_ke");
                String sumber = obj.getString("sumber");

                if (!terhubungKe.equals("0")) {
                    // Ini pelanggan
                    if (sumber.equalsIgnoreCase("pelanggan")) {

                        double fromLat = obj.getDouble("latitude");
                        double fromLon = obj.getDouble("longitude");
                        GeoPoint fromPoint = new GeoPoint(fromLat, fromLon);

                        // Cari titik tujuan (harus ODP)
                        for (int j = 0; j < dataODP.length(); j++) {
                            JSONObject obj2 = dataODP.getJSONObject(j);
                            if (obj2.getString("id").equals(terhubungKe) && obj2.getString("sumber").equalsIgnoreCase("odp")) {
                                double toLat = obj2.getDouble("latitude");
                                double toLon = obj2.getDouble("longitude");
                                GeoPoint toPoint = new GeoPoint(toLat, toLon);

                                Polyline line = new Polyline();
                                line.setPoints(java.util.Arrays.asList(fromPoint, toPoint));
                                line.setWidth(5f);
                                line.setColor(Color.BLACK);

                                mapView.getOverlays().add(line);
                                break;
                            }
                        }
                    }
                }
            }

            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

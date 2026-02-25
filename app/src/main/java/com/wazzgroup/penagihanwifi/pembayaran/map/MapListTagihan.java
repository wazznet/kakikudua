package com.wazzgroup.penagihanwifi.pembayaran.map;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wazzgroup.penagihanwifi.KangtagihActivity;
import com.wazzgroup.penagihanwifi.R;

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
import com.wazzgroup.penagihanwifi.helper.TokenAuthenticator;
import com.wazzgroup.penagihanwifi.helper.TokenInterceptor;
import com.wazzgroup.penagihanwifi.map.MapActivity;
import com.wazzgroup.penagihanwifi.pembayaran.tagihan.ManajemenPenagihan;

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
public class MapListTagihan extends AppCompatActivity {

    private MapView mapView;
    private TextView koordinatText;
    private JSONArray dataODP;
    private HashMap<String, GeoPoint> odpPoints = new HashMap<>();
    private FusedLocationProviderClient fusedLocationClient;
    private boolean modeUpdatePelanggan = false;
    private GeoPoint lokasiPelangganBaru;
    String iduserr;
    String url = GlobalHelper.BASE_URL_V2;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map_list_tagihan);
        iduserr = GlobalHelper.getIdUser(this);
        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());
        mapView = findViewById(R.id.mapView);
        koordinatText = findViewById(R.id.koordinatText);



        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(17.0);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getCurrentLocationAndCenterMap();
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {



            }
            return false;
        });
        tampilkanODP();
    }
    private void kembaliKeList() {
        String akses = GlobalHelper.getakses(this);
        if(akses.equals("kangtagih")){
            Intent intent = new Intent(this, KangtagihActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }else {
            Intent intent = new Intent(this, ManajemenPenagihan.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
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
                            ((ResolvableApiException) e).startResolutionForResult(MapListTagihan.this, 101);
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



    private void tampilkanODP() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();
        RequestBody formBody = new FormBody.Builder()
                .add("api", "search_belum_bayar")
                .add("user", iduserr)
                .add("keyword", "")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MapListTagihan.this,
                                "Gagal ambil ODP",
                                Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String result = response.body().string();

                runOnUiThread(() -> {
                    try {

                        JSONObject responseObj = new JSONObject(result);

                        if (!responseObj.getString("status").equals("success")) {
                            Toast.makeText(MapListTagihan.this,
                                    "Data tidak ditemukan",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray dataArray = responseObj.getJSONArray("data");

                        mapView.getOverlays().clear();
                        odpPoints.clear();

                        for (int i = 0; i < dataArray.length(); i++) {

                            JSONObject obj = dataArray.getJSONObject(i);

                            String id = obj.getString("id_pelanggan");
                            String nama = obj.getString("nama");
                            String hp = obj.getString("hp");
                            double lat = obj.getDouble("latitude");
                            double lon = obj.getDouble("longitude");

                            GeoPoint point = new GeoPoint(lat, lon);
                            odpPoints.put(id, point);

                            tambahMarkerODP(lat, lon, nama, id, hp);
                        }

                    } catch (Exception e) {
                        Toast.makeText(MapListTagihan.this,
                                "Format JSON salah",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void tambahMarkerODP(double lat, double lon, String nama, String id, String hp) {
        GeoPoint point = new GeoPoint(lat, lon);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle("Nama: " + nama );

        marker.setOnMarkerClickListener((m, mapView) -> {
            String info;
                info = "Nama: " + nama+" \nNo HP: " + hp ;


            koordinatText.setText(info);
            return true;
        });

        // Atur gambar icon sesuai sumber
        int iconRes = R.drawable.gps;

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







}

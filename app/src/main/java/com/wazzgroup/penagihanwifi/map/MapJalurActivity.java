package com.wazzgroup.penagihanwifi.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.TeknisiActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.wazzgroup.penagihanwifi.helper.TokenAuthenticator;
import com.wazzgroup.penagihanwifi.helper.TokenInterceptor;

import androidx.core.app.ActivityCompat;

public class MapJalurActivity extends AppCompatActivity {


    private List<String> listId = new ArrayList<>();
    private MapView mapView;
    private TextView koordinatText;
    private JSONArray dataODP;
    private HashMap<String, GeoPoint> odpPoints = new HashMap<>();
    String iduserr;
    String url = GlobalHelper.BASE_URL;
    lapangan o;
    private FusedLocationProviderClient fusedLocationClient; // 🔹 Tambahan

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map_jalur);

        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());
        mapView = findViewById(R.id.mapView);
        koordinatText = findViewById(R.id.koordinatText);
        iduserr = GlobalHelper.getIdUser(this);
        // Inisialisasi MapView
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(17.0);

        // 🔹 Inisialisasi lokasi perangkat
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 🔹 Cek izin lokasi dan set titik awal ke lokasi perangkat
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);

        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                GeoPoint startPoint;
                    startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                mapController.setCenter(startPoint);

                // 🔹 Tambahkan marker posisi perangkat (opsional)


                mapView.invalidate();
            });
        }

        // 🔹 Ambil data JSON dari intent
        String json = getIntent().getStringExtra("map");
        o = new Gson().fromJson(json, lapangan.class);

        tampilkanODP(o.id);
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, TeknisiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void tampilkanODP(String jalur) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();


        Log.d("DATA_INPUT", "nama: " + jalur );

        RequestBody formBody = new FormBody.Builder()
                .add("api", "odp")
                .add("id_jalur", jalur)
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MapJalurActivity.this, "Gagal ambil ODP: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();

                runOnUiThread(() -> {
                    try {
                        // Clear data lama
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

                            GeoPoint point = new GeoPoint(lat, lon);
                            odpPoints.put(id, point);

                            tambahMarkerODP(lat, lon, nama, id, type, port);
                        }

                        buatGarisODP();

                    } catch (Exception e) {
                        Toast.makeText(MapJalurActivity.this, "Data ODP salah format", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void tambahMarkerODP(double lat, double lon, String nama, String id, String type, String port) {
        GeoPoint point = new GeoPoint(lat, lon);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle("ODP: " + nama + "\nID: " + id);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        Drawable iconDrawable;

        if (Objects.equals(type, "odp")) {
            iconDrawable = getResources().getDrawable(R.drawable.odp);
        } else if (Objects.equals(type, "odc")) {
            iconDrawable = getResources().getDrawable(R.drawable.odc);
        } else {
            iconDrawable = getResources().getDrawable(R.drawable.rumah2);
        }

        // 🔹 Ubah ukuran icon menjadi 30x30 px
        Bitmap bitmap = ((BitmapDrawable) iconDrawable).getBitmap();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 30, 30, false);
        Drawable resizedDrawable = new BitmapDrawable(getResources(), resizedBitmap);
        marker.setIcon(resizedDrawable);

        // 🔹 Aksi ketika marker diklik
        marker.setOnMarkerClickListener((m, mapView) -> {
            String info = "ODP: " + nama + "\nID: " + id + "\nType: " + type + "\nPort: " + port + "\nKoordinat: " + lat + ", " + lon;
            koordinatText.setText(info);
            return true;
        });

        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }



    private void buatGarisODP() {
        try {
            for (int i = 0; i < dataODP.length(); i++) {
                JSONObject obj = dataODP.getJSONObject(i);
                String id = obj.getString("id");
                String terhubungKe = obj.getString("terhubung_ke");

                if (!terhubungKe.equals("0") && odpPoints.containsKey(terhubungKe)) {
                    GeoPoint from = odpPoints.get(id);
                    GeoPoint to = odpPoints.get(terhubungKe);

                    Polyline line = new Polyline();
                    line.setPoints(java.util.Arrays.asList(from, to));
                    line.setWidth(5f);
                    line.setColor(Color.RED);

                    mapView.getOverlays().add(line);
                }
            }

            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

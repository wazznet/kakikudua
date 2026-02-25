package com.wazzgroup.penagihanwifi.map;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
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
import com.wazzgroup.penagihanwifi.lottie.LottieSuccessActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditOdpActivity extends AppCompatActivity {
    private lapangan datalama;
    private TextView txtKoordinat,textOdpDipilih,textOdpidDipilih,namaodp;
    private FusedLocationProviderClient fusedLocationClient;
    private GeoPoint lokasiTerpilih = null;
    private String idOdpDipilih = "";

    private String namaOdpDipilih = "";
    private JSONArray dataODP;
    String iduserr;
    String url = GlobalHelper.BASE_URL_V2;
    Spinner spinnerType, spinnerPort;
    EditText notetxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_odp);
        String json = getIntent().getStringExtra("data_odp");
        datalama = new Gson().fromJson(json, lapangan.class);
        iduserr = GlobalHelper.getIdUser(this);
        spinnerType = findViewById(R.id.typeodp);
        spinnerPort = findViewById(R.id.port);

         namaodp = findViewById(R.id.namaodp);


        String[] dataType = {"odp", "odc", "closure"};

        ArrayAdapter<String> adapterType = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                dataType
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                return tv;
            }
        };

        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapterType);

// AUTO SELECT TYPE
        if (datalama != null && datalama.type != null) {
            for (int i = 0; i < dataType.length; i++) {
                if (dataType[i].equalsIgnoreCase(datalama.type)) {
                    spinnerType.setSelection(i);
                    break;
                }
            }
        }

        String[] dataPort = {"0", "2", "4", "8", "16", "32"};

        ArrayAdapter<String> adapterPort = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                dataPort
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                return tv;
            }
        };

        adapterPort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPort.setAdapter(adapterPort);

// AUTO SELECT PORT
        if (datalama != null && datalama.port != null) {
            for (int i = 0; i < dataPort.length; i++) {
                if (dataPort[i].equals(datalama.port)) {
                    spinnerPort.setSelection(i);
                    break;
                }
            }
        }


        textOdpDipilih = findViewById(R.id.odp);
        textOdpidDipilih = findViewById(R.id.odpid);
        ConstraintLayout btnKirim = findViewById(R.id.simpan);

        txtKoordinat = findViewById(R.id.koordinat);
        notetxt = findViewById(R.id.editTextText);

        ConstraintLayout btnPilihLokasi = findViewById(R.id.Layoutlokasi);

        btnPilihLokasi.setOnClickListener(v -> bukaDialogPilihLokasi());

        ConstraintLayout  btnPilihLokasiOdp = findViewById(R.id.LayoutOdp);

        btnPilihLokasiOdp.setOnClickListener(v -> bukaDialogPilihLokasiODP());


        ImageView kembali = findViewById(R.id.back);
        kembali.setOnClickListener(v -> kembaliKeList(datalama.id_jalur,datalama.nama_jalur));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }




        btnKirim.setOnClickListener(v -> kirimData(datalama.id_jalur));
        isiDataLama();
    }
    private void isiDataLama() {
        namaodp.setText(datalama.nama);
        if(Objects.equals(datalama.note, "null")){
            notetxt.setText("");
        }else{
            notetxt.setText(datalama.note);
        }
        //notetxt.setText(datalama.note);
        txtKoordinat.setText("Lat: " + datalama.latitude + ", Lon: " + datalama.longitude);
        textOdpDipilih.setText(datalama.nama_terhubung);

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
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();

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
                runOnUiThread(() -> Toast.makeText(EditOdpActivity.this, "Gagal ambil data ODP: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();

                    try {
                        dataODP = new JSONArray(json);
                        runOnUiThread(() -> tampilkanDialogODP(latUser, lonUser));
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(EditOdpActivity.this, "Format JSON salah: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
    private void kirimData(String idjalur) {
        String note = notetxt.getText().toString().trim();

        String type = spinnerType.getSelectedItem().toString();
        String port = spinnerPort.getSelectedItem().toString();

        if (lokasiTerpilih == null) {
            Toast.makeText(this, "Silakan pilih lokasi di peta terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idOdpDipilih == null || idOdpDipilih.isEmpty()) {
            Toast.makeText(this, "Silakan pilih ODP terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (type.equals("Pilih Area")) {
            Toast.makeText(this, "Silakan pilih area", Toast.LENGTH_SHORT).show();
            return;
        }

        if (port.equals("Pilih paket")) {
            Toast.makeText(this, "Silakan pilih paket", Toast.LENGTH_SHORT).show();
            return;
        }

        String latitudeBaru = String.valueOf(lokasiTerpilih.getLatitude());
        String longitudeBaru = String.valueOf(lokasiTerpilih.getLongitude());
        String latitude = latitudeBaru.isEmpty() ? datalama.latitude : latitudeBaru;
        String longitude = longitudeBaru.isEmpty() ? datalama.longitude : longitudeBaru;

        String idODP = idOdpDipilih.isEmpty() ? datalama.terhubung_ke : idOdpDipilih;
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(this))
                .authenticator(new TokenAuthenticator(this))
                .build();

        Log.d("DATA_INPUT", "id jalur: " + idjalur +
                ", type: " + type +
                ", port: " + port +
                ", lat: " + latitude +
                ", lon: " + longitude +
                ", terhubung_ke: " + idODP);

        RequestBody formBody = new FormBody.Builder()
                .add("post", "Tambahodpbaru") // ⬅️ FIX
                .add("user", iduserr)
                .add("idjalur", idjalur)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .add("idodp", idODP)
                .add("type", type)
                .add("port", port)
                .add("note", note)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(EditOdpActivity.this,
                                "Gagal: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String hasil = response.body().string();
                Log.d("RESPON_API", hasil);

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        String status = json.optString("status");
                        String message = json.optString("message");

                        if ("success".equalsIgnoreCase(status)) {
                            Intent intent = new Intent(EditOdpActivity.this, LapanganActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            Toast.makeText(EditOdpActivity.this,
                                    "Berhasil: " + message,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(EditOdpActivity.this,
                                    "Gagal: " + message,
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(EditOdpActivity.this,
                                "Respon server tidak valid",
                                Toast.LENGTH_LONG).show();
                    }

                    LottieSuccessActivity.showLottie(findViewById(android.R.id.content));
                });
            }
        });
    }


    private void kembaliKeList(String idjalur,String namajalur) {
        Intent intent = new Intent(this, ListOdpActivity.class);
        intent.putExtra("id", idjalur);
        intent.putExtra("nama", namajalur);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


}
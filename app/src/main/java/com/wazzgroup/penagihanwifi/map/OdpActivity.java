package com.wazzgroup.penagihanwifi.map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OdpActivity extends AppCompatActivity {

    private List<String> listId = new ArrayList<>();
    private Spinner sjalur;
    private MapView mapView;
    private TextView koordinatText;
    private JSONArray dataODP;
    private HashMap<String, GeoPoint> odpPoints = new HashMap<>();
    private OkHttpClient client = new OkHttpClient();
    private boolean modeTambahODP = false;
    private Marker previewMarker = null;
    String iduserr;
    String url = GlobalHelper.BASE_URL;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_odp);

        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> kembaliKeList());
        Button btnTambahODP = findViewById(R.id.btnTambahODP);
        sjalur = findViewById(R.id.jalur);
        mapView = findViewById(R.id.mapView);
        koordinatText = findViewById(R.id.koordinatText);
        iduserr = GlobalHelper.getIdUser(this);
        // Inisialisasi MapView
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(17.0);
        GeoPoint startPoint = new GeoPoint(-8.288095, 113.514681);
        mapController.setCenter(startPoint);
        Button btnTambahJalur = findViewById(R.id.btnTambahJalur);

        btnTambahJalur.setOnClickListener(v -> {
            showDialogTambahJalur();
        });
        loadSpinnerjalur(sjalur); // Panggil load spinner jalur
        sjalur.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                tampilkanODP(); // Setiap kali spinner berubah, langsung refresh ODP di map
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Tidak perlu diisi
            }
        });

        btnTambahODP.setOnClickListener(v -> {
            modeTambahODP = true;
            Toast.makeText(this, "Pilih lokasi ODP dengan klik di peta", Toast.LENGTH_SHORT).show();
        });

        // Event klik peta
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP && modeTambahODP) {
                GeoPoint point = (GeoPoint) mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
                pilihLokasiODP(point);
                modeTambahODP = false;
                return true;
            }
            return false;
        });
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, TeknisiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void loadSpinnerjalur(Spinner spinner) {

        RequestBody formBody = new FormBody.Builder()
                .add("api", "jalur")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OdpActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                            listNama.add(item.getString("nama_jalur"));
                            listId.add(item.getString("id"));
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(OdpActivity.this,
                                    android.R.layout.simple_spinner_item, listNama) {
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
                            spinner.setAdapter(adapter);

                            // Setelah spinner terisi, tampilkan ODP
                            tampilkanODP();
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(OdpActivity.this, "Format JSON salah", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(OdpActivity.this, "Respon server error", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void tampilkanODP() {

        int posisi = sjalur.getSelectedItemPosition();
        String jalur = listId.get(posisi);
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
                runOnUiThread(() -> Toast.makeText(OdpActivity.this, "Gagal ambil ODP: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                        Toast.makeText(OdpActivity.this, "Data ODP salah format", Toast.LENGTH_SHORT).show();
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

    private void pilihLokasiODP(GeoPoint lokasiTerpilih) {
        View view = getLayoutInflater().inflate(R.layout.dialog_tambah_odp, null);
        int posisi = sjalur.getSelectedItemPosition();
        String id_jalur = listId.get(posisi);

        EditText editNama = view.findViewById(R.id.editNamaOdp);
        Spinner spinnerTerhubung = view.findViewById(R.id.spinnerTerhubungKe);
        Spinner spinnerType = view.findViewById(R.id.spinnerType);
        Spinner spinnerPort = view.findViewById(R.id.spinnerPort);

        ArrayList<String> listIdODP = new ArrayList<>();
        ArrayList<String> listNamaODP = new ArrayList<>();

        listIdODP.add("0");
        listNamaODP.add("Tidak terhubung");

        try {
            for (int i = 0; i < dataODP.length(); i++) {
                JSONObject obj = dataODP.getJSONObject(i);
                String id = obj.getString("id");
                String nama = obj.getString("nama");

                listIdODP.add(id);
                listNamaODP.add(nama);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        spinnerTerhubung.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listNamaODP));
        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"odp", "odc", "server", "spliter", "rasio"}));
        spinnerPort.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"2", "4", "8", "16", "32"}));

        if (previewMarker != null) {
            mapView.getOverlays().remove(previewMarker);
        }

        previewMarker = new Marker(mapView);
        previewMarker.setPosition(lokasiTerpilih);
        previewMarker.setTitle("Lokasi ODP baru");
        previewMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(previewMarker);
        mapView.invalidate();

        new AlertDialog.Builder(this)
                .setTitle("Tambah ODP")
                .setView(view)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String nama = editNama.getText().toString();
                    String terhubungKe = listIdODP.get(spinnerTerhubung.getSelectedItemPosition());
                    String type = spinnerType.getSelectedItem().toString();
                    String port = spinnerPort.getSelectedItem().toString();

                    if (nama.isEmpty()) {
                        Toast.makeText(this, "Nama ODP wajib diisi", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    kirimODPBaru(lokasiTerpilih.getLatitude(), lokasiTerpilih.getLongitude(),id_jalur, nama, terhubungKe, type, port);

                    mapView.getOverlays().remove(previewMarker);
                    previewMarker = null;

                })
                .setNegativeButton("Batal", (dialog, which) -> {
                    mapView.getOverlays().remove(previewMarker);
                    mapView.invalidate();
                    previewMarker = null;
                })
                .show();
    }

    private void kirimODPBaru(double lat, double lon, String id_jalur, String nama, String terhubungKe, String type, String port) {
        RequestBody formBody = new FormBody.Builder()
                .add("api", "tambah_odp")
                .add("user", iduserr)
                .add("id_jalur", id_jalur)
                .add("nama", nama)
                .add("latitude", String.valueOf(lat))
                .add("longitude", String.valueOf(lon))
                .add("terhubung_ke", terhubungKe)
                .add("type", type)
                .add("port", port)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OdpActivity.this, "Gagal simpan ODP: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String status = obj.getString("status");
                        String message = obj.getString("message");

                        Toast.makeText(OdpActivity.this, status.equalsIgnoreCase("success") ? "Berhasil: " + message : "Gagal: " + message, Toast.LENGTH_SHORT).show();

                        // Refresh data ODP
                        tampilkanODP();

                    } catch (Exception e) {
                        Toast.makeText(OdpActivity.this, "Respon tidak valid", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void showDialogTambahJalur() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Jalur Baru");

        final EditText input = new EditText(this);
        input.setHint("Masukkan Nama Jalur");
        input.setPadding(20, 20, 20, 20);

        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String namaJalur = input.getText().toString().trim();

            if (namaJalur.isEmpty()) {
                Toast.makeText(this, "Nama jalur tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            kirimJalurBaru(namaJalur);
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void kirimJalurBaru(String namaJalur) {

        RequestBody formBody = new FormBody.Builder()
                .add("api", "tambah_jalur")
                .add("user", iduserr) // ganti dengan user login jika perlu
                .add("nama_jalur", namaJalur)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OdpActivity.this, "Gagal tambah jalur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String status = obj.getString("status");
                        String message = obj.getString("message");

                        if (status.equalsIgnoreCase("success")) {
                            Toast.makeText(OdpActivity.this, "Jalur berhasil ditambah", Toast.LENGTH_SHORT).show();
                            loadSpinnerjalur(sjalur); // refresh spinner jalur
                        } else {
                            Toast.makeText(OdpActivity.this, "Gagal: " + message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(OdpActivity.this, "Respon tidak valid", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}

package com.wazzgroup.penagihanwifi.halamanpelanggan.mywifi;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.RouteInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.InetAddress;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.halamanpelanggan.PelangganActivity;
import com.wazzgroup.penagihanwifi.halamanpelanggan.pay.PayOnline;
import com.wazzgroup.penagihanwifi.halamanpelanggan.shop.PelangganPembelianActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PelangganHostspotActivity extends AppCompatActivity {
    private TextView txtSsid, txtSignal, txtGateway, txtPing, txtIsp;
    private Button btnOpenWeb;
    private String gatewayIp;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private final Handler handler = new Handler();
    private OkHttpClient client = new OkHttpClient();
    String url = GlobalHelper.BASE_URL;
    private TableLayout tableLayout;
    private TextView none,textView52;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pelanggan_hostspot);
         tableLayout = findViewById(R.id.tableLayout);
        none = findViewById(R.id.textView51);
        textView52 = findViewById(R.id.textView52);

        txtSsid = findViewById(R.id.txtSsid);
        txtSignal = findViewById(R.id.txtSignal);
        btnOpenWeb = findViewById(R.id.btnOpenWeb);
        btnOpenWeb.setEnabled(false);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> {
            Intent intent = new Intent(this, PelangganActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                updateWifiInfo();
            }
        } else {
            updateWifiInfo();
        }

        btnOpenWeb.setOnClickListener(v -> {
            if (gatewayIp != null && !gatewayIp.isEmpty()) {
                Intent intent = new Intent(PelangganHostspotActivity.this, PasswordActivity.class);
                intent.putExtra("gateway_ip", gatewayIp);
                startActivity(intent);
            }
        });

        handler.postDelayed(updateRunnable, 3000);
        carisn();


    }
    private void carisn(){
        String iduserr = GlobalHelper.getIdPelanggan(this);
        RequestBody formBody = new FormBody.Builder()

                .add("api", "ceksnpelanggan")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {

                runOnUiThread(() -> Toast.makeText(PelangganHostspotActivity.this, "Koneksi gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(result);
                        if (obj.getString("status").equals("success")) {
                            String sn = obj.getString("sn");
                            acs(sn);

                        }


                    } catch (JSONException e) {
                        Toast.makeText(PelangganHostspotActivity.this, "Format respon salah", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    public void acs(String sn) {
        String iduserr = GlobalHelper.getIdPelanggan(this);
        String url = GlobalHelper.BASE_URL;
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("api", "carisn")
                .add("user", iduserr)
                .add("sn", sn)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PelangganHostspotActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);

                        if (obj.optString("status").equals("success")) {

                          JSONArray terkoneksiArray = obj.optJSONArray("terkoneksi");


                            // Bersihkan tabel biar tidak dobel
                            tableLayout.removeAllViews();

                            // Header tabel
                            TableRow header = new TableRow(PelangganHostspotActivity.this);
                            TextView col1 = new TextView(PelangganHostspotActivity.this);
                            col1.setText("Nama");
                            col1.setPadding(20, 20, 20, 20);
                            col1.setTextColor(Color.BLACK); // warna hitam

                            TextView col2 = new TextView(PelangganHostspotActivity.this);
                            col2.setText("IP");
                            col2.setPadding(20, 20, 20, 20);
                            col2.setTextColor(Color.BLACK); // warna hitam

                            header.addView(col1);
                            header.addView(col2);
                            tableLayout.addView(header);

                            // Isi tabel dari array terkoneksi
                            if (terkoneksiArray != null) {
                                for (int i = 0; i < terkoneksiArray.length(); i++) {
                                    JSONObject item = terkoneksiArray.getJSONObject(i);

                                    String nama = item.optString("nama", "");
                                    String ip = item.optString("ip", "");

                                    // kalau nama kosong/null → set default
                                    if (nama == null || nama.trim().isEmpty()) {
                                        nama = "Perangkat tidak diketahui";
                                    }

                                    TableRow row = new TableRow(PelangganHostspotActivity.this);

                                    TextView txtNama = new TextView(PelangganHostspotActivity.this);
                                    txtNama.setText(nama);
                                    txtNama.setPadding(20, 20, 20, 20);

                                    TextView txtIp = new TextView(PelangganHostspotActivity.this);
                                    txtIp.setText(ip);
                                    txtIp.setPadding(20, 20, 20, 20);

                                    row.addView(txtNama);
                                    row.addView(txtIp);

                                    tableLayout.addView(row);
                                }
                            }

                        } else {
                            tableLayout.setVisibility(View.GONE);
                            textView52.setVisibility(View.GONE);
                            none.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(PelangganHostspotActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateWifiInfo();
        }
    }

    private void updateWifiInfo() {
        if (wifiManager == null || connectivityManager == null) {
            txtSsid.setText("WiFi tidak tersedia");
            return;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int rssi = wifiInfo.getRssi();
        String ssid = wifiInfo.getSSID().replace("\"", "");
        int signalLevel = WifiManager.calculateSignalLevel(rssi, 5);
        String signalText = getSignalStrengthText(signalLevel);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                gatewayIp = getGatewayIp(connectivityManager, network);
            } else {
                gatewayIp = null;
            }
        } else {
            gatewayIp = getLegacyGatewayIp();
        }

        txtSsid.setText( ssid);
        txtSignal.setText( signalText + " (" + rssi + " dBm)");
        //txtGateway.setText("Gateway: " + (gatewayIp != null ? gatewayIp : "Tidak ditemukan"));

//        new Thread(() -> {
//            boolean pingResult = pingTest("173.1.1.1");
//            runOnUiThread(() -> txtPing.setText(pingResult ? "Ping: Sukses ✅" : "Ping: Gagal ❌"));
//        }).start();

        btnOpenWeb.setEnabled(gatewayIp != null && !gatewayIp.isEmpty());
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateWifiInfo();
            handler.postDelayed(this, 3000);
        }
    };

    private String getGatewayIp(ConnectivityManager connectivityManager, Network network) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
            if (linkProperties != null) {
                for (RouteInfo route : linkProperties.getRoutes()) {
                    InetAddress gateway = route.getGateway();
                    if (gateway != null && gateway.isSiteLocalAddress()) {
                        return gateway.getHostAddress();
                    }
                }
            }
        }
        return "Tidak dapat mengambil Gateway";
    }

    private String getLegacyGatewayIp() {
        int ip = wifiManager.getDhcpInfo().gateway;
        return String.format("%d.%d.%d.%d",
                (ip & 0xFF), (ip >> 8 & 0xFF),
                (ip >> 16 & 0xFF), (ip >> 24 & 0xFF));
    }

    private String getSignalStrengthText(int level) {
        switch (level) {
            case 4:
            case 3:
                return "Sinyal Bagus 👍";
            case 2:
                return "Sinyal Sedang ⚠️";
            default:
                return "Sinyal Lemah ❌";
        }
    }

    private boolean pingTest(String ipAddress) {
        try {
            return InetAddress.getByName(ipAddress).isReachable(2000);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
}

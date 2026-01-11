package com.wazzgroup.penagihanwifi.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.client.Pelanggan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FragmentAcs extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_acs, container, false);

        TextView typeperangkat = view.findViewById(R.id.typeperangkat);
        TextView ponmode = view.findViewById(R.id.ponmode);
        TextView pon = view.findViewById(R.id.pon);
        TextView ssid = view.findViewById(R.id.ssid);
        TextView terkoenksi = view.findViewById(R.id.terkoenksi);
        TableLayout tableLayout = view.findViewById(R.id.tableLayout);
        TextView textView38 =view.findViewById(R.id.textView38);
        TextView textView47 =view.findViewById(R.id.textView47);
        TextView textView48 =view.findViewById(R.id.textView48);
        TextView textView49 =view.findViewById(R.id.textView49);
        ScrollView scrollView4 =view.findViewById(R.id.scrollView4);
        Bundle args = getArguments();
        if (args != null) {
            String json = args.getString("data_pelanggan");
            Pelanggan p = new Gson().fromJson(json, Pelanggan.class);

            String url = GlobalHelper.BASE_URL;
            OkHttpClient client = new OkHttpClient();
            String iduserr = GlobalHelper.getIdUser(requireContext());

            RequestBody formBody = new FormBody.Builder()
                    .add("api", "carisn")
                    .add("user", iduserr)
                    .add("sn", p.sn)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String json = response.body().string();

                    requireActivity().runOnUiThread(() -> {
                        try {
                            JSONObject obj = new JSONObject(json);

                            if (obj.optString("status").equals("success")) {


                                String redaman = obj.optString("redaman");
                                String perangkat = obj.optString("perangkat");
                                String typealat = obj.optString("type-alat");
                                String SSID = obj.optString("SSID");
                                String jumlah_terhubung = obj.optString("jumlah_terhubung");
                                JSONArray terkoneksiArray = obj.optJSONArray("terkoneksi");

                                typeperangkat.setText(perangkat.isEmpty() ? "Belum ditambahkan" : "Type modem " + perangkat);
                                ponmode.setText(typealat.isEmpty() ? "Belum ditambahkan" : "PON mode " + typealat);
                                pon.setText(redaman.isEmpty() ? "Belum ditambahkan" : "Redaman " + redaman);
                                ssid.setText(SSID.isEmpty() ? "Belum ditambahkan" : "SSID " + SSID);
                                terkoenksi.setText(jumlah_terhubung.isEmpty() ? "Belum ditambahkan" : "Perangkat Terhubung " + jumlah_terhubung);

                                // Bersihkan tabel biar tidak dobel
                                tableLayout.removeAllViews();

                                // Header tabel
                                TableRow header = new TableRow(requireContext());
                                TextView col1 = new TextView(requireContext());
                                col1.setText("Nama");
                                col1.setPadding(20, 20, 20, 20);

                                TextView col2 = new TextView(requireContext());
                                col2.setText("IP");
                                col2.setPadding(20, 20, 20, 20);

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

                                        TableRow row = new TableRow(requireContext());

                                        TextView txtNama = new TextView(requireContext());
                                        txtNama.setText(nama);
                                        txtNama.setPadding(20, 20, 20, 20);

                                        TextView txtIp = new TextView(requireContext());
                                        txtIp.setText(ip);
                                        txtIp.setPadding(20, 20, 20, 20);

                                        row.addView(txtNama);
                                        row.addView(txtIp);

                                        tableLayout.addView(row);
                                    }
                                }

                            }else{
                                typeperangkat.setVisibility(View.GONE);
                                ponmode.setVisibility(View.GONE);
                                pon.setVisibility(View.GONE);
                                ssid.setVisibility(View.GONE);
                                terkoenksi.setVisibility(View.GONE);
                                tableLayout.setVisibility(View.GONE);
                                textView38.setVisibility(View.GONE);
                                scrollView4.setVisibility(View.GONE);
                                textView47.setVisibility(View.VISIBLE);
                                textView48.setVisibility(View.VISIBLE);
                                textView49.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Gagal parsing data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        return view;
    }
}

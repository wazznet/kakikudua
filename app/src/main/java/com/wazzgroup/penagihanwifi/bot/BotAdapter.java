package com.wazzgroup.penagihanwifi.bot;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.acs.AcskuActivity;
import com.wazzgroup.penagihanwifi.helper.TokenAuthenticator;
import com.wazzgroup.penagihanwifi.helper.TokenInterceptor;

import org.json.JSONException;
import org.json.JSONObject;

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

public class BotAdapter extends ArrayAdapter<bot> {
    private List<bot> originalList;
    private List<bot> filteredList;
    Switch switch1;

    public BotAdapter(Context context, List<bot> list) {
        super(context, 0, list);
        originalList = new ArrayList<>(list);
        filteredList = list;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public bot getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        bot pelanggan = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bot, parent, false);
        }

        TextView txtNama = convertView.findViewById(R.id.txtNama);
        TextView txtTanggal = convertView.findViewById(R.id.txtTanggal);
        Switch switch1 = convertView.findViewById(R.id.switch1); // <<< INI YANG BELUM ADA

        txtNama.setText((pelanggan.nama == null || pelanggan.nama.isEmpty())
                ? "Belum di tambahkan"
                : (position + 1) + ". " + pelanggan.nama);

        txtTanggal.setText("Tagihan: " + pelanggan.tanggal);

        final boolean[] defaultState = {Objects.equals(pelanggan.autowa, "1")};

// set posisi awal switch
        switch1.setOnCheckedChangeListener(null); // sementara lepas listener
        switch1.setChecked(defaultState[0]);      // set default dari DB
        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != defaultState[0]) {
                if (isChecked) {
                    Toast.makeText(buttonView.getContext(), "Auto Chat "+pelanggan.nama+" dihidupkan", Toast.LENGTH_SHORT).show();
                    cek(pelanggan.id, "1");
                } else {
                    Toast.makeText(buttonView.getContext(), "Auto Chat "+pelanggan.nama+" dimatikan", Toast.LENGTH_SHORT).show();
                    cek(pelanggan.id, "0");
                }
                // update state terakhir
                defaultState[0] = isChecked;
            }
        });

        return convertView;
    }


    private void cek(String idpelanggan, String isi) {
        String url = GlobalHelper.BASE_URL;
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(getContext()))
                .authenticator(new TokenAuthenticator(getContext()))
                .build();

        String iduserr = GlobalHelper.getIdUser(getContext());

        RequestBody formBody = new FormBody.Builder()
                .add("api", "editauto")
                .add("user", iduserr)
                .add("idpelanggan", idpelanggan)
                .add("nilai", isi)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) getContext()).runOnUiThread(() ->
                        Toast.makeText(getContext(), "Gagal koneksi", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                ((Activity) getContext()).runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.optString("status").equals("success")) {
                            // sukses update
                            Toast.makeText(getContext(), "Update sukses", Toast.LENGTH_SHORT).show();

                        } else {

                            //String status =obj.optString("message");
                            Toast.makeText(getContext(), "Update gagal", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}


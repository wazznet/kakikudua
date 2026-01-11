package com.wazzgroup.penagihanwifi.mikrotik;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;

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

public class MikrotikAdapter extends ArrayAdapter<mikrotik> {
    private List<mikrotik> originalList;
    private List<mikrotik> filteredList;
    Switch switch1;

    public MikrotikAdapter(Context context, List<mikrotik> list) {
        super(context, 0, list);
        originalList = new ArrayList<>(list);
        filteredList = list;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public mikrotik getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mikrotik pelanggan = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mikrotik, parent, false);
        }

        TextView txtNama = convertView.findViewById(R.id.txtNama);
        TextView txtTanggal = convertView.findViewById(R.id.txtTanggal);
        Switch switch1 = convertView.findViewById(R.id.switch1); // <<< INI YANG BELUM ADA

        txtNama.setText((pelanggan.nama == null || pelanggan.nama.isEmpty())
                ? "Belum di tambahkan"
                : (position + 1) + ". " + pelanggan.nama);



        return convertView;
    }


    

}


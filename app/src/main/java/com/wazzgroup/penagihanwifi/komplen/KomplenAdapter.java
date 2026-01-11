package com.wazzgroup.penagihanwifi.komplen;



import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.wazzgroup.penagihanwifi.R;

import java.util.ArrayList;

public class KomplenAdapter extends ArrayAdapter<KolmplenPelanggan> {

    private ArrayList<KolmplenPelanggan> dataAsli;

    public KomplenAdapter(Context context, ArrayList<KolmplenPelanggan> data) {
        super(context, 0, data);
        this.dataAsli = new ArrayList<>(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        KolmplenPelanggan t = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_komplen, parent, false);
        }

        TextView nama = convertView.findViewById(R.id.txtNama);
        TextView tanggal = convertView.findViewById(R.id.txttanggal);
        TextView status = convertView.findViewById(R.id.txtstatus);
        TextView no = convertView.findViewById(R.id.nomer);
        ConstraintLayout layoutItem = (ConstraintLayout) convertView;

        // Set text
        nama.setText((position + 1) + ". " + t.nama);
        tanggal.setText("Tanggal : " + t.tanggal_dibuat + " | Area: " + t.nama_area);
        status.setText("Status: " + t.status);
        no.setText(t.hp);

        // Set background sesuai status
        switch (t.status.toLowerCase()) {
            case "belum":
                layoutItem.setBackgroundColor(Color.parseColor("#F44336")); // Merah
                break;
            case "proses":
                layoutItem.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
                break;
            case "selesai":
                layoutItem.setBackgroundColor(Color.parseColor("#FFFFFF")); // Putih
                break;
            default:
                layoutItem.setBackgroundColor(Color.parseColor("#BADCDBDB")); // Default abu-abu
                break;
        }

        return convertView;
    }


    public void filter(String text) {
        text = text.toLowerCase();
        clear();
        if (text.isEmpty()) {
            addAll(dataAsli);
        } else {
            for (KolmplenPelanggan t : dataAsli) {
                if (t.nama.toLowerCase().contains(text) ||
                        t.nama_area.toLowerCase().contains(text)) {
                    add(t);
                }
            }
        }
        notifyDataSetChanged();
    }
}


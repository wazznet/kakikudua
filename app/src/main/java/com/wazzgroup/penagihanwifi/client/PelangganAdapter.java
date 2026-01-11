package com.wazzgroup.penagihanwifi.client;

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
import java.util.List;
import java.util.Objects;

public class PelangganAdapter extends ArrayAdapter<Pelanggan> {
    private List<Pelanggan> originalList;
    private List<Pelanggan> filteredList;

    public PelangganAdapter(Context context, List<Pelanggan> list) {
        super(context, 0, list);
        originalList = new ArrayList<>(list);
        filteredList = list;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Pelanggan getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Pelanggan pelanggan = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_pelanggan, parent, false);
        }

if(Objects.equals(pelanggan.status, "1")){
    ConstraintLayout layout = convertView.findViewById(R.id.mainLayout);
    layout.setBackgroundColor(Color.parseColor("#BAECEDEF")); // contoh: merah
}else{
    ConstraintLayout layout = convertView.findViewById(R.id.mainLayout);
    layout.setBackgroundColor(Color.parseColor("#FF0000")); // contoh: merah
}
        TextView txtNama = convertView.findViewById(R.id.txtNama);
        TextView txtHp = convertView.findViewById(R.id.txtHp);
        TextView txtTanggal = convertView.findViewById(R.id.txtTanggal);
        TextView txtsecreet = convertView.findViewById(R.id.txtsecreet);
        txtNama.setText((pelanggan.nama == null || pelanggan.nama.isEmpty()) ? "Belum di tambahkan" : (position+1) + ". " + pelanggan.nama);
        txtHp.setText((pelanggan.hp == null || pelanggan.hp.isEmpty()) ? "Belum di tambahkan" : ("HP: " + pelanggan.hp));
        txtTanggal.setText("Tagihan: " + pelanggan.tanggal);
        txtsecreet.setText((pelanggan.secreet == null || pelanggan.secreet.isEmpty()) ? "Belum di tambahkan" : ("Secreet: " + pelanggan.secreet));

        return convertView;
    }

    public void filter(String text) {
        text = text.toLowerCase();
        filteredList.clear();

        if (text.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            for (Pelanggan p : originalList) {
                if (p.nama.toLowerCase().contains(text) ||
                        p.hp.toLowerCase().contains(text) ||
                        p.id.toLowerCase().contains(text) ||
                        p.terhubung_ke.toLowerCase().contains(text)) {
                    filteredList.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }
}


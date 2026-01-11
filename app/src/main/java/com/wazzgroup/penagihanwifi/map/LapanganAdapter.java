package com.wazzgroup.penagihanwifi.map;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.client.Pelanggan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LapanganAdapter extends ArrayAdapter<lapangan> {
    private List<lapangan> originalList;
    private List<lapangan> filteredList;

    public LapanganAdapter(Context context, List<lapangan> list) {
        super(context, 0, list);
        originalList = new ArrayList<>(list);
        filteredList = list;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public lapangan getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        lapangan lapangan = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_jalur, parent, false);
        }

    ConstraintLayout layout = convertView.findViewById(R.id.mainLayout);
    layout.setBackgroundColor(Color.parseColor("#BAECEDEF")); // contoh: merah

        TextView txtNama = convertView.findViewById(R.id.txtNama);
        TextView txtHp = convertView.findViewById(R.id.txtHp);
        txtNama.setText((lapangan.nama == null || lapangan.nama.isEmpty()) ? "Belum di tambahkan" : (position+1) + ". " + lapangan.nama+" (" + lapangan.id+")");

        if(Objects.equals(lapangan.pan, "pan")){
            txtHp.setText("");

        }else {
            if (lapangan.banyak_clinet == null || lapangan.banyak_clinet.isEmpty()) {
                txtHp.setText((lapangan.banyak_odp == null || lapangan.banyak_odp.isEmpty()) ? "Belum di tambahkan" : ("odp/odc: " + lapangan.banyak_odp));

            } else {
                txtHp.setText((lapangan.banyak_clinet == null || lapangan.banyak_clinet.isEmpty()) ? "0" : ("customers: " + lapangan.banyak_clinet));

            }
        }
        return convertView;
    }


}


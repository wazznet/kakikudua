package com.wazzgroup.penagihanwifi.halamanpelanggan.tagihan;



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

public class TagihanPelangganAdapter extends ArrayAdapter<TagihanPelanggan> {

    private ArrayList<TagihanPelanggan> dataAsli;

    public TagihanPelangganAdapter(Context context, ArrayList<TagihanPelanggan> data) {
        super(context, 0, data);
        this.dataAsli = new ArrayList<>(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TagihanPelanggan t = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_tagihan_pelanggan, parent, false);
        }

        TextView nama = convertView.findViewById(R.id.txtNama);
        TextView paket = convertView.findViewById(R.id.txtPaket);
        TextView total = convertView.findViewById(R.id.nomer);
        ConstraintLayout layoutItem = (ConstraintLayout) convertView;

        nama.setText((position + 1) + "."+ t.nama);
        paket.setText("Tanggal penagihan: " + t.tanggalpenagihan + " | tagihan bulan : " + t.bulan);
        total.setText("Tagihan: Rp" + t.jumlah+" { "+t.status+" )" );
        switch (t.status.toLowerCase()) {
            case "belum":
                layoutItem.setBackgroundColor(Color.parseColor("#F44336")); // Merah
                break;
            case "lunas":
                layoutItem.setBackgroundColor(Color.parseColor("#BADCDBDB")); // Orange
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
            for (TagihanPelanggan t : dataAsli) {
                if (t.nama.toLowerCase().contains(text) ||
                        t.nama_area.toLowerCase().contains(text)) {
                    add(t);
                }
            }
        }
        notifyDataSetChanged();
    }
}


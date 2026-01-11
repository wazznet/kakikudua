package com.wazzgroup.penagihanwifi.pembukuan;



import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wazzgroup.penagihanwifi.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class PembukuanAdapter extends ArrayAdapter<PembukuanClass> {

    private ArrayList<PembukuanClass> dataAsli;

    public PembukuanAdapter(Context context, ArrayList<PembukuanClass> data) {
        super(context, 0, data);
        this.dataAsli = new ArrayList<>(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PembukuanClass t = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_pembukuan, parent, false);
        }

        TextView nama = convertView.findViewById(R.id.txtNama);
        TextView paket = convertView.findViewById(R.id.txtPaket);
        TextView no = convertView.findViewById(R.id.nomer);
        int dataInt = Integer.parseInt(t.data); // Ubah ke int
        NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));


        if(Objects.equals(t.type, "pengeluaran")){


            nama.setTextColor(Color.parseColor("#FF0000")); // merah
            no.setTextColor(Color.parseColor("#FF0000")); // merah
            String dataku = "Rp. -" + formatRupiah.format(dataInt);
            no.setText(dataku);

        }else{
            nama.setTextColor(Color.parseColor("#1048F2")); // merah
            no.setTextColor(Color.parseColor("#1048F2")); // merah
            String dataku = "Rp. " + formatRupiah.format(dataInt);
            no.setText(dataku);
        }
        nama.setText((position + 1) + ". "+ t.type+" - "+t.type_data);
        String ket = t.keterangan;
        if (ket.length() > 15) {
            ket = ket.substring(0, 15) + "...";
        }
        paket.setText("Tanggal: " + t.tanggal + "/"+ t.bulan + "/"+ t.tahun + " | : " + ket);


        return convertView;
    }

    public void filter(String text) {
        text = text.toLowerCase();
        clear();
        if (text.isEmpty()) {
            addAll(dataAsli);
        } else {
            for (PembukuanClass t : dataAsli) {
                if (t.nama.toLowerCase().contains(text) ||
                        t.nama_area.toLowerCase().contains(text)) {
                    add(t);
                }
            }
        }
        notifyDataSetChanged();
    }
}


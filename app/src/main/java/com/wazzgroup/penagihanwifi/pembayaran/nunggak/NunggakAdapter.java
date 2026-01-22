package com.wazzgroup.penagihanwifi.pembayaran.nunggak;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.pembayaran.tagihan.TagihanBelumLunas;

import java.util.ArrayList;

public class NunggakAdapter extends ArrayAdapter<TagihanBelumLunas> {

    private ArrayList<TagihanBelumLunas> dataAsli;

    public NunggakAdapter(Context context, ArrayList<TagihanBelumLunas> data) {
        super(context, 0, data);
        this.dataAsli = new ArrayList<>(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TagihanBelumLunas t = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_tagihan, parent, false);
        }

        TextView nama = convertView.findViewById(R.id.txtNama);
        TextView paket = convertView.findViewById(R.id.txtPaket);
        TextView total = convertView.findViewById(R.id.txtTotal);
        TextView no = convertView.findViewById(R.id.nomer);

        nama.setText((position + 1) + "."+ t.nama);
        paket.setText("Tanggal penagihan: " + t.tanggalpenagihan + " | Area: " + t.nama_area);
        total.setText("Tagihan: Rp" + t.totalTagihan + " (" + t.rincianBulan + ")");
        no.setText(t.hp);

        return convertView;
    }

    public void filter(String text) {
        text = text.toLowerCase();
        clear();
        if (text.isEmpty()) {
            addAll(dataAsli);
        } else {
            for (TagihanBelumLunas t : dataAsli) {
                if (t.nama.toLowerCase().contains(text) ||
                        t.nama_area.toLowerCase().contains(text)) {
                    add(t);
                }
            }
        }
        notifyDataSetChanged();
    }
}


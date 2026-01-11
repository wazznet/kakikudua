package com.wazzgroup.penagihanwifi.halamanpelanggan.shop;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wazzgroup.penagihanwifi.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RiwayatAdapter extends ArrayAdapter<ClashRiwayat> {

    private ArrayList<ClashRiwayat> dataAsli;

    public RiwayatAdapter(Context context, ArrayList<ClashRiwayat> data) {
        super(context, 0, data);
        this.dataAsli = new ArrayList<>(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ClashRiwayat t = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_riwayat, parent, false);
        }

        TextView nama = convertView.findViewById(R.id.txtNama);
        TextView harga = convertView.findViewById(R.id.harga);
        TextView statuspembayaran = convertView.findViewById(R.id.status_pembayaran);
        TextView statuslayanan = convertView.findViewById(R.id.status_layanan);

        nama.setText((position + 1) + "."+ t.service_name);
        int hargaInt = Integer.parseInt(t.harga); // Ubah ke int
        NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));
        String formatted = "Rp. " + formatRupiah.format(hargaInt);

        harga.setText(formatted);
        statuspembayaran.setText("status pembayaran : " + t.status_pembayaran);
        statuslayanan.setText("status pesanan : " + t.status);

        return convertView;
    }


}


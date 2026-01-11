package com.wazzgroup.penagihanwifi.halamanpelanggan.shop;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.halamanpelanggan.shop.ClashShop;

import java.util.ArrayList;
import java.text.NumberFormat;
import java.util.Locale;
public class ShopAdapter extends ArrayAdapter<ClashShop> {

    private ArrayList<ClashShop> dataAsli;

    public ShopAdapter(Context context, ArrayList<ClashShop> data) {
        super(context, 0, data);
        this.dataAsli = new ArrayList<>(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ClashShop t = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_shop, parent, false);
        }

        TextView nama = convertView.findViewById(R.id.txtNama);
        TextView harga = convertView.findViewById(R.id.harga);
        TextView typeitem = convertView.findViewById(R.id.typeitem);

        nama.setText((position + 1) + "."+ t.service_name);
        int hargaInt = Integer.parseInt(t.price); // Ubah ke int
        NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));
        String formatted = "Rp. " + formatRupiah.format(hargaInt);

        harga.setText(formatted);
        typeitem.setText(t.type );

        return convertView;
    }


}


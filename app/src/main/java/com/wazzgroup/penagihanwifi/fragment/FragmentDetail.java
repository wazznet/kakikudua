package com.wazzgroup.penagihanwifi.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.wazzgroup.penagihanwifi.client.Pelanggan;
import com.wazzgroup.penagihanwifi.R;

public class FragmentDetail extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        TextView namaText = view.findViewById(R.id.nama);
        TextView nomerhpText = view.findViewById(R.id.nomerhp);
        TextView editnoText = view.findViewById(R.id.editno);
        TextView paketspinnerText = view.findViewById(R.id.paketspinner);
        TextView arenaspinnerText = view.findViewById(R.id.arenaspinner);
        TextView editTanggalText = view.findViewById(R.id.editTanggal);
        TextView textRfidText = view.findViewById(R.id.textRfid);
        TextView koordinatText = view.findViewById(R.id.koordinat);
        ConstraintLayout btnPilihLokasi = view.findViewById(R.id.Layoutlokasi);

        btnPilihLokasi.setOnClickListener(v -> lihatlokasidimap());
        ConstraintLayout btnPilihOdp = view.findViewById(R.id.LayoutOdp);

        btnPilihOdp.setOnClickListener(v -> lihatlokasiodpdimap());

        Bundle args = getArguments();
        if (args != null) {
            String json = args.getString("data_pelanggan");
            Pelanggan p = new Gson().fromJson(json, Pelanggan.class);



            namaText.setText(p.nama);
            nomerhpText.setText((p.hp == null || p.hp.isEmpty()) ? "Belum di tambahkan" : p.hp);
            editnoText.setText((p.secreet == null || p.secreet.isEmpty()) ? "Belum di tambahkan" : p.secreet);
            paketspinnerText.setText((p.paket == null || p.paket.isEmpty()) ? "Belum di tambahkan" : p.paket);
            arenaspinnerText.setText((p.nama_area == null || p.nama_area.isEmpty()) ? "Belum di tambahkan" : p.nama_area);
            editTanggalText.setText(p.created_at);
            textRfidText.setText((p.rfid == null || p.rfid.isEmpty()) ? "Belum di tambahkan" : p.rfid);

            koordinatText.setText("ODP: "+ p.odp_nama+'-'+p.odp_id+'-'+p.odp_port);

        }

        return view;
    }
    private void lihatlokasidimap() {
        Bundle args = getArguments();
        if (args != null) {
            String json = args.getString("data_pelanggan");
            Pelanggan p = new Gson().fromJson(json, Pelanggan.class);

            String latitude = p.latitude;
            String longitude = p.longitude;

            if (latitude == null || longitude == null || latitude.isEmpty() || longitude.isEmpty()) {
                Toast.makeText(getContext(), "Lokasi pelanggan belum di tambahkan", Toast.LENGTH_SHORT).show();
                return;
            }

            String geoUri = "geo:0,0?q=" + latitude + "," + longitude + "(Lokasi Pelanggan)";
            Intent geoIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(geoUri));

            try {
                startActivity(geoIntent);
            } catch (Exception e) {
                String webUri = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
                Intent webIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(webUri));

                try {
                    startActivity(webIntent);
                } catch (Exception ex) {
                    Toast.makeText(getContext(), "Tidak bisa membuka lokasi di Maps atau Browser", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void lihatlokasiodpdimap() {
        Bundle args = getArguments();
        if (args != null) {
            String json = args.getString("data_pelanggan");
            Pelanggan p = new Gson().fromJson(json, Pelanggan.class);

            String latitude = p.odp_latitude;
            String longitude = p.odp_longitude;

            if (latitude == null || longitude == null || latitude.isEmpty() || longitude.isEmpty()) {
                Toast.makeText(getContext(), "Lokasi ODP belum di tambahkan", Toast.LENGTH_SHORT).show();
                return;
            }

            String geoUri = "geo:0,0?q=" + latitude + "," + longitude + "(Lokasi ODP)";
            Intent geoIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(geoUri));

            try {
                startActivity(geoIntent);
            } catch (Exception e) {
                String webUri = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
                Intent webIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(webUri));

                try {
                    startActivity(webIntent);
                } catch (Exception ex) {
                    Toast.makeText(getContext(), "Tidak bisa membuka lokasi di Maps atau Browser", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



}

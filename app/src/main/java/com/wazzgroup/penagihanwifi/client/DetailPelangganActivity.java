package com.wazzgroup.penagihanwifi.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.fragment.FragmentAcs;
import com.wazzgroup.penagihanwifi.fragment.FragmentDetail;
import com.wazzgroup.penagihanwifi.fragment.FragmentGangguan;
import com.wazzgroup.penagihanwifi.fragment.FragmentTagihan;

public class DetailPelangganActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_detail_pelanggan);

        TextView namaText = findViewById(R.id.namapelanggan);
        TextView noText = findViewById(R.id.nomerhppelanggan);

        String json = getIntent().getStringExtra("pelanggan");
        Pelanggan p = new Gson().fromJson(json, Pelanggan.class);

        namaText.setText(p.nama);
        noText.setText(p.hp);

        Button detail = findViewById(R.id.btndetail);
        Button tagihan = findViewById(R.id.btntagihan);
        Button gangguan = findViewById(R.id.btngangguan);
        ImageView kembali = findViewById(R.id.back);

        // Fragment default saat pertama kali dijalankan
        gantiFragment(new FragmentDetail());

        detail.setOnClickListener(v -> gantiFragment(new FragmentDetail()));
        tagihan.setOnClickListener(v -> gantiFragment(new FragmentTagihan()));
        gangguan.setOnClickListener(v -> gantiFragment(new FragmentAcs()));

        // Tombol kembali (tombol di layout)
        kembali.setOnClickListener(v -> kembaliKeList());
    }

    private void gantiFragment(Fragment fragment) {
        String json = getIntent().getStringExtra("pelanggan");

        Bundle bundle = new Bundle();
        bundle.putString("data_pelanggan", json);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Override tombol back di HP agar langsung ke ListPelangganActivity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        kembaliKeList();
    }

    // Fungsi kembali
    private void kembaliKeList() {
        Intent intent = new Intent(this, ClientActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

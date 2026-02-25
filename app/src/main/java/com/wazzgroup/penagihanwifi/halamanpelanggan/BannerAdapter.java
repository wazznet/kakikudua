package com.wazzgroup.penagihanwifi.halamanpelanggan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wazzgroup.penagihanwifi.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.VH> {

    private List<BannerModel> list;
    private Context context;

    public BannerAdapter(Context context, List<BannerModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_banner, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        BannerModel banner = list.get(position % list.size());
        h.img.setImageResource(banner.image);

        h.img.setOnClickListener(v -> {
            context.startActivity(new Intent(context, banner.target));
        });
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE; // infinite
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgBanner);
        }
    }
}

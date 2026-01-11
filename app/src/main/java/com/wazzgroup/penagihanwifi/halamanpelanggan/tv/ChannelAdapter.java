package com.wazzgroup.penagihanwifi.halamanpelanggan.tv;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.wazzgroup.penagihanwifi.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import com.bumptech.glide.Glide;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {

    Context context;
    List<Channel> list;

    public ChannelAdapter(Context context, List<Channel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Channel channel = list.get(position);
        holder.nama.setText(channel.nama);
        Glide.with(context).load(channel.logo).into(holder.logo);

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, PlayerActivity.class);
            i.putExtra("url", channel.url);
            i.putExtra("user_agent", channel.user_agent);
            i.putExtra("license_type", channel.license_type);
            i.putExtra("license_key", channel.license_key);
            i.putExtra("referrer", channel.referrer);
            i.putExtra("format", channel.format);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView logo;
        TextView nama;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            logo = itemView.findViewById(R.id.logo);
            nama = itemView.findViewById(R.id.nama);
        }
    }
}

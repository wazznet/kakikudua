package com.wazzgroup.penagihanwifi.settings;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wazzgroup.penagihanwifi.R;

import java.util.List;

public class AdapterInfo extends RecyclerView.Adapter<AdapterInfo.ViewHolder> {

    private List<ClassInfo> akunList;
    private OnItemClickListener listener;

    // 🔹 Listener interface
    public interface OnItemClickListener {
        void onItemClick(ClassInfo akun);
    }

    // 🔹 Constructor dengan listener
    public AdapterInfo(List<ClassInfo> akunList, OnItemClickListener listener) {
        this.akunList = akunList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdapterInfo.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new AdapterInfo.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterInfo.ViewHolder holder, int position) {
        ClassInfo area = akunList.get(position);

        holder.textView.setText((position + 1) + ". Versi: " + area.getversi()+ " | tanggal : " + area.gettanggal());
        holder.textView.setBackgroundResource(R.drawable.circular_overlay3);

        int marginInDp = 2;
        int marginTopInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                marginInDp,
                holder.itemView.getResources().getDisplayMetrics()
        );
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.textView.getLayoutParams();
        params.setMargins(0, marginTopInPx, 0, 0);
        holder.textView.setLayoutParams(params);

        // 🔹 Klik item -> panggil listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(area);
            }
        });
        holder.textView.setTextColor(Color.BLACK);

    }

    @Override
    public int getItemCount() {
        return akunList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}


//    private List<ClassInfo> areaList;
//
//    public AdapterInfo(List<ClassInfo> areaList) {
//        this.areaList = areaList;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(android.R.layout.simple_list_item_1, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @SuppressLint("SetTextI18n")
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        ClassInfo area = areaList.get(position);
//        // position mulai dari 0, jadi tambahkan +1 untuk nomor urut
//        int hargaku = area.getharga();
//        if(hargaku<=0){
//
//            holder.textView.setText((position + 1) + ". " + area.getNama() + " | Total Pelanggan : " + area.getJumlah());
//            holder.textView.setBackgroundResource(R.drawable.circular_overlay3);
//            int marginInDp = 2;
//            int marginTopInPx = (int) TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP,
//                    marginInDp,
//                    holder.itemView.getResources().getDisplayMetrics()
//            );
//
//            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.textView.getLayoutParams();
//            params.setMargins(0, marginTopInPx, 0, 0); // left, top, right, bottom
//            holder.textView.setLayoutParams(params);
//        }else{
//            int hargafix = hargaku;
//            holder.textView.setText((position + 1) + ". " + area.getNama() +" | Rp."+hargafix+ " | Total Pelanggan : " + area.getJumlah());
//            holder.textView.setBackgroundResource(R.drawable.circular_overlay3);
//            int marginInDp = 2;
//            int marginTopInPx = (int) TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP,
//                    marginInDp,
//                    holder.itemView.getResources().getDisplayMetrics()
//            );
//
//            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.textView.getLayoutParams();
//            params.setMargins(0, marginTopInPx, 0, 0); // left, top, right, bottom
//            holder.textView.setLayoutParams(params);
//        }
//        }
//
//    @Override
//    public int getItemCount() {
//        return areaList.size();
//    }
//
//    static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView textView;
//        ViewHolder(View itemView) {
//            super(itemView);
//            textView = itemView.findViewById(android.R.id.text1);
//        }
//    }
//}

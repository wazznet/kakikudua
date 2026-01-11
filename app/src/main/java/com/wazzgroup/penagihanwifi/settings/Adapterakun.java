
package com.wazzgroup.penagihanwifi.settings;
import android.annotation.SuppressLint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wazzgroup.penagihanwifi.R;

import java.util.List;
public class Adapterakun extends RecyclerView.Adapter<Adapterakun.ViewHolder> {
    private List<ClassAkun> akunList;
    private OnItemClickListener listener;

    // 🔹 Listener interface
    public interface OnItemClickListener {
        void onItemClick(ClassAkun akun);
    }

    // 🔹 Constructor dengan listener
    public Adapterakun(List<ClassAkun> akunList, OnItemClickListener listener) {
        this.akunList = akunList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassAkun area = akunList.get(position);

        String rawNamaArea = area.getnama_area();
        String namaarea;
        if (rawNamaArea == null || rawNamaArea.trim().isEmpty() || rawNamaArea.equalsIgnoreCase("null")) {
            namaarea = "all area";
        } else {
            namaarea = rawNamaArea;
        }

        holder.textView.setText((position + 1) + ". " + area.getnama()
                + " | Sebagai : " + area.getakses()
                + " | area : " + namaarea);

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

//public class Adapterakun extends RecyclerView.Adapter<Adapterakun.ViewHolder> {
//    private List<ClassAkun> akunList;
//
//    public Adapterakun(List<ClassAkun> akunList) {
//        this.akunList = akunList;
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
//        ClassAkun area = akunList.get(position);
//        // position mulai dari 0, jadi tambahkan +1 untuk nomor urut
//        String rawNamaArea = area.getnama_area();
//        String namaarea;
//
//        if (rawNamaArea == null || rawNamaArea.trim().isEmpty() || rawNamaArea.equalsIgnoreCase("null")) {
//            namaarea = "all area";
//        } else {
//            namaarea = rawNamaArea;
//        }
//
//        holder.textView.setText((position + 1) + ". " + area.getnama() + " | Sebagai : " + area.getakses()+ " | area : " + namaarea);
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
//
//        }
//
//    @Override
//    public int getItemCount() {
//        return akunList.size();
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

package com.wazzgroup.penagihanwifi.halamanpelanggan.quran;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.halamanpelanggan.PelangganActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListSuratActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<JSONObject> listSurat = new ArrayList<>();
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_surat);

        ImageView kembali = findViewById(R.id.back2);
        kembali.setOnClickListener(v -> kembaliKeList());
        recyclerView = findViewById(R.id.recyclerSurat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getDataSurat();
    }

    private void getDataSurat() {

        Request request = new Request.Builder()
                .url("https://equran.id/api/v2/surat")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String res = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(res);
                        JSONArray data = obj.getJSONArray("data");

                        for (int i = 0; i < data.length(); i++) {
                            listSurat.add(data.getJSONObject(i));
                        }

                        recyclerView.setAdapter(new SuratAdapter());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    class SuratAdapter extends RecyclerView.Adapter<SuratAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNamaLatin, txtInfo;

            ViewHolder(View v) {
                super(v);
                txtNamaLatin = v.findViewById(R.id.txtNamaLatin);
                txtInfo = v.findViewById(R.id.txtInfo);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_surat, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                JSONObject surat = listSurat.get(position);

                holder.txtNamaLatin.setText(
                        surat.getInt("nomor") + ". " +
                                surat.getString("namaLatin"));

                holder.txtInfo.setText(
                        surat.getString("arti") + " • " +
                                surat.getInt("jumlahAyat") + " Ayat");

                holder.itemView.setOnClickListener(v -> {
                    Intent i = new Intent(ListSuratActivity.this,
                            DetailSuratActivity.class);
                    try {
                        i.putExtra("nomor", surat.getInt("nomor"));
                        startActivity(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return listSurat.size();
        }
    }
    private void kembaliKeList() {
        Intent intent = new Intent(this, PelangganActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

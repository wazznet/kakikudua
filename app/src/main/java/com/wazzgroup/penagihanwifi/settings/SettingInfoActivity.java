package com.wazzgroup.penagihanwifi.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingInfoActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdapterInfo adapter;
    private List<ClassInfo> versiList = new ArrayList<>();
    private String url = GlobalHelper.BASE_URL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting_info);
        recyclerView = findViewById(R.id.recyclerlog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //adapter = new AdapterInfo(versiList);
        adapter = new AdapterInfo(versiList, versi -> {
            showVersiDialog(versiList);
        });

        recyclerView.setAdapter(adapter);
        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> {
            Intent intent = new Intent(SettingInfoActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        fetchArea();
    }

    private void showVersiDialog(List<ClassInfo> versiList) {
        // Buat String[] untuk ditampilkan di AlertDialog
        String[] items = new String[versiList.size()];
        for (int i = 0; i < versiList.size(); i++) {
            ClassInfo v = versiList.get(i);
            items[i] = "Versi: " + v.getversi() + "\nTanggal: " + v.gettanggal() +
                    "\n" + v.getlist_update();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Riwayat Update");
        builder.setItems(items, (dialog, which) -> {
            ClassInfo pilih = versiList.get(which);
            Toast.makeText(this,
                    "Dipilih: " + pilih.getversi(),
                    Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Tutup", null);
        builder.show();
    }

    private void fetchArea() {
        OkHttpClient client = new OkHttpClient();


        String iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "log_update")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Request gagal: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray dataArray = jsonObject.getJSONArray("data");

                            versiList.clear();
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject obj = dataArray.getJSONObject(i);
                                int id = obj.getInt("id");
                                String versi = obj.getString("versi");
                                String list_update = obj.getString("list_update");
                                String tanggal = obj.getString("tanggal");

                                versiList.add(new ClassInfo(id, versi, list_update,tanggal));
                            }

                            runOnUiThread(() -> adapter.notifyDataSetChanged());
                        }

                    } catch (Exception e) {
                        Log.e("API_ERROR", "Parsing error: " + e.getMessage());
                    }
                }
            }
        });
    }
}
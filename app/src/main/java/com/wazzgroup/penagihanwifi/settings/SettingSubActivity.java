package com.wazzgroup.penagihanwifi.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingSubActivity extends AppCompatActivity {
    private RecyclerView recyclerView,recyclerView2;
    private Adaptersub Adaptersub;
    private Adapterakun Adapterakun;
    private List<Class2> areaList = new ArrayList<>();
    private List<ClassAkun> akunList = new ArrayList<>();
    private String url = GlobalHelper.BASE_URL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting_sub);
        recyclerView = findViewById(R.id.recyclersub);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 pasang adapter dengan listener
        Adapterakun = new Adapterakun(akunList, area -> {

            tampilkanDialog(area);
        });

        recyclerView.setAdapter(Adapterakun);
//        recyclerView = findViewById(R.id.recyclersub);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        Adapterakun = new Adapterakun(akunList);
//
//        recyclerView.setAdapter(Adapterakun);

        recyclerView2 = findViewById(R.id.recyclerlog);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        Adaptersub = new Adaptersub(areaList);
        recyclerView2.setAdapter(Adaptersub);

        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> {
            Intent intent = new Intent(SettingSubActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        Button btnTambahArea = findViewById(R.id.button);
        btnTambahArea.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Tambah User Baru");

            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_tambah_user, null);
            builder.setView(dialogView);

            EditText inputNama = dialogView.findViewById(R.id.inputNama);
            EditText inputUsername = dialogView.findViewById(R.id.inputUsername);
            EditText inputPassword = dialogView.findViewById(R.id.inputPassword);
            EditText nohp = dialogView.findViewById(R.id.nohp);
            RadioGroup radioRole = dialogView.findViewById(R.id.radioRole);
            RadioGroup radioArea = dialogView.findViewById(R.id.radioArea);

            // 🔹 Ambil daftar area dari API
            // 🔹 Ambil daftar area dari API
            new Thread(() -> {
                try {
                    String iduserr = GlobalHelper.getIdUser(this);
                    OkHttpClient client = new OkHttpClient();
                    RequestBody formBody = new FormBody.Builder()
                            .add("api", "area")
                            .add("user", iduserr) // <-- id_user aktif
                            .build();

                    Request request = new Request.Builder()
                            .url(url) // base url server kamu
                            .post(formBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);
                        if (json.getString("status").equals("success")) {
                            JSONArray data = json.getJSONArray("data");

                            runOnUiThread(() -> {
                                radioArea.removeAllViews();

                                // 🔹 Tambahkan manual opsi "All Area" paling atas
                                RadioButton rbAll = new RadioButton(this);
                                rbAll.setText("All Area");
                                rbAll.setTag("0"); // id 0 untuk All Area
                                radioArea.addView(rbAll);

                                // 🔹 Tambahkan area hasil API
                                for (int i = 0; i < data.length(); i++) {
                                    try {
                                        JSONObject area = data.getJSONObject(i);
                                        String id = area.getString("id");
                                        String nama = area.getString("nama");
                                        int jumlah = area.getInt("jumlah");

                                        RadioButton rb = new RadioButton(this);
                                        rb.setText(nama + " (" + jumlah + " pelanggan)");
                                        rb.setTag(id); // simpan id_area ke tag
                                        radioArea.addView(rb);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                // 🔹 Default centang All Area biar aman
                                rbAll.setChecked(true);
                                // 🔹 Setelah radioArea sudah diisi (di dalam runOnUiThread)
                                radioArea.setOnCheckedChangeListener((group, checkedId) -> {
                                    if (checkedId != -1) {
                                        RadioButton selected = dialogView.findViewById(checkedId);
                                        String idSelected = selected.getTag().toString();
                                        String namaSelected = selected.getText().toString();

                                        // Log ke Logcat
                                        Log.d("TambahUser", "User memilih area: " + namaSelected + " (ID: " + idSelected + ")");

                                        // Bisa juga kasih Toast supaya kelihatan langsung
                                        Toast.makeText(this, "Pilih Area: " + namaSelected, Toast.LENGTH_SHORT).show();
                                    }
                                });

                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();


            builder.setPositiveButton("Simpan", null); // ⬅️ penting: null dulu, biar tidak auto-dismiss
            builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dlg -> {
                Button btnSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnSimpan.setOnClickListener(v1 -> {
                    String nama = inputNama.getText().toString().trim();
                    String username = inputUsername.getText().toString().trim();
                    String password = inputPassword.getText().toString().trim();
                    String nohpnya = nohp.getText().toString().trim();

                    int selectedRoleId = radioRole.getCheckedRadioButtonId();
                    String role = "";
                    if (selectedRoleId != -1) {
                        RadioButton selectedRole = dialogView.findViewById(selectedRoleId);
                        role = selectedRole.getText().toString();
                    }

                    int selectedAreaId = radioArea.getCheckedRadioButtonId();
                    String idArea = "";
                    if (selectedAreaId != -1) {
                        RadioButton selectedArea = dialogView.findViewById(selectedAreaId);
                        idArea = selectedArea.getTag().toString();
                    }

                    if (nama.isEmpty() || username.isEmpty() || password.isEmpty()
                            || nohpnya.isEmpty() || role.isEmpty() || idArea.isEmpty()) {
                        Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                        return; // ❌ jangan dismiss
                    }

                    String post ="tambah_user";

                    int idtarget = 0;
                    // kirim API
                    tambahUserKeApi(post, idtarget, nama, username, password, role, idArea, nohpnya, new ApiCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "User berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                            akun();
                            dialog.dismiss(); // ✅ baru tutup kalau sukses
                        }

                        @Override
                        public void onError(String errorMsg) {
                            Toast.makeText(getApplicationContext(), "Gagal tambah User: " + errorMsg, Toast.LENGTH_LONG).show();
                            // ❌ dialog tetap terbuka
                        }
                    });
                });
            });
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        });


        fetchLog();
        akun();
    }
    private void tampilkanDialog(ClassAkun area) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("edit User ");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_tambah_user, null);
        builder.setView(dialogView);

        EditText inputNama = dialogView.findViewById(R.id.inputNama);
        EditText inputUsername = dialogView.findViewById(R.id.inputUsername);
        inputUsername.setFocusable(false);
        inputUsername.setClickable(false);
        EditText inputPassword = dialogView.findViewById(R.id.inputPassword);
        EditText nohp = dialogView.findViewById(R.id.nohp);
        RadioGroup radioRole = dialogView.findViewById(R.id.radioRole);
        RadioButton radioTeknisi = dialogView.findViewById(R.id.radioTeknisi);
        RadioButton radioMarketing = dialogView.findViewById(R.id.radioMarketing);
        RadioButton radioReseller = dialogView.findViewById(R.id.radioReseller);

        RadioGroup radioArea = dialogView.findViewById(R.id.radioArea);
        inputNama.setText(area.getnama());
        inputUsername.setText(area.getusername());
        nohp.setText(area.getnohp());
        if(Objects.equals(area.getakses(), "teknisi")){
            radioRole.check(radioTeknisi.getId());
        }
        if(Objects.equals(area.getakses(), "marketing")){
            radioRole.check(radioMarketing.getId());
        }
        if(Objects.equals(area.getakses(), "reseller")){
            radioRole.check(radioReseller.getId());
        }
        //inputPassword.setVisibility(View.GONE);
        // 🔹 Ambil daftar area dari API
        // 🔹 Ambil daftar area dari API
        new Thread(() -> {
            try {
                String iduserr = GlobalHelper.getIdUser(this);
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormBody.Builder()
                        .add("api", "area")
                        .add("user", iduserr) // <-- id_user aktif
                        .build();

                Request request = new Request.Builder()
                        .url(url) // base url server kamu
                        .post(formBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    if (json.getString("status").equals("success")) {
                        JSONArray data = json.getJSONArray("data");

                        runOnUiThread(() -> {
                            radioArea.removeAllViews();

// 🔹 Tambahkan manual opsi "All Area" paling atas
                            RadioButton rbAll = new RadioButton(this);
                            rbAll.setText("All Area");
                            rbAll.setTag("0"); // Simpan 0 sebagai ID untuk All Area
                            radioArea.addView(rbAll);

// 🔹 Tambahkan area hasil API
                            for (int i = 0; i < data.length(); i++) {
                                try {
                                    JSONObject area2 = data.getJSONObject(i);
                                    String id = area2.getString("id");
                                    String nama = area2.getString("nama");
                                    int jumlah = area2.getInt("jumlah");

                                    RadioButton rb = new RadioButton(this);
                                    rb.setText(nama + " (" + jumlah + " pelanggan)");
                                    rb.setTag(id); // simpan id_area ke tag
                                    radioArea.addView(rb);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            // 🔹 Default centang All Area biar aman
                            int selectedAreaId = area.getarea();

                            for (int i = 0; i < radioArea.getChildCount(); i++) {
                                View v = radioArea.getChildAt(i);
                                if (v instanceof RadioButton) {
                                    RadioButton rb = (RadioButton) v;
                                    int tagId = Integer.parseInt(rb.getTag().toString());
                                    if (tagId == selectedAreaId) {
                                        rb.setChecked(true);
                                        break;
                                    }
                                }
                            }


                            // 🔹 Setelah radioArea sudah diisi (di dalam runOnUiThread)
//                            radioArea.setOnCheckedChangeListener((group, checkedId) -> {
//                                if (checkedId != -1) {
//                                    RadioButton selected = dialogView.findViewById(checkedId);
//                                    String idSelected = selected.getTag().toString();
//                                    String namaSelected = selected.getText().toString();
//
//                                    // Log ke Logcat
//                                    Log.d("TambahUser", "User memilih area: " + namaSelected + " (ID: " + idSelected + ")");
//
//                                    // Bisa juga kasih Toast supaya kelihatan langsung
//                                    Toast.makeText(this, "Pilih Area: " + namaSelected, Toast.LENGTH_SHORT).show();
//                                }
//                            });

                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();


        builder.setPositiveButton("Simpan", null); // ⬅️ penting: null dulu, biar tidak auto-dismiss
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            Button btnSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSimpan.setOnClickListener(v1 -> {
                String nama = inputNama.getText().toString().trim();
                String username = inputUsername.getText().toString().trim();
                String nohpnya = nohp.getText().toString().trim();

                String password = inputPassword.getText().toString().trim();

                int selectedRoleId = radioRole.getCheckedRadioButtonId();
                String role = "";
                if (selectedRoleId != -1) {
                    RadioButton selectedRole = dialogView.findViewById(selectedRoleId);
                    role = selectedRole.getText().toString();
                }

                int selectedAreaId = radioArea.getCheckedRadioButtonId();
                String idArea = "";
                if (selectedAreaId != -1) {
                    RadioButton selectedArea = dialogView.findViewById(selectedAreaId);
                    idArea = selectedArea.getTag().toString();
                }

                
                if (nama.isEmpty() || username.isEmpty() || password.isEmpty()
                        || nohpnya.isEmpty() || role.isEmpty() || idArea.isEmpty()) {
                    Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                    return; // ❌ jangan dismiss
                }

                //String password ="";
                String post ="edit_user";
                int idtarget = area.getId();
                // kirim API
                tambahUserKeApi(post,idtarget, nama, username, password, role, idArea, nohpnya, new ApiCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "User berhasil Diedit", Toast.LENGTH_SHORT).show();
                        akun();
                        dialog.dismiss(); // ✅ baru tutup kalau sukses
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Toast.makeText(getApplicationContext(), "Gagal Diedit User: " + errorMsg, Toast.LENGTH_LONG).show();
                        // ❌ dialog tetap terbuka
                    }
                });
            });
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    private void tambahUserKeApi(String post, int idtarget, String nama, String username, String password,
                                 String role, String idArea, String nohpnya, ApiCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", post)
                .add("id_user", iduserr)
                .add("id_target", String.valueOf(idtarget))
                .add("nama", nama)
                .add("username", username)
                .add("password", password)
                .add("role", role)
                .add("idArea", idArea)
                .add("nohp", nohpnya)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> callback.onError("Koneksi gagal: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(resp);
                        if (json.getString("status").equals("success")) {
                            callback.onSuccess();
                        } else {
                            String msg = json.optString("message", "Gagal tanpa detail");
                            callback.onError(msg);
                        }
                    } catch (Exception e) {
                        callback.onError("Parsing error: " + e.getMessage());
                    }
                });
            }
        });
    }

    interface ApiCallback {
        void onSuccess();
        void onError(String errorMsg);
    }
    private void akun() {
    OkHttpClient client = new OkHttpClient();


    String iduserr = GlobalHelper.getIdUser(this);
    RequestBody formBody = new FormBody.Builder()
            .add("api", "sub_akun")
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

                        akunList.clear();
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject obj = dataArray.getJSONObject(i);
                            int id = obj.getInt("id");
                            String nama = obj.getString("nama");
                            String akses = obj.getString("akses");
                            String username = obj.getString("username");
                            String nohp = obj.getString("nohp");
                            int area = obj.getInt("area");
                            String nama_area = obj.getString("nama_area");
                            akunList.add(new ClassAkun(id, nama, akses, username, nohp, area, nama_area));
                        }

                        runOnUiThread(() -> Adapterakun.notifyDataSetChanged());
                    }

                } catch (Exception e) {
                    Log.e("API_ERROR", "Parsing error: " + e.getMessage());
                }
            }
        }
    });
}
    private void fetchLog() {
        OkHttpClient client = new OkHttpClient();


        String iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "log_login")
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

                            areaList.clear();
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject obj = dataArray.getJSONObject(i);
                                int id = obj.getInt("id");
                                String perangkat = obj.getString("perangkat");
                                String status = obj.getString("status");
                                areaList.add(new Class2(id, perangkat, status));
                            }

                            runOnUiThread(() -> Adaptersub.notifyDataSetChanged());
                        }

                    } catch (Exception e) {
                        Log.e("API_ERROR", "Parsing error: " + e.getMessage());
                    }
                }
            }
        });
    }
}
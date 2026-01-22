package com.wazzgroup.penagihanwifi.pembukuan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PembukuanTambahActivity extends AppCompatActivity {
    String url = GlobalHelper.BASE_URL;
    private ListView listView;
    private ArrayList<PembukuanClass> listData = new ArrayList<>();
    private PembukuanAdapter adapter;
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 200;
    private Uri imageUri;
    private ImageView imgPreview; // untuk dialog
    private static final int REQUEST_PERMISSION = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pembukuan_tambah);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        listView = findViewById(R.id.listTagihan);
        ConstraintLayout btnTambah = findViewById(R.id.tombolrfid);
        btnTambah.setOnClickListener(v -> {
            showPembukuanDialog(); // panggil fungsi dialog
        });
        adapter = new PembukuanAdapter(this, listData);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            PembukuanClass data = listData.get(position);
            tampilkanDialogPilihan(data);
        });

        ImageView kembali = findViewById(R.id.backtolist);
        kembali.setOnClickListener(v -> {
            Intent intent = new Intent(PembukuanTambahActivity.this, PembukuanActivity.class);
            startActivity(intent);
        });
        ambilData();
    }
    private void tampilkanDialogPilihan(PembukuanClass p) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pembukuan_view, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.show();
        Button btnKembali = dialogView.findViewById(R.id.btnKembali);
        btnKembali.setOnClickListener(v -> dialog.dismiss());

        ImageView fotoView = dialogView.findViewById(R.id.imageView10);

// Default GONE
        fotoView.setVisibility(View.GONE);
        String iduserr = GlobalHelper.getIdUser(this);

        String fotoUrl = "https://fufufafa.wazzapp.my.id/view/"+iduserr+"/" + p.bukti;

        if (p.bukti != null && !p.bukti.trim().isEmpty()) {
            fotoView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(R.drawable.circular_overlay2)
                    .error(R.drawable.baseline_error_24)
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                GlideException e,
                                Object model,
                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                boolean isFirstResource) {
                            // Tetap GONE jika gagal
                            fotoView.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                android.graphics.drawable.Drawable resource,
                                Object model,
                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                com.bumptech.glide.load.DataSource dataSource,
                                boolean isFirstResource) {
                            // Tampilkan hanya kalau berhasil
                            fotoView.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(fotoView);
        }


        TextView type = dialogView.findViewById(R.id.type);
        type.setText("Type : "+p.type);
        TextView typedata = dialogView.findViewById(R.id.typedata);
        typedata.setText("Dari/Untuk : "+ p.type_data);
        TextView angggaran = dialogView.findViewById(R.id.angggaran);
        angggaran.setText("Rp "+ p.data);
        TextView tanggal = dialogView.findViewById(R.id.tanggal);
        tanggal.setText("tanggal : "+ p.tanggal+"/"+ p.bulan+"/"+ p.tahun+"");
        TextView keterangan = dialogView.findViewById(R.id.keterangan);
        keterangan.setText(p.keterangan );



    }
    private void showPembukuanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pembukuan, null);
        builder.setView(dialogView);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupJenis);
        RadioGroup radiotypedata = dialogView.findViewById(R.id.radioGroupTtpedata);
        EditText etAnggaran = dialogView.findViewById(R.id.etAnggaran);
        EditText etKeterangan = dialogView.findViewById(R.id.etKeterangan);
        ImageView btnPilihFoto = dialogView.findViewById(R.id.imgPreview);
        imgPreview = dialogView.findViewById(R.id.imgPreview);

        RadioButton bw = dialogView.findViewById(R.id.bw);
        RadioButton gaji = dialogView.findViewById(R.id.gaji);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadio2 = group.findViewById(checkedId);
            String jenis22 = selectedRadio2.getText().toString();

            if (jenis22.equalsIgnoreCase("Pemasukan")) {
                bw.setVisibility(View.GONE);
                gaji.setVisibility(View.GONE);
                android.util.Log.d("API_RESPONSE", "Respon dari server: " + jenis22);
            } else {
                bw.setVisibility(View.VISIBLE);
                gaji.setVisibility(View.VISIBLE);
                android.util.Log.d("API_RESPONSE", "Respon dari server: " + jenis22);
            }
        });
        builder.setPositiveButton("Simpan", null);
        builder.setNegativeButton("Batal", (d, w) -> d.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        btnPilihFoto.setOnClickListener(v -> {
            String[] options = {"Kamera", "Galeri"};
            AlertDialog.Builder pilih = new AlertDialog.Builder(this);
            pilih.setTitle("Pilih Foto")
                    .setItems(options, (dialog1, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, REQUEST_CAMERA);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, REQUEST_GALLERY);
                        }
                    }).show();
        });

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            int selectedId2 = radiotypedata.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Pilih jenis transaksi", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedId2 == -1) {
                Toast.makeText(this, "Pilih jenis transaksi", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton selectedRadio = dialogView.findViewById(selectedId);
            RadioButton selectedRadiodata = dialogView.findViewById(selectedId2);
            String jenis = selectedRadio.getText().toString();
            String jenis2 = selectedRadiodata.getText().toString();
            String anggaran = etAnggaran.getText().toString().trim();
            String keterangan = etKeterangan.getText().toString().trim();
            String namaId2 = getResources().getResourceEntryName(selectedId2);

            String namaId = getResources().getResourceEntryName(selectedId);



            if (anggaran.isEmpty() || keterangan.isEmpty()) {
                Toast.makeText(this, "Lengkapi semua data", Toast.LENGTH_SHORT).show();
                return;
            }
            //android.util.Log.d("API_RESPONSE", "Respon dari server: " + namaId);
           // android.util.Log.d("API_RESPONSE", "Respon dari server: " + namaId2);

            String iduserr = GlobalHelper.getIdUser(this);

            OkHttpClient client = new OkHttpClient();
            Request request;

            if (imageUri != null) {
                try {
                    // Baca file dari ContentResolver
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    byte[] fileBytes = getBytes(inputStream);

                    // Ambil nama file (fallback kalau null)
                    String fileName = getFileNameFromUri(imageUri);
                    if (fileName == null) {
                        fileName = "upload_" + System.currentTimeMillis() + ".jpg";
                    }

                    MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("api", "tambah_pembukuan")
                            .addFormDataPart("id_user", iduserr)
                            .addFormDataPart("jenis", namaId)
                            .addFormDataPart("jenis_data", namaId2)
                            .addFormDataPart("anggaran", anggaran)
                            .addFormDataPart("keterangan", keterangan)
                            .addFormDataPart(
                                    "foto",
                                    fileName,
                                    RequestBody.create(MediaType.parse("image/*"), fileBytes)
                            );

                    request = new Request.Builder()
                            .url(url)
                            .post(multipartBuilder.build())
                            .build();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Gagal membaca foto", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            else {
                RequestBody formBody = new FormBody.Builder()
                        .add("api", "tambah_pembukuan")
                        .add("id_user", iduserr)
                        .add("jenis", namaId)
                        .add("jenis_data", namaId2)
                        .add("anggaran", anggaran)
                        .add("keterangan", keterangan)
                        .build();

                request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build();
            }

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    android.util.Log.d("API_RESPONSE", "gagal karena: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(PembukuanTambahActivity.this, "Gagal kirim data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String hasil = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject json = new JSONObject(hasil);
                            String status = json.optString("status", "");
                            String message = json.optString("message", "Tidak ada pesan");
                            if (status.equalsIgnoreCase("success")) {
                                Toast.makeText(PembukuanTambahActivity.this, message, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                ambilData();
                            } else {
                                Toast.makeText(PembukuanTambahActivity.this, "Gagal: " + message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(PembukuanTambahActivity.this, "Respon tidak valid dari server", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        });
    }

    private void pilihFoto() {
        String[] options = {"Kamera", "Galeri"};
        AlertDialog.Builder pilih = new AlertDialog.Builder(this);
        pilih.setTitle("Pilih Foto")
                .setItems(options, (dialog1, which) -> {
                    if (which == 0) {
                        if (checkPermission()) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, REQUEST_CAMERA);
                        }
                    } else {
                        if (checkPermission()) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, REQUEST_GALLERY);
                        }
                    }
                }).show();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                return false;
            }
        }
        return true;
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imgPreview.setImageBitmap(photo);
                imgPreview.setVisibility(View.VISIBLE);

                // Simpan foto ke Uri sementara
                imageUri = getImageUri(this, photo);

            } else if (requestCode == REQUEST_GALLERY) {
                imageUri = data.getData();
                imgPreview.setImageURI(imageUri);
                imgPreview.setVisibility(View.VISIBLE);
            }
        }
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "temp_image", null);
        return Uri.parse(path);
    }
    private void ambilData() {
        OkHttpClient client = new OkHttpClient();
        String iduserr = GlobalHelper.getIdUser(this);

        RequestBody formBody = new FormBody.Builder()
                .add("api", "listpembukuan")
                .add("user", iduserr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PembukuanTambahActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                    String hasil = response.body().string();
                android.util.Log.d("API_RESPONSE", "Respon dari server: " + hasil);
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(hasil);
                        JSONArray data = json.getJSONArray("data");

                        listData.clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            PembukuanClass t = new PembukuanClass();
                            t.id = obj.optString("id", "");
                            t.type = obj.optString("type", "");
                            t.type_data = obj.optString("type_data", "");
                            t.data = obj.optString("data", "0");
                            t.keterangan = obj.optString("keterangan", "");
                            t.bukti = obj.optString("bukti", "");
                            t.bulan = String.valueOf(obj.optInt("bulan", 0));
                            t.tahun = String.valueOf(obj.optInt("tahun", 0));
                            t.tanggal = String.valueOf(obj.optInt("tanggal", 0));

                            listData.add(t);
                        }


                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e("API_ERROR", "Error parsing JSON", e);
                        Toast.makeText(PembukuanTambahActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();}
                });
            }
        });
    }
}
package com.wazzgroup.penagihanwifi.acs;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;
import com.wazzgroup.penagihanwifi.client.EditPelangganActivity;
import com.wazzgroup.penagihanwifi.client.Pelanggan;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScanSnActivity extends AppCompatActivity {

    private static final int REQ_CAMERA = 100;
    private static final String TAG = "SCAN_SN_MODEM";

    private PreviewView previewView;
    private TextView txtResult;
    private ImageButton btnFlash;

    private ExecutorService executor;
    private Camera camera;
    private boolean flashOn = false;
    private boolean sudahScan = false;
    private Pelanggan pelangganLama;
    private TextView editNama;
    private ConstraintLayout btnUpdate;
    String url = GlobalHelper.BASE_URL;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_scan_sn);
        String json = getIntent().getStringExtra("data_pelanggan");
        pelangganLama = new Gson().fromJson(json, Pelanggan.class);
        previewView = findViewById(R.id.previewView);
        txtResult = findViewById(R.id.txtResult);
        btnFlash = findViewById(R.id.btnFlash);
        editNama = findViewById(R.id.nama);

        editNama.setText(pelangganLama.nama);

        executor = Executors.newSingleThreadExecutor();

        btnFlash.setOnClickListener(v -> toggleFlash());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQ_CAMERA
            );
        } else {
            startCamera();
        }
        btnUpdate = findViewById(R.id.simpan);

        btnUpdate.setOnClickListener(v -> updateData());
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(
                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                BarcodeScannerOptions options =
                        new BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                .build();

                BarcodeScanner scanner = BarcodeScanning.getClient(options);

                analysis.setAnalyzer(executor, imageProxy -> {
                    if (sudahScan || imageProxy.getImage() == null) {
                        imageProxy.close();
                        return;
                    }

                    InputImage image = InputImage.fromMediaImage(
                            imageProxy.getImage(),
                            imageProxy.getImageInfo().getRotationDegrees()
                    );

                    scanner.process(image)
                            .addOnSuccessListener(barcodes -> {


                                for (Barcode barcode : barcodes) {
                                    String raw = barcode.getRawValue();
                                    Log.d(TAG, "📦 RAW: " + raw);

                                    String sn = extractSn(raw);
                                    if (sn != null) {
                                        sudahScan = true;
                                        runOnUiThread(() ->
                                                txtResult.setText(sn)
                                        );
                                        Log.d(TAG, "✅ SN DITEMUKAN: " + sn);
                                        break;
                                    }
                                }
                            })
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Scan error", e)
                            )
                            .addOnCompleteListener(task -> imageProxy.close());
                });

                provider.unbindAll();
                camera = provider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                );

            } catch (Exception e) {
                Log.e(TAG, "Camera error", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void toggleFlash() {
        if (camera == null) return;

        flashOn = !flashOn;
        camera.getCameraControl().enableTorch(flashOn);

        btnFlash.setImageResource(
                flashOn ? R.drawable.ic_add : R.drawable.ic_add
        );
    }

    private String extractSn(String text) {
        if (text == null) return null;

        Pattern p = Pattern.compile(
                "(SN[:= ]*)?([A-Z0-9]{8,16})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(2);
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CAMERA &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void updateData() {
        String sn = txtResult.getText().toString().trim();
        if (sn.isEmpty() ) {
            Toast.makeText(this, "Lengkapi semua data!", Toast.LENGTH_SHORT).show();
            return;
        }

        String iduserr = GlobalHelper.getIdUser(this);
        RequestBody formBody = new FormBody.Builder()
                .add("api", "editsn")
                .add("id", pelangganLama.id)
                .add("user", iduserr)
                .add("sn", sn)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ScanSnActivity.this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.d(TAG, "Raw JSON: " + res);

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(res);
                        String status = obj.getString("status");

                        if (status.equalsIgnoreCase("success")) {
                            Toast.makeText(ScanSnActivity.this, "Berhasil update data", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String pesan = obj.has("message") ? obj.getString("message") : "Gagal update data";
                            Toast.makeText(ScanSnActivity.this, pesan, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(ScanSnActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}

package com.wazzgroup.penagihanwifi.halamanpelanggan.quran;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.wazzgroup.penagihanwifi.R;

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

public class DetailSuratActivity extends AppCompatActivity {

    LinearLayout layoutAyat;
    TextView txtJudul;
    ScrollView scrollView;

    Button btnPrev, btnNext, btnToggle, btnPlay;

    OkHttpClient client = new OkHttpClient();

    int nomorSurat = 1;
    boolean tampilTerjemahan = true;

    ArrayList<JSONObject> listAyat = new ArrayList<>();

    MediaPlayer mediaPlayer;
    String audioUrl = "";
    boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_surat);

        layoutAyat = findViewById(R.id.layoutAyat);
        txtJudul = findViewById(R.id.txtJudul);
        scrollView = findViewById(R.id.scrollView);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnToggle = findViewById(R.id.btnToggle);
        btnPlay = findViewById(R.id.btnPlay);

        nomorSurat = getIntent().getIntExtra("nomor", 1);

        getDetailSurat(nomorSurat);

        btnPrev.setOnClickListener(v -> {
            if (nomorSurat > 1) {
                nomorSurat--;
                stopAudio();
                getDetailSurat(nomorSurat);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (nomorSurat < 114) {
                nomorSurat++;
                stopAudio();
                getDetailSurat(nomorSurat);
            }
        });

        btnToggle.setOnClickListener(v -> {
            tampilTerjemahan = !tampilTerjemahan;
            btnToggle.setText(tampilTerjemahan ?
                    "Arti Aktif" : "Arti MAti");
            refreshTampilanAyat();
        });

        btnPlay.setOnClickListener(v -> {
            if (!isPlaying) {
                playAudio();
            } else {
                stopAudio();
            }
        });
    }

    private void getDetailSurat(int id) {

        listAyat.clear();
        layoutAyat.removeAllViews();
        scrollView.smoothScrollTo(0, 0);

        Request request = new Request.Builder()
                .url("https://equran.id/api/v2/surat/" + id)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(DetailSuratActivity.this,
                                "Gagal koneksi", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String res = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(res);
                        JSONObject data = obj.getJSONObject("data");

                        txtJudul.setText(
                                data.getString("namaLatin") +
                                        " (" + data.getString("arti") + ")"
                        );

                        // 🔥 Ambil Audio URL
                        JSONObject audioObj = data.getJSONObject("audioFull");
                        audioUrl = audioObj.getString("05");

                        JSONArray ayatArray = data.getJSONArray("ayat");

                        for (int i = 0; i < ayatArray.length(); i++) {
                            listAyat.add(ayatArray.getJSONObject(i));
                        }

                        refreshTampilanAyat();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void refreshTampilanAyat() {

        layoutAyat.removeAllViews();

        for (JSONObject ayat : listAyat) {
            try {
                tambahAyatKeLayout(ayat);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void tambahAyatKeLayout(JSONObject ayat) throws JSONException {

        String teksArab = ayat.getString("teksArab");
        String teksIndo = ayat.getString("teksIndonesia");
        String nomorAyat = ayat.getString("nomorAyat");

        TextView tv = new TextView(this);
        tv.setTextSize(23);
        tv.setPadding(0, 25, 0, 25);

        if (tampilTerjemahan) {
            tv.setText(teksArab + " (" + nomorAyat + ")\n\n" + teksIndo);
        } else {
            tv.setText(teksArab + " (" + nomorAyat + ")");
        }

        layoutAyat.addView(tv);
    }

    private void playAudio() {

        if (audioUrl.isEmpty()) return;

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();

            isPlaying = true;
            btnPlay.setText("⏹ Stop Audio");

            mediaPlayer.setOnCompletionListener(mp -> {
                stopAudio();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopAudio() {

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        isPlaying = false;
        btnPlay.setText("▶ Play Audio");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudio();
    }
}

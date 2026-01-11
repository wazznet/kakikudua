package com.wazzgroup.penagihanwifi.halamanpelanggan.tv;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.wazzgroup.penagihanwifi.R;

public class PlayerActivity extends AppCompatActivity {

    private SimpleExoPlayer player;
    private PlayerView playerView;

    String url, userAgent, licenseType, licenseKey, referrer, format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerView = findViewById(R.id.playerView);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // Tutup PlayerActivity saat tombol ditekan
        
        // Ambil data dari Intent
        url = getIntent().getStringExtra("url");
        userAgent = getIntent().getStringExtra("user_agent");
        licenseType = getIntent().getStringExtra("license_type");
        licenseKey = getIntent().getStringExtra("license_key");
        referrer = getIntent().getStringExtra("referrer");
        format = getIntent().getStringExtra("format");

        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true); // << Tambahan ini supaya layar tetap nyala

        playStream();
    }

    private void playStream() {
        try {
            DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                    .setUserAgent(userAgent != null ? userAgent : "ExoPlayer");

            if (referrer != null) {
                dataSourceFactory.setDefaultRequestProperties(getHeaders());
            }

            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(Uri.parse(url))
                    .build();

            MediaSource mediaSource;

            if (format.equalsIgnoreCase("m3u8") || url.endsWith(".m3u8")) {
                mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            } else if (format.equalsIgnoreCase("mpd") || url.endsWith(".mpd")) {
                DefaultDrmSessionManager drmSessionManager = createDrmSessionManager();

                DashMediaSource.Factory dashFactory = new DashMediaSource.Factory(dataSourceFactory);

                if (drmSessionManager != null) {
                    dashFactory.setDrmSessionManagerProvider(unused -> drmSessionManager);
                }

                mediaSource = dashFactory.createMediaSource(mediaItem);
            } else {
                showError("Format tidak didukung");
                return;
            }

            player.setMediaSource(mediaSource);
            player.prepare();
            player.play();

            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    showError("Playback Error: " + error.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("Gagal memutar: " + e.getMessage());
        }
    }

    private DefaultDrmSessionManager createDrmSessionManager() throws UnsupportedDrmException {
        if (licenseType == null || licenseKey == null || licenseKey.isEmpty()) {
            return null; // Tidak pakai DRM
        }

        try {
            if (licenseType.equalsIgnoreCase("clearkey")) {
                UUID clearkeyUUID = UUID.fromString("e2719d58-a985-b3c9-781a-b030af78d30e");

                String[] splitKey = licenseKey.split(":");
                if (splitKey.length != 2) {
                    throw new IllegalArgumentException("Format Clearkey harus kid:key");
                }

                String kid = splitKey[0];
                String key = splitKey[1];

                String jsonKey = generateClearkeyJson(kid, key);
                byte[] clearkeyBytes = jsonKey.getBytes(StandardCharsets.UTF_8);

                return new DefaultDrmSessionManager.Builder()
                        .setUuidAndExoMediaDrmProvider(clearkeyUUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
                        .build(new MediaDrmCallback() {
                            @Override
                            public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest request) {
                                return clearkeyBytes;
                            }

                            @Override
                            public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest request) {
                                return new byte[0];
                            }
                        });

            } else if (licenseType.equalsIgnoreCase("com.widevine.alpha")) {
                UUID widevineUUID = UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed");

                HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseKey,
                        new DefaultHttpDataSource.Factory()
                                .setUserAgent(userAgent != null ? userAgent : "ExoPlayer")
                                .setDefaultRequestProperties(getHeaders()));

                return new DefaultDrmSessionManager.Builder()
                        .setUuidAndExoMediaDrmProvider(widevineUUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
                        .build(drmCallback);
            } else {
                throw new IllegalArgumentException("DRM tidak didukung");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("DRM Error: " + e.getMessage());
            return null;
        }
    }

    private String generateClearkeyJson(String kidHex, String keyHex) {
        String kidB64 = base64UrlEncode(hexStringToByteArray(kidHex));
        String keyB64 = base64UrlEncode(hexStringToByteArray(keyHex));

        try {
            JSONArray keys = new JSONArray();
            JSONObject keyObj = new JSONObject();
            keyObj.put("kty", "oct");
            keyObj.put("kid", kidB64);
            keyObj.put("k", keyB64);
            keys.put(keyObj);

            JSONObject obj = new JSONObject();
            obj.put("keys", keys);
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.encodeToString(data, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (referrer != null && !referrer.isEmpty()) {
            headers.put("Referer", referrer);
        }
        return headers;
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}

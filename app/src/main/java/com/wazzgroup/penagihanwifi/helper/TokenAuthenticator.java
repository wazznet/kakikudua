package com.wazzgroup.penagihanwifi.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.wazzgroup.penagihanwifi.GlobalHelper;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TOKEN_DEBUG";
    private static final int MAX_REFRESH_RETRY = 3;

    private Context context;
    private String url = GlobalHelper.BASE_URL_V2;

    public TokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        // Hindari infinite loop dari OkHttp
        if (responseCount(response) >= 2) {
            Log.d(TAG, "Auth dibatalkan karena response loop");
            return null;
        }

        String refreshToken = TokenManager.getRefreshToken(context);

        if (refreshToken == null || refreshToken.isEmpty()) {
            Log.d(TAG, "Refresh token kosong. Tidak bisa refresh.");
            return null;
        }

        Log.d(TAG, "Mulai proses refresh token...");

        @SuppressLint("HardwareIds")
        String androidId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        for (int attempt = 1; attempt <= MAX_REFRESH_RETRY; attempt++) {

            Log.d(TAG, "Percobaan refresh ke-" + attempt);

            try {

                OkHttpClient client = new OkHttpClient();

                RequestBody body = new FormBody.Builder()
                        .add("api", "refresh")
                        .add("refresh_token", refreshToken)
                        .add("androidId", androidId)
                        .build();

                Request refreshRequest = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                Response refreshResponse = client.newCall(refreshRequest).execute();

                if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {

                    String json = refreshResponse.body().string();
                    Log.d(TAG, "Response refresh: " + json);

                    JSONObject obj = new JSONObject(json);

                    String status = obj.optString("status", "");

                    if (status.equals("success")) {

                        String newAccess = obj.optString("access_token", "");
                        String newRefresh = obj.optString("refresh_token", "");

                        if (!newAccess.isEmpty() && !newRefresh.isEmpty()) {

                            Log.d(TAG, "Refresh berhasil. Simpan token baru.");

                            TokenManager.saveTokens(context, newAccess, newRefresh);

                            return response.request().newBuilder()
                                    .header("Authorization", "Bearer " + newAccess)
                                    .build();
                        }
                    }

                    // Kalau server bilang token invalid → logout
                    if (status.equals("invalid_token")) {
                        Log.d(TAG, "Refresh token invalid. Logout dipanggil.");
                        TokenManager.logout(context);
                        return null;
                    }

                } else {
                    Log.d(TAG, "HTTP gagal: " + refreshResponse.code());
                }

            } catch (Exception e) {
                Log.d(TAG, "Error refresh attempt " + attempt + ": " + e.getMessage());
            }

            // Delay kecil sebelum retry
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }

        Log.d(TAG, "Refresh gagal setelah " + MAX_REFRESH_RETRY + " percobaan.");

        // JANGAN logout kalau cuma network error
        return null;
    }

    private int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }
}
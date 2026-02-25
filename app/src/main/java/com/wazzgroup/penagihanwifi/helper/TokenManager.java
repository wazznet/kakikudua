package com.wazzgroup.penagihanwifi.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TokenManager {

    private static final String PREF_NAME = "app_pref";
    private static final String ACCESS = "access_token";
    private static final String REFRESH = "refresh_token";

    public static void saveTokens(Context ctx, String access, String refresh) {

        if (access == null || access.isEmpty()) {
            Log.d("TOKEN_DEBUG", "Access kosong. Tidak disimpan.");
            return;
        }

        if (refresh == null || refresh.isEmpty()) {
            Log.d("TOKEN_DEBUG", "Refresh kosong. Tidak disimpan.");
            return;
        }

        SharedPreferences pref = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        pref.edit()
                .putString(ACCESS, access)
                .putString(REFRESH, refresh)
                .commit(); // pakai commit supaya aman
    }

    public static String getAccessToken(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(ACCESS, "");
    }

    public static String getRefreshToken(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(REFRESH, "");
    }

    public static void logout(Context ctx) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}

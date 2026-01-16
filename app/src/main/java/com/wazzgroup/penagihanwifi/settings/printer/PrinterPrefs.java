package com.wazzgroup.penagihanwifi.settings.printer;

import android.content.Context;
import android.content.SharedPreferences;

public class PrinterPrefs {

    private static final String PREF = "printer_pref";
    private static final String KEY_MAC = "printer_mac";

    public static void savePrinter(Context ctx, String mac) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_MAC, mac).apply();
    }

    public static String getPrinter(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(KEY_MAC, null);
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }
}

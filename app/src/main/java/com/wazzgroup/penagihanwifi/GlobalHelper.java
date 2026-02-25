package com.wazzgroup.penagihanwifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;

public class GlobalHelper {

    public static String getIdUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString("iduser", "");
    }
    public static String getIdlogin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString("idlogin", "");

    }
    public static String getIdsub(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString("sub_akun", "");

    }
    public static String getno(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString("nohp", "");

    }
    public static String getnamaku(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString("nama", "");

    }
    public static String getIdPelanggan(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString("idpelannggan", "");
    }

    public static String getwifiid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString("wifiid", "");
    }
    public static String getwifinama(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString("nama_wifi", "");
    }
    public static String getakses(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString("akses", "");
    }
    public static final String BASE_URL = "https://fufufafa.wazzapp.my.id/tambah.php";
    public static final String BASE_URLtv = "https://fufufafa.wazzapp.my.id/playlist.php";
    public static final String BASE_URL_wa = "https://whatsapp-gateway.wazzgroup.com";
    public static final String BASE_URL_V2 = "https://fufufafa.wazzapp.my.id/orangsolo/komdigi.php";


}

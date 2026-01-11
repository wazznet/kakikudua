package com.wazzgroup.penagihanwifi.halamanpelanggan.paket;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;

import com.google.firebase.messaging.FirebaseMessaging;
import com.wazzgroup.penagihanwifi.R;

public class PelangganPaketActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "my_channel_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pelanggan_paket);
        // Ambil token Firebase (device token unik untuk tiap HP)
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM_TOKEN", "Gagal ambil token", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("FCM_TOKEN", "Token: " + token);

                    // TODO: kirim token ini ke server PHP (pakai OkHttp atau Retrofit)
                });
    }

    }
//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Channel Notifikasi";
//            String description = "Channel untuk menampilkan notifikasi penting";
//            int importance = NotificationManager.IMPORTANCE_HIGH; // agar muncul di atas
//
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }
//
//    // 🔹 Fungsi untuk menampilkan notifikasi
//    private void showNotification(String title, String message) {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti dengan ikon kamu
//                .setContentTitle(title)
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_HIGH) // agar muncul di atas
//                .setAutoCancel(true);
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        notificationManager.notify(100, builder.build());
//    }
//}
package com.wazzgroup.penagihanwifi.lottie;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.wazzgroup.penagihanwifi.R;

public class LottieSuccessActivity extends AppCompatActivity {

    public static void showLottie(View rootView) {
        View lottieContainer = rootView.findViewById(R.id.lottieContainer);
        LottieAnimationView anim = rootView.findViewById(R.id.lottieSuccess);

        if (lottieContainer != null && anim != null) {
            lottieContainer.setVisibility(View.VISIBLE);
            anim.setProgress(0f);
            anim.playAnimation();

            new Handler().postDelayed(() -> {
                lottieContainer.setVisibility(View.GONE);
            }, 2000);
        }
    }
}
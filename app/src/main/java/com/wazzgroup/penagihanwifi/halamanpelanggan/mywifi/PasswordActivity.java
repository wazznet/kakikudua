package com.wazzgroup.penagihanwifi.halamanpelanggan.mywifi;


import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wazzgroup.penagihanwifi.R;

public class PasswordActivity extends AppCompatActivity {


    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("gateway_ip")) {
            String gatewayIp = intent.getStringExtra("gateway_ip");
            webView.loadUrl("http://" + gatewayIp);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack(); // Kembali ke halaman sebelumnya dalam WebView
        } else {
            super.onBackPressed(); // Keluar dari WebViewActivity jika tidak ada halaman sebelumnya
        }
    }
}
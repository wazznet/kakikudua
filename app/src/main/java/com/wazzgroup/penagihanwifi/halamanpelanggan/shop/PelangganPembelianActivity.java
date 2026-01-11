package com.wazzgroup.penagihanwifi.halamanpelanggan.shop;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.wazzgroup.penagihanwifi.GlobalHelper;
import com.wazzgroup.penagihanwifi.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PelangganPembelianActivity extends AppCompatActivity {

    private TextView namapelanggannya,saldo;
    String url = GlobalHelper.BASE_URL; // Pastikan ini endpoint yang sesuai
    private OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pelanggan_pembelian);
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        String nama_pelanggan = prefs.getString("nama_pelanggan", "");
        String id = prefs.getString("idpelannggan", "");

        namapelanggannya = findViewById(R.id.namapelanggannya);
        saldo = findViewById(R.id.textView11);
        namapelanggannya.setText(nama_pelanggan);
        ceksaldo(id);
        LinearLayout emoney = findViewById(R.id.emoney);
        LinearLayout pln = findViewById(R.id.plnpulsa);
        LinearLayout game = findViewById(R.id.game);

        emoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openemoney();
            }
        });

        game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game();
            }
        });

    }
    public void openemoney() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate layout custom
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_emoney, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        LinearLayout shopeepay = dialogView.findViewById(R.id.shopee);
        LinearLayout gopay = dialogView.findViewById(R.id.gopay);
        LinearLayout ovo = dialogView.findViewById(R.id.ovo);
        LinearLayout dana = dialogView.findViewById(R.id.dana);
        shopeepay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopEmoneyActivity.class);

                intent.putExtra("operator", "SHOPEE PAY");
                startActivity(intent);

            }
        });
        gopay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopEmoneyActivity.class);

                intent.putExtra("operator", "GO PAY");
                startActivity(intent);

            }
        });
        ovo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopEmoneyActivity.class);

                intent.putExtra("operator", "OVO");
                startActivity(intent);

            }
        });
        dana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopEmoneyActivity.class);

                intent.putExtra("operator", "DANA");
                startActivity(intent);

            }
        });


        dialog.show();
    }
    public void pln() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate layout custom
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pln, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        LinearLayout shopeepay = dialogView.findViewById(R.id.pra);
        LinearLayout gopay = dialogView.findViewById(R.id.pasca);
        shopeepay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopPlnpraActivity.class);

                startActivity(intent);

            }
        });
        gopay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopPlnpascaActivity.class);

                startActivity(intent);

            }
        });



        dialog.show();
    }
    public void game() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate layout custom
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_game, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        LinearLayout ml = dialogView.findViewById(R.id.ml);
        LinearLayout ff = dialogView.findViewById(R.id.ff);
        LinearLayout pubg = dialogView.findViewById(R.id.pubg);
        LinearLayout gi = dialogView.findViewById(R.id.gi);
        LinearLayout pb = dialogView.findViewById(R.id.pb);
        LinearLayout ros = dialogView.findViewById(R.id.ros);
        ml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopGameActivity.class);
                intent.putExtra("operator", "MOBILE LEGENDS");
                intent.putExtra("note","1");
                startActivity(intent);

            }
        });
        ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopGameActivity.class);
                intent.putExtra("operator", "FREE FIRE");
                intent.putExtra("note","2");

                startActivity(intent);

            }
        });
        pubg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopGameActivity.class);
                intent.putExtra("operator", "PUBG MOBILE");
                intent.putExtra("note","2");

                startActivity(intent);

            }
        });
        gi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopGameActivity.class);
                intent.putExtra("operator", "Genshin Impact");
                intent.putExtra("note","1");

                startActivity(intent);

            }
        });
        pb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopGameActivity.class);
                intent.putExtra("operator", "POINT BLANK");
                intent.putExtra("note","2");

                startActivity(intent);

            }
        });
        ros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PelangganPembelianActivity.this, ShopGameActivity.class);
                intent.putExtra("operator", "2");
                intent.putExtra("note","-");

                startActivity(intent);

            }
        });





        dialog.show();
    }
    private void ceksaldo(String id) {
        RequestBody formBody = new FormBody.Builder()
                .add("api", "ceksaldo") // Pastikan API PHP mendukung login dengan ID
                .add("id", id)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PelangganPembelianActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getString("status").equals("success")) {
                            String saldopelanggan = obj.getString("saldopelanggan");
                            saldo.setText("Rp. "+ saldopelanggan);

                        } else {
                            Toast.makeText(PelangganPembelianActivity.this, "Login gagal: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(PelangganPembelianActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    public void onLinearLayoutClick(View view) {
        int id = view.getId();

        if (id == R.id.pulsa) {
            startActivity(new Intent(this, ShopPulsaActivity.class));
        } else if (id == R.id.data) {
            startActivity(new Intent(this, ShopDataActivity.class));
        }
//        else if (id == R.id.game) {
//            startActivity(new Intent(this, ShopGameActivity.class));
//        }
        else if (id == R.id.plnpulsa) {
            startActivity(new Intent(this, ShopPlnpraActivity.class));
        }
//        else if (id == R.id.emoney) {
//            startActivity(new Intent(this, ShopEmoneyActivity.class));
//        }
//        else if (id == R.id.plnpasca) {
//            startActivity(new Intent(this, ShopPlnpascaActivity.class));
//        }
    }
}
package com.wazzgroup.penagihanwifi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    EditText editUsername, editPassword;
    Button btnLogin;
    OkHttpClient client = new OkHttpClient();
    String url = GlobalHelper.BASE_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String akses = prefs.getString("akses", "");

        if (isLoggedIn) {
            Intent i;
            switch (akses.toLowerCase()) {
                case "admin":
                    i = new Intent(LoginActivity.this, TeknisiActivity.class);
                    break;
                case "teknisi":
                    i = new Intent(LoginActivity.this, TeknisiActivity.class);
                    break;
                case "cs":
                    i = new Intent(LoginActivity.this, CsActivity.class);
                    break;
                default:
                    Toast.makeText(this, "Akses tidak dikenali", Toast.LENGTH_SHORT).show();
                    return;
            }
            startActivity(i);
            finish();
            return;
        }


        editUsername = findViewById(R.id.etUsername);
        editPassword = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username dan Password harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String version = Build.VERSION.RELEASE; // Versi Android
        String brand = Build.BRAND; // Brand HP

        String deviceInfo = brand + " " +manufacturer + " " + model + " (Android " + version + ")";
        String androidId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        RequestBody formBody = new FormBody.Builder()
                .add("api", "login")
                .add("username", username)
                .add("password", password)
                .add("deviceInfo", deviceInfo)
                .add("androidId", androidId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, "Gagal koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getString("status").equals("success")) {
                            //String akses = obj.getString("akses");
                            String nama_wifi = obj.getString("nama_wifi");
                            String id = obj.getString("id");
                            String sub_akun = obj.getString("sub_akun");
                            String level = obj.getString("level");
                            String nohp = obj.getString("nohp");
                            String nama = obj.getString("nama");
                            String akses = obj.getString("akses").trim(); // Tambahkan trim disini
                            Log.d("DEBUG_AKSES", "Akses dari server: [" + akses + "]");

                            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("nama_wifi", nama_wifi);
                            editor.putString("idlogin", id);
                            editor.putString("nama", nama);
                            editor.putString("nohp", nohp);
                            try {
                                int subAkunInt = Integer.parseInt(sub_akun);

                                if (subAkunInt > 0) {
                                    editor.putString("iduser", sub_akun);
                                } else {
                                    editor.putString("iduser", id);
                                }

                            } catch (NumberFormatException e) {
                                // Jika sub_akun bukan angka, anggap saja 0 atau fallback ke id
                                editor.putString("iduser", id);
                            }
                            editor.putString("level", level);
                            editor.putString("akses", akses);
                            editor.apply();

                            Intent i;
                            switch (akses.toLowerCase()) {
                                case "admin":
                                    i = new Intent(LoginActivity.this, TeknisiActivity.class);
                                    break;
                                case "teknisi":
                                    i = new Intent(LoginActivity.this, TeknisiActivity.class);
                                    break;
                                case "cs":
                                    i = new Intent(LoginActivity.this, CsActivity.class);
                                    break;
                                default:
                                    Toast.makeText(LoginActivity.this, "Akses tidak dikenali", Toast.LENGTH_SHORT).show();
                                    return;
                            }

                            startActivity(i);
                            finish();

                        } else {
                            Toast.makeText(LoginActivity.this, "Login gagal: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }
} // END

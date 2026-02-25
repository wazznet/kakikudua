package com.wazzgroup.penagihanwifi.helper;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    private Context context;

    public TokenInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        String accessToken = TokenManager.getAccessToken(context);
        String refreshToken = TokenManager.getRefreshToken(context);

        Log.d("TOKEN_DEBUG", "AccessToken: " + accessToken);
        Log.d("TOKEN_DEBUG", "refreshToken: " + refreshToken);

        Request original = chain.request();

        if (accessToken != null) {
            Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
            return chain.proceed(request);
        }

        return chain.proceed(original);
    }
}

package com.hcmute.mobile_android.network;

import android.content.Context;

import com.hcmute.mobile_android.util.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        var originalRequest = chain.request();
        var request = originalRequest;
        String token = new TokenManager(context).getToken();
        if (token != null && !token.isEmpty()) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }
        
        Response response = chain.proceed(request);
        
        if (response.code() == 401) {
            // Token is likely invalid or expired
            new TokenManager(context).clearToken();
            
            // Redirect to login only if not already on login/otp screens
            android.content.Intent intent = new android.content.Intent(context, com.hcmute.mobile_android.ui.activities.LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
        
        return response;
    }
}

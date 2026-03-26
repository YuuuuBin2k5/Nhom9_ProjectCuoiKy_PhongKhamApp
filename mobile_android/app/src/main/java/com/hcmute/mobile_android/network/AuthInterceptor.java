package com.hcmute.mobile_android.network;

import android.content.Context;

import com.hcmute.mobile_android.util.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        TokenManager tm = new TokenManager(context);
        String token = tm.getToken();

        if (token != null && !token.isEmpty()) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        Response response = chain.proceed(request);

        // Handle Session Expired (401 Unauthorized)
        if (response.code() == 401) {
            String refreshToken = tm.getRefreshToken();
            boolean refreshSuccess = false;
            
            if (refreshToken != null && !refreshToken.isEmpty() && !request.url().encodedPath().contains("/refresh")) {
                String newToken = refreshAccessToken(refreshToken);
                if (newToken != null) {
                    tm.saveToken(newToken);
                    response.close();
                    Request newRequest = request.newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer " + newToken)
                            .build();
                    return chain.proceed(newRequest);
                }
            }
            
            // If refresh fails or no refresh token, redirect to login
            if (!refreshSuccess) {
                tm.clearToken();
                android.content.Intent intent = new android.content.Intent(context, com.hcmute.mobile_android.ui.activities.LoginActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        }
        return response;
    }

    private String refreshAccessToken(String refreshToken) {
        String baseUrl = com.hcmute.mobile_android.BuildConfig.API_BASE_URL;
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS).build();
        
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("refreshToken", refreshToken);
            okhttp3.RequestBody body = okhttp3.RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));
            
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(baseUrl + "api/auth/refresh")
                    .post(body)
                    .build();
                    
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String respStr = response.body().string();
                    org.json.JSONObject obj = new org.json.JSONObject(respStr);
                    return obj.getString("token");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

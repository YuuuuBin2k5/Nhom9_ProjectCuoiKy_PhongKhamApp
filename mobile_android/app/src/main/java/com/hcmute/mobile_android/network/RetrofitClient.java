package com.hcmute.mobile_android.network;

import android.content.Context;

import com.hcmute.mobile_android.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Lớp khởi tạo và quản lý đối tượng Retrofit duy nhất (Singleton) cho toàn bộ ứng dụng.
 */
public class RetrofitClient {

    private static volatile Retrofit retrofit = null;
    private static String lastBaseUrl = null;

    /**
     * Trả về đối tượng ApiService để thực hiện các yêu cầu mạng.
     *
     * @param context Context của ứng dụng.
     * @return Đối tượng ApiService đã được cấu hình.
     */
    public static ApiService getApiService(Context context) {
        String baseUrl = BuildConfig.API_BASE_URL;
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        if (retrofit == null || !baseUrl.equals(lastBaseUrl)) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null || !baseUrl.equals(lastBaseUrl)) {
                    lastBaseUrl = baseUrl;
                    AuthInterceptor authInterceptor = new AuthInterceptor(context.getApplicationContext());
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .addInterceptor(authInterceptor)
                            .addInterceptor(logging)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build();
                }
            }
        }
        return retrofit.create(ApiService.class);
    }
}

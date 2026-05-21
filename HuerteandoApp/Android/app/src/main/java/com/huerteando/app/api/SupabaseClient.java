package com.huerteando.app.api;

import android.content.Context;

import com.huerteando.app.MyApp;
import com.huerteando.app.R;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            Context context = MyApp.getInstance().getApplicationContext();
            String baseUrl = context.getString(R.string.supabase_url_signup)
                    .replace("auth/v1/signup", "");
            String apiKey = context.getString(R.string.supabase_api_key);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .header("apikey", apiKey)
                                .header("Content-Type", "application/json")
                                .build();
                        return chain.proceed(request);
                    })
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build();
        }
        return retrofit;
    }
}

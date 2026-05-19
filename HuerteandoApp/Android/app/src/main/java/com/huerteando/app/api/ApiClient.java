package com.huerteando.app.api;

import com.huerteando.app.MyApp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String BASE_URL = "https://huerteandoapp-1.onrender.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request.Builder requestBuilder = chain.request().newBuilder();
                        String token = MyApp.getSession().getToken();
                        if (token != null) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(requestBuilder.build());
                    })
                    .addInterceptor(chain -> {
                        okhttp3.Response response = chain.proceed(chain.request());
                        if (response.code() == 401) {
                            MyApp.getSession().cerrarSesion();
                            retrofit = null;
                        }
                        return response;
                    })
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build();
        }
        return retrofit;
    }
}
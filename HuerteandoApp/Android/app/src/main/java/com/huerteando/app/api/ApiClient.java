package com.huerteando.app.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient — Configuración básica de Retrofit sin JWT.
 * 
 * Eliminado el interceptor de autenticación (JWT) porque el
 * backend utiliza un sistema de login básico basado en nick y password.
 */
public class ApiClient {

    public static final String BASE_URL = "https://huerteandoapp-1.onrender.com/";
    private static Retrofit retrofit = null;

    /**
     * Devuelve una instancia única de Retrofit.
     * Ya no necesitamos pasarle el token JWT.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Interceptor de LOGGING para depuración en Logcat
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
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

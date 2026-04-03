package com.huerteando.app.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient — Fábrica de instancias de Retrofit
 *
 * CONCEPTO IMPORTANTE:
 * Retrofit es el "cartero" que envía tus peticiones HTTP al servidor.
 * OkHttp es el "sistema de transporte" que usa Retrofit por debajo.
 * El Interceptor es como un "sello" que se añade automáticamente a cada carta (petición).
 *
 * PATRÓN USADO: No usamos Singleton puro porque el token puede cambiar
 * entre sesiones. En su lugar, creamos una instancia por token.
 * Para proyectos más grandes se usaría un DI framework como Hilt/Dagger.
 */
public class ApiClient {

    // ⚠️ IMPORTANTE: Cambia esta URL según dónde corra tu servidor:
    // - Emulador Android →  "http://10.0.2.2:8080/api/"
    // - Dispositivo real  → "http://TU_IP_LOCAL:8080/api/"  (ej: 192.168.1.50:8080/api/)
    // - Producción        → "https://tudominio.com/api/"
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";

    /**
     * Crea un cliente Retrofit configurado con el token JWT del usuario.
     *
     * @param token El token JWT obtenido al hacer login. Puede ser null (para login/registro).
     * @return Instancia de Retrofit lista para crear el ApiService.
     */
    public static Retrofit getClient(String token) {

        // --- 1. INTERCEPTOR DE AUTENTICACIÓN ---
        // Un interceptor "intercepta" cada petición antes de enviarla y puede modificarla.
        // Aquí añadimos el token JWT a la cabecera "Authorization" de TODAS las peticiones.
        // Así no tenemos que añadirlo manualmente en cada llamada.
        okhttp3.Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder();

            if (token != null && !token.isEmpty()) {
                // El formato estándar es: "Bearer " + el token
                // "Bearer" indica que es un token de tipo portador (JWT)
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            return chain.proceed(requestBuilder.build());
        };

        // --- 2. INTERCEPTOR DE LOGGING (solo en debug) ---
        // Muestra en Logcat todas las peticiones y respuestas HTTP.
        // MUY ÚTIL para depurar. Se elimina automáticamente en producción (debugImplementation).
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Muestra body completo

        // --- 3. CLIENTE HTTP (OkHttp) ---
        // OkHttp es la librería de red subyacente. Le añadimos nuestros interceptores.
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)    // Primero el de auth
                .addInterceptor(loggingInterceptor) // Luego el de log
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Timeout de conexión
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)    // Timeout de lectura
                .build();

        // --- 4. RETROFIT ---
        // Retrofit convierte automáticamente tus interfaces Java (ApiService)
        // en peticiones HTTP reales. GsonConverterFactory convierte JSON ↔ objetos Java.
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();
    }

    /**
     * Versión sin token — para llamadas públicas (login y registro).
     */
    public static Retrofit getClient() {
        return getClient(null);
    }
}
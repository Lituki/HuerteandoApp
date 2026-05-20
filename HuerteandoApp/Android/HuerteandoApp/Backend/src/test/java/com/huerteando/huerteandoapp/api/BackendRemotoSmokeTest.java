package com.huerteando.huerteandoapp.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class BackendRemotoSmokeTest {

    private static final String BASE_URL = System.getProperty(
            "remote.api.base-url",
            "https://huerteandoapp-1.onrender.com"
    );

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @BeforeAll
    static void comprobarBackendRemotoDisponible() {
        assumeTrue(Boolean.getBoolean("remote.tests"),
                "Pruebas remotas desactivadas. Ejecutar con -Dremote.tests=true");
        assumeTrue(backendDisponible(), "La API remota no esta disponible en " + BASE_URL);
    }

    @Test
    void remotoListarObservacionesDevuelveOk() throws Exception {
        HttpResponse<String> response = get("/api/observaciones/");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("["));
    }

    @Test
    void remotoListarTiposObservacionDevuelveOk() throws Exception {
        HttpResponse<String> response = get("/api/tipos-observacion");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("["));
    }

    @Test
    void remotoListarEspeciesDevuelveOk() throws Exception {
        HttpResponse<String> response = get("/api/especies");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("["));
    }

    @Test
    void remotoDisponibilidadDevuelveCamposEsperados() throws Exception {
        String nick = encode("sergio");
        String email = encode("sergio@demo.local");

        HttpResponse<String> response = get("/api/auth/disponibilidad?nick=" + nick + "&email=" + email);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("nickDisponible"));
        assertTrue(response.body().contains("emailDisponible"));
        assertTrue(response.body().contains("disponible"));
    }

    @Test
    void remotoCrearObservacionSinTokenDevuelveUnauthorized() throws Exception {
        String json = """
                {
                  "titulo": "Prueba remota sin token",
                  "latitud": 37.98,
                  "longitud": -1.12
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/observaciones"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
    }

    private static HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static boolean backendDisponible() {
        try {
            return get("/api/observaciones/").statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

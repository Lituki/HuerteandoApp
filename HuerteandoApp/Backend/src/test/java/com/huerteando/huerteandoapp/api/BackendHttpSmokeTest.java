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

class BackendHttpSmokeTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    @BeforeAll
    static void comprobarBackendArrancado() {
        assumeTrue(backendDisponible(), "El backend local no esta arrancado en " + BASE_URL);
    }

    @Test
    void listarObservacionesDevuelveOk() throws Exception {
        HttpResponse<String> response = get("/api/observaciones/");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("["));
        assertTrue(response.body().contains("titulo"));
    }

    @Test
    void listarTiposObservacionDevuelveOk() throws Exception {
        HttpResponse<String> response = get("/api/tipos-observacion");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("["));
        assertTrue(response.body().contains("nombre"));
    }

    @Test
    void listarEspeciesDevuelveOk() throws Exception {
        HttpResponse<String> response = get("/api/especies");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("["));
        assertTrue(response.body().contains("nombreCientifico"));
    }

    @Test
    void disponibilidadDetectaUsuarioExistente() throws Exception {
        String nick = encode("sergio");
        String email = encode("sergio@demo.local");

        HttpResponse<String> response = get("/api/auth/disponibilidad?nick=" + nick + "&email=" + email);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("nickDisponible"));
        assertTrue(response.body().contains("emailDisponible"));
        assertTrue(response.body().contains("disponible"));
    }

    @Test
    void crearObservacionSinTokenDevuelveUnauthorized() throws Exception {
        String json = """
                {
                  "titulo": "Prueba sin token",
                  "latitud": 37.98,
                  "longitud": -1.12
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/observaciones"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
    }

    private static HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(5))
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

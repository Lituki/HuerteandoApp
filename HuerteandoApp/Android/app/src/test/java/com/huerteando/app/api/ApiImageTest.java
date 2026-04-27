package com.huerteando.app.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.huerteando.app.clases.Imagen;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Clase de prueba para verificar si el backend acepta el envío de imágenes en formato JSON.
 */
public class ApiImageTest {

    private static final String BASE_URL = "https://huerteandoapp-1.onrender.com/";

    @Test
    public void testSubirImagenJson() throws IOException {
        // 1. Configurar Retrofit para la prueba
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        // 2. Preparar el objeto Imagen (JSON)
        // Usamos un ID de observación existente para la prueba (ej: 9)
        Long idObservacion = 9L; 
        Imagen imagenPrueba = new Imagen();
        imagenPrueba.setUrlArchivo("uploads/test_test.jpg");
        imagenPrueba.setTitulo("Prueba desde Test Unitario");

        // 3. Realizar la llamada síncrona

        Response<Imagen> response = api.subirImagen(idObservacion, imagenPrueba).execute();

        // 4. Verificar resultados
        System.out.println("Código de respuesta: " + response.code());
        if (response.errorBody() != null) {
            System.out.println("Cuerpo de error: " + response.errorBody().string());
        }

        // Si el servidor acepta JSON, debería devolver 201 (Created) o 200 (OK)
        assertTrue("El servidor debería aceptar la petición JSON (Código 200 o 201)", 
                response.code() == 201 || response.code() == 200);
        
        assertNotNull(response.body());
        assertEquals("uploads/test_test.jpg", response.body().getUrlArchivo());
    }
}

package com.huerteando.app.clases;

/**
 * Respuesta del servidor al subir imágenes.
 */
public class ImagenResponse {
    private boolean success;
    private String mensaje;
    private String[] urls;  // URLs de las imágenes subidas

    public boolean isSuccess() { return success; }
    public String getMensaje() { return mensaje; }
    public String[] getUrls() { return urls; }
}

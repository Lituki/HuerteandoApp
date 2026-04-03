package com.huerteando.app.clases;

/**
 * Clase para enviar un nuevo comentario al servidor.
 */
public class ComentarioRequest {
    private String contenido;

    public ComentarioRequest(String contenido) {
        this.contenido = contenido;
    }

    public String getContenido() { return contenido; }
}

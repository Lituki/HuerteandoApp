package com.huerteando.app.clases;

import com.google.gson.annotations.SerializedName;

/**
 * Clase para enviar un nuevo comentario al servidor.
 * Se ha añadido el objeto usuario para cumplir con los requisitos del Backend.
 */
public class ComentarioRequest {
    private final String contenido;

    @SerializedName("usuario")
    private final UsuarioRequest usuario;

    public ComentarioRequest(String contenido, Long idUsuario) {
        this.contenido = contenido;
        this.usuario = new UsuarioRequest(idUsuario);
    }

    public String getContenido() { return contenido; }
    public UsuarioRequest getUsuario() { return usuario; }

    /**
     * Mini-clase interna para representar el objeto Usuario que espera el backend.
     */
    public static class UsuarioRequest {
        public Long id;
        public UsuarioRequest(Long id) {
            this.id = id;
        }
    }
}

package com.huerteando.app.clases;

import com.google.gson.annotations.SerializedName;

/**
 * Clase que representa un comentario sincronizada con el Backend.
 */
public class Comentario {
    private Long id;
    private String contenido;

    @SerializedName("creadoEn")
    private String creadoEn; // Formato ISO

    @SerializedName("usuario")
    private UsuarioResponse usuario; // El backend devuelve el objeto usuario anidado

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getCreadoEn() { return creadoEn; }
    public void setCreadoEn(String creadoEn) { this.creadoEn = creadoEn; }

    // Helpers para obtener datos del autor sin romper la lógica del Adapter
    public String getAutorNick() {
        return (usuario != null) ? usuario.nick : "Anónimo";
    }

    public String getAutorAvatarUrl() {
        return (usuario != null) ? usuario.avatarUrl : null;
    }

    /**
     * Clase interna para mapear el objeto usuario del JSON.
     */
    private static class UsuarioResponse {
        public String nick;
        public String avatarUrl;
    }
}

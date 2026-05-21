package com.huerteando.app.clases;

public class Comentario {
    private Long id;
    private String contenido;
    private String creadoEn;
    private Usuario usuario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public String getCreadoEn() { return creadoEn; }
    public void setCreadoEn(String creadoEn) { this.creadoEn = creadoEn; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    // Helpers para compatibilidad con Adapters
    public String getAutorNick() {
        return (usuario != null) ? usuario.getNick() : "Anónimo";
    }
    public String getAutorAvatarUrl() {
        return (usuario != null) ? usuario.getAvatarUrl() : null;
    }
    public Long getUsuarioId() {
        return (usuario != null) ? usuario.getId() : null;
    }
}
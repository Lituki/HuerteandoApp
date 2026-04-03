package com.huerteando.app.clases;

public class Comentario {
    private Long id;
    private String contenido;
    private String creadoEn;
    private String autorNick;
    private String autorAvatarUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(String creadoEn) {
        this.creadoEn = creadoEn;
    }

    public String getAutorNick() {
        return autorNick;
    }

    public void setAutorNick(String autorNick) {
        this.autorNick = autorNick;
    }

    public String getAutorAvatarUrl() {
        return autorAvatarUrl;
    }

    public void setAutorAvatarUrl(String autorAvatarUrl) {
        this.autorAvatarUrl = autorAvatarUrl;
    }
}
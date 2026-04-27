package com.huerteando.app.clases;

/**
 * Modelo para enviar y recibir imágenes del servidor.
 * Sincronizado con la entidad Imagen del Backend.
 */
public class Imagen {
    private Long id;
    private String urlArchivo; // Aquí viajará el String Base64 en el POST
    private String titulo;
    private String creadoEn;

    public Imagen() {}

    public Imagen(String urlArchivo, String titulo) {
        this.urlArchivo = urlArchivo;
        this.titulo = titulo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUrlArchivo() { return urlArchivo; }
    public void setUrlArchivo(String urlArchivo) { this.urlArchivo = urlArchivo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getCreadoEn() { return creadoEn; }
    public void setCreadoEn(String creadoEn) { this.creadoEn = creadoEn; }
}

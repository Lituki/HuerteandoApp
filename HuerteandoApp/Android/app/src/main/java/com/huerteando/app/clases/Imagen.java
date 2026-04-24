package com.huerteando.app.clases;

/**
 * Modelo para enviar imágenes al servidor según el manual.
 */
public class Imagen {
    private String urlArchivo;
    private String titulo;

    public Imagen() {}

    public Imagen(String urlArchivo, String titulo) {
        this.urlArchivo = urlArchivo;
        this.titulo = titulo;
    }

    public String getUrlArchivo() { return urlArchivo; }
    public void setUrlArchivo(String urlArchivo) { this.urlArchivo = urlArchivo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
}

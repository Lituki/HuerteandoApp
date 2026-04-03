package com.huerteando.app.clases;

import java.util.List;

// Esta clase representa una observación del servidor (JSON → Java)
public class Observacion {

    private Long id;
    private String titulo;
    private String descripcion;
    private String fechaObservacion;  // Formato ISO: "2024-05-01"
    private String estado;            // "ABIERTA", "CERRADA"
    private String nombreTradicional; // Nombre popular huertano (opcional)

    // Ubicación — van en la misma tabla según los requisitos
    private double latitud;
    private double longitud;
    private String direccionTxt;
    private String nombreZona;        // Topónimo local

    // Relaciones
    private String tipoObservacion;   // "PLANTA", "RINCON", "DENUNCIA"
    private String especieNombre;     // Puede ser null si no se identificó
    private String autorNick;         // Quién la creó
    private String autorAvatarUrl;

    // Contadores para mostrar en la tarjeta
    private int numLikes;
    private int numComentarios;
    private boolean likePropio;       // Si el usuario actual ya le dio like

    // Lista de imágenes (máximo 3 según requisitos)
    private List<String> imagenesUrl;

    // Getters y Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaObservacion() {
        return fechaObservacion;
    }

    public void setFechaObservacion(String fechaObservacion) {
        this.fechaObservacion = fechaObservacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombreTradicional() {
        return nombreTradicional;
    }

    public void setNombreTradicional(String nombreTradicional) {
        this.nombreTradicional = nombreTradicional;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getDireccionTxt() {
        return direccionTxt;
    }

    public void setDireccionTxt(String direccionTxt) {
        this.direccionTxt = direccionTxt;
    }

    public String getNombreZona() {
        return nombreZona;
    }

    public void setNombreZona(String nombreZona) {
        this.nombreZona = nombreZona;
    }

    public String getTipoObservacion() {
        return tipoObservacion;
    }

    public void setTipoObservacion(String tipoObservacion) {
        this.tipoObservacion = tipoObservacion;
    }

    public String getEspecieNombre() {
        return especieNombre;
    }

    public void setEspecieNombre(String especieNombre) {
        this.especieNombre = especieNombre;
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

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    public int getNumComentarios() {
        return numComentarios;
    }

    public void setNumComentarios(int numComentarios) {
        this.numComentarios = numComentarios;
    }

    public boolean isLikePropio() {
        return likePropio;
    }

    public void setLikePropio(boolean likePropio) {
        this.likePropio = likePropio;
    }

    public List<String> getImagenesUrl() {
        return imagenesUrl;
    }

    public void setImagenesUrl(List<String> imagenesUrl) {
        this.imagenesUrl = imagenesUrl;
    }
}
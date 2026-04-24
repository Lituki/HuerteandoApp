package com.huerteando.app.clases;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Esta clase representa una observación del servidor (JSON → Java)
public class Observacion {

    private Long id;
    private String titulo;
    private String descripcion;

    @SerializedName("fechaObservacion")
    private String fechaObservacion;  // Formato ISO: "2024-05-01T00:00:00"

    @SerializedName("estadoObservacion")
    private String estado;            // "ABIERTA", "CERRADA"

    @SerializedName("nombreTradicional")
    private String nombreTradicional; // Nombre popular huertano (opcional)

    private double latitud;
    private double longitud;

    @SerializedName("direccionTxt")
    private String direccionTxt;

    @SerializedName("nombreZona")
    private String nombreZona;        // Topónimo local

    // Relaciones
    @SerializedName("tipoObservacion")
    private TipoResponse tipoRaw;     // El backend devuelve un objeto TipoObservacion

    @SerializedName("especieNombre")
    private String especieNombre;     // Puede ser null si no se identificó

    @SerializedName("usuario")
    private UsuarioResponse usuario;   // Quién la creó

    @SerializedName("numLikes")
    private int numLikes;

    @SerializedName("numComentarios")
    private int numComentarios;

    @SerializedName("likePropio")
    private boolean likePropio;       // Si el usuario actual ya le dio like

    @SerializedName("imagenesUrl")
    private List<String> imagenesUrl;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFechaObservacion() { return fechaObservacion; }
    public void setFechaObservacion(String fechaObservacion) { this.fechaObservacion = fechaObservacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getNombreTradicional() { return nombreTradicional; }
    public void setNombreTradicional(String nombreTradicional) { this.nombreTradicional = nombreTradicional; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public String getDireccionTxt() { return direccionTxt; }
    public void setDireccionTxt(String direccionTxt) { this.direccionTxt = direccionTxt; }
    public String getNombreZona() { return nombreZona; }
    public void setNombreZona(String nombreZona) { this.nombreZona = nombreZona; }

    // Helper para obtener el nombre del tipo desde el objeto raw
    public String getTipoObservacion() {
        return (tipoRaw != null) ? tipoRaw.nombre : "Desconocido";
    }

    public String getEspecieNombre() { return especieNombre; }
    public void setEspecieNombre(String especieNombre) { this.especieNombre = especieNombre; }

    // Helper para obtener el nick del autor desde el objeto usuario
    public String getAutorNick() {
        return (usuario != null) ? usuario.nick : "Anónimo";
    }

    public String getAutorAvatarUrl() {
        return (usuario != null) ? usuario.avatarUrl : null;
    }

    public int getNumLikes() { return numLikes; }
    public void setNumLikes(int numLikes) { this.numLikes = numLikes; }
    public int getNumComentarios() { return numComentarios; }
    public void setNumComentarios(int numComentarios) { this.numComentarios = numComentarios; }
    public boolean isLikePropio() { return likePropio; }
    public void setLikePropio(boolean likePropio) { this.likePropio = likePropio; }
    public List<String> getImagenesUrl() { return imagenesUrl; }
    public void setImagenesUrl(List<String> imagenesUrl) { this.imagenesUrl = imagenesUrl; }

    /**
     * Clases internas para mapear objetos anidados del JSON del backend
     */
    private static class TipoResponse {
        public String nombre;
    }

    private static class UsuarioResponse {
        public String nick;
        public String avatarUrl;
    }
}

package com.huerteando.app.clases;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Esta clase representa una observación del servidor (JSON → Java)
public class Observacion {

    private Long id;
    private String titulo;
    private String descripcion;

    @SerializedName("fecha_observacion")
    private String fechaObservacion;  // Formato ISO: "2024-05-01"

    @SerializedName("estado_observacion")
    private String estado;            // "ABIERTA", "CERRADA"

    @SerializedName("nombre_tradicional")
    private String nombreTradicional; // Nombre popular huertano (opcional)


    // Ubicación — van en la misma tabla según los requisitos
    private double latitud;
    private double longitud;

    @SerializedName("direccion_txt")
    private String direccionTxt;

    @SerializedName("nombre_zona")
    private String nombreZona;        // Topónimo local

    // Relaciones
    @SerializedName("tipo_observacion")
    private String tipoObservacion;   // "PLANTA", "RINCON", "DENUNCIA"

    @SerializedName("especie_nombre")
    private String especieNombre;     // Puede ser null si no se identificó

    @SerializedName("autor_nick")
    private String autorNick;         // Quién la creó

    @SerializedName("autor_avatar_url")
    private String autorAvatarUrl;

    // Contadores para mostrar en la tarjeta
    @SerializedName("num_likes")
    private int numLikes;

    @SerializedName("num_comentarios")
    private int numComentarios;

    @SerializedName("like_propio")
    private boolean likePropio;       // Si el usuario actual ya le dio like

    //Lista de imagenes (URL)
    @SerializedName("imagenes_url")
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
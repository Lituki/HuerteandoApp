package com.huerteando.app.clases;

/**
 * Clase para crear o actualizar una observación.
 * Se envía al servidor cuando el usuario crea o edita una observación.
 */
import com.google.gson.annotations.SerializedName;

public class ObservacionRequest {
    private final String titulo;
    private final String descripcion;

    @SerializedName("fecha_observacion")
    private final String fechaObservacion;

    @SerializedName("tipo_observacion")
    private final String tipoObservacion;

    @SerializedName("especie_nombre")
    private final String especieNombre;

    private final double latitud;
    private final double longitud;

    @SerializedName("direccion_txt")
    private final String direccionTxt;

    @SerializedName("nombre_zona")
    private final String nombreZona;

    @SerializedName("nombre_tradicional")
    private final String nombreTradicional;

    public ObservacionRequest(String titulo, String descripcion, String fechaObservacion,
                              String tipoObservacion, String especieNombre,
                              double latitud, double longitud, String direccionTxt,
                              String nombreZona, String nombreTradicional) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaObservacion = fechaObservacion;
        this.tipoObservacion = tipoObservacion;
        this.especieNombre = especieNombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccionTxt = direccionTxt;
        this.nombreZona = nombreZona;
        this.nombreTradicional = nombreTradicional;
    }

    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getFechaObservacion() { return fechaObservacion; }
    public String getTipoObservacion() { return tipoObservacion; }
    public String getEspecieNombre() { return especieNombre; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getDireccionTxt() { return direccionTxt; }
    public String getNombreZona() { return nombreZona; }
    public String getNombreTradicional() { return nombreTradicional; }
}

package com.huerteando.app.clases;

/**
 * Clase para crear o actualizar una observación.
 * Se envía al servidor cuando el usuario crea o edita una observación.
 */
public class ObservacionRequest {
    private String titulo;
    private String descripcion;
    private String fechaObservacion;  // Formato: "2024-05-01"
    private String tipoObservacion;   // PLANTA, RINCON, DENUNCIA
    private String especieNombre;     // Opcional
    private double latitud;
    private double longitud;
    private String direccionTxt;
    private String nombreZona;
    private String nombreTradicional; // Opcional

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

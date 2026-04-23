package com.huerteando.app.clases;

/**
 * Clase para crear o actualizar una observación.
 * Se envía al servidor cuando el usuario crea o edita una observación.
 */
public class ObservacionRequest {
    private final String titulo;
    private final String descripcion;
    private final String fechaObservacion;  // Formato: "2024-05-01"
    private final String tipoObservacion;   // PLANTA, RINCON, DENUNCIA
    private final String especieNombre;     // Opcional
    private final double latitud;
    private final double longitud;
    private final String direccionTxt;
    private final String nombreZona;
    private final String nombreTradicional; // Opcional

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

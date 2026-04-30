package com.huerteando.app.clases;
import com.google.gson.annotations.SerializedName;
/**
 * Clase para crear o actualizar una observación.
 * Sincronizada con los tipos de datos del Backend y el esquema SQL.
 */
public class ObservacionRequest {
    private final String titulo;
    private final String descripcion;

    @SerializedName("fechaObservacion")
    private final String fechaObservacion;

    @SerializedName("tipoObservacion")
    private final TipoRequest tipoObservacion;

    @SerializedName("usuario")
    private final UsuarioRequest usuario;

    @SerializedName("especieNombre")
    private final String especieNombre;

    // Cambiado de BigDecimal a double para compatibilidad con Mapas y JSON simple
    private final double latitud;
    private final double longitud;

    @SerializedName("direccionTxt")
    private final String direccionTxt;

    @SerializedName("nombreZona")
    private final String nombreZona;

    @SerializedName("nombreTradicional")
    private final String nombreTradicional;

    @SerializedName("estadoObservacion")
    private final String estadoObservacion;

    public ObservacionRequest(String titulo, String descripcion, String fechaObservacion,
                              TipoRequest tipoObservacion, String especieNombre,
                              double latitud, double longitud, String direccionTxt,
                              String nombreZona, String nombreTradicional,
                              UsuarioRequest usuario, String estado) {
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
        this.usuario = usuario;
        this.estadoObservacion = (estado != null) ? estado : "ABIERTA";
    }

    // Clases internas para el mapeo de IDs que espera el Controller de Spring
    public static class TipoRequest {
        public int id;
        public TipoRequest(int id) { this.id = id; }
    }

    public static class UsuarioRequest {
        public Long id;
        public UsuarioRequest(Long id) { this.id = id; }
    }
}

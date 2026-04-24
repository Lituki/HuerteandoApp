package com.huerteando.app.clases;

/**
 * Clase para crear o actualizar una observación.
 * Se envía al servidor cuando el usuario crea o edita una observación.
 */
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

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

    private final BigDecimal latitud;
    private final BigDecimal longitud;

    @SerializedName("direccionTxt")
    private final String direccionTxt;

    @SerializedName("nombreZona")
    private final String nombreZona;

    @SerializedName("nombreTradicional")
    private final String nombreTradicional;

    @SerializedName("estadoObservacion")
    private final String estadoObservacion = "ABIERTA";

    public ObservacionRequest(String titulo, String descripcion, String fechaObservacion,
                              TipoRequest tipoObservacion, String especieNombre,
                              BigDecimal latitud, BigDecimal longitud, String direccionTxt,
                              String nombreZona, String nombreTradicional,
                              UsuarioRequest usuario) {
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
    }

    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getFechaObservacion() { return fechaObservacion; }
    public TipoRequest getTipoObservacion() { return tipoObservacion; }
    public UsuarioRequest getUsuario() { return usuario; }
    public String getEspecieNombre() { return especieNombre; }
    public BigDecimal getLatitud() { return latitud; }
    public BigDecimal getLongitud() { return longitud; }
    public String getDireccionTxt() { return direccionTxt; }
    public String getNombreZona() { return nombreZona; }
    public String getNombreTradicional() { return nombreTradicional; }

    /**
     * Mini-clase interna para representar el objeto TipoObservacion que espera el backend.
     */
    public static class TipoRequest {
        public int id;
        public TipoRequest(int id) {
            this.id = id;
        }
    }

    /**
     * Mini-clase interna para representar el objeto Usuario que espera el backend.
     */
    public static class UsuarioRequest {
        public Long id;
        public UsuarioRequest(Long id) {
            this.id = id;
        }
    }
}

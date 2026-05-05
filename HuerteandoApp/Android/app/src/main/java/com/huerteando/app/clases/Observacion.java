package com.huerteando.app.clases;

import java.util.List;

public class Observacion {
    private Long id;
    private String titulo;
    private String descripcion;
    private String estadoObservacion;
    private String nombreTradicional;
    private String identificacionPropuesta;
    private double latitud;
    private double longitud;
    private String direccionTxt;
    private String nombreZona;
    private String fechaObservacion;
    private String creadoEn;
    private TipoObservacion tipoObservacion;
    private Usuario usuario;
    private Especie especie;
    private List<Imagen> imagenes;
    
    // Me gusta logic (local app support)
    private int numMeGustas;
    private boolean meGustaPropio;
    private int numComentarios;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstadoObservacion() { return estadoObservacion; }
    public void setEstadoObservacion(String estadoObservacion) { this.estadoObservacion = estadoObservacion; }
    public String getNombreTradicional() { return nombreTradicional; }
    public void setNombreTradicional(String nombreTradicional) { this.nombreTradicional = nombreTradicional; }
    public String getIdentificacionPropuesta() { return identificacionPropuesta; }
    public void setIdentificacionPropuesta(String identificacionPropuesta) { this.identificacionPropuesta = identificacionPropuesta; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public String getDireccionTxt() { return direccionTxt; }
    public void setDireccionTxt(String direccionTxt) { this.direccionTxt = direccionTxt; }
    public String getNombreZona() { return nombreZona; }
    public void setNombreZona(String nombreZona) { this.nombreZona = nombreZona; }
    public String getFechaObservacion() { return fechaObservacion; }
    public void setFechaObservacion(String fechaObservacion) { this.fechaObservacion = fechaObservacion; }
    public String getCreadoEn() { return creadoEn; }
    public void setCreadoEn(String creadoEn) { this.creadoEn = creadoEn; }
    public TipoObservacion getTipoObservacion() { return tipoObservacion; }
    public void setTipoObservacion(TipoObservacion tipoObservacion) { this.tipoObservacion = tipoObservacion; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }
    public List<Imagen> getImagenes() { return imagenes; }
    public void setImagenes(List<Imagen> imagenes) { this.imagenes = imagenes; }
    
    public int getNumMeGustas() { return numMeGustas; }
    public void setNumMeGustas(int numMeGustas) { this.numMeGustas = numMeGustas; }
    public boolean isMeGustaPropio() { return meGustaPropio; }
    public void setMeGustaPropio(boolean meGustaPropio) { this.meGustaPropio = meGustaPropio; }
    public int getNumComentarios() { return numComentarios; }
    public void setNumComentarios(int numComentarios) { this.numComentarios = numComentarios; }
}
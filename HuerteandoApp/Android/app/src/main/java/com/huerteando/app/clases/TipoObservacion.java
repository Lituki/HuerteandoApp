package com.huerteando.app.clases;

/**
 * Modelo para los tipos de observación (Catálogo).
 */
public class TipoObservacion {
    private Integer id;
    private String nombre;
    private String descripcion;

    public TipoObservacion() {}

    public TipoObservacion(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return nombre; // Útil para mostrar en Spinners directamente
    }
}

package com.huerteando.app.clases;

/**
 * Modelo para las especies (Catálogo).
 */
public class Especie {
    private Long id;
    private String nombreCientifico;
    private String nombreComun;
    private String familia;

    public Especie() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCientifico() { return nombreCientifico; }
    public void setNombreCientifico(String nombreCientifico) { this.nombreCientifico = nombreCientifico; }

    public String getNombreComun() { return nombreComun; }
    public void setNombreComun(String nombreComun) { this.nombreComun = nombreComun; }

    public String getFamilia() { return familia; }
    public void setFamilia(String familia) { this.familia = familia; }

    @Override
    public String toString() {
        return nombreComun != null ? nombreComun : nombreCientifico;
    }
}

package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;

/*
Tabla: catalogo_eei
Referencia oficial de especies invasoras (EEI).
Se usa para contrastar lo que se observa en campo.
*/
@Entity
@Table(name = "catalogo_eei")
public class CatalogoEei {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_catalogo_eei")
    private Long id; // id interno

    @Column(name = "nombre_cientifico", nullable = false, unique = true)
    private String nombreCientifico; // clave unica del catalogo

    @Column(name = "nombre_comun")
    private String nombreComun;

    @Column(name = "reino")
    private String reino;

    @Column(name = "familia")
    private String familia;

    @Column(name = "normativa_ref")
    private String normativaRef; // norma legal asociada

    @Column(name = "fecha_actualizacion")
    private java.time.LocalDateTime fechaActualizacion;

    public CatalogoEei() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCientifico() { return nombreCientifico; }
    public void setNombreCientifico(String nombreCientifico) { this.nombreCientifico = nombreCientifico; }

    public String getNombreComun() { return nombreComun; }
    public void setNombreComun(String nombreComun) { this.nombreComun = nombreComun; }

    public String getReino() { return reino; }
    public void setReino(String reino) { this.reino = reino; }

    public String getFamilia() { return familia; }
    public void setFamilia(String familia) { this.familia = familia; }

    public String getNormativaRef() { return normativaRef; }
    public void setNormativaRef(String normativaRef) { this.normativaRef = normativaRef; }

    public java.time.LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(java.time.LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}

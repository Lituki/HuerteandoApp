package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
Tabla: especie
Aqui guardamos las especies que se van observando.
Luego se comparan con EEI y, si toca, se enlazan por id_catalogo_eei.
*/
@Entity
@Table(name = "especie")
public class Especie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especie")
    private Long id; // id interno

    @Column(name = "nombre_cientifico", nullable = false, unique = true)
    private String nombreCientifico; // nombre unico para evitar duplicados

    @Column(name = "nombre_comun")
    private String nombreComun;

    @Column(name = "familia")
    private String familia;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_eei")
    private CatalogoEei catalogoEei; // null si aun no se ha ligado al catalogo EEI

    public Especie() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCientifico() { return nombreCientifico; }
    public void setNombreCientifico(String nombreCientifico) { this.nombreCientifico = nombreCientifico; }

    public String getNombreComun() { return nombreComun; }
    public void setNombreComun(String nombreComun) { this.nombreComun = nombreComun; }

    public String getFamilia() { return familia; }
    public void setFamilia(String familia) { this.familia = familia; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public CatalogoEei getCatalogoEei() { return catalogoEei; }
    public void setCatalogoEei(CatalogoEei catalogoEei) { this.catalogoEei = catalogoEei; }
}

package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;

/*
Tabla: tipo_observacion
Catalogo corto de tipos (Planta, Rincon, Incidencia).
*/
@Entity
@Table(name = "tipo_observacion")
public class TipoObservacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_observacion")
    private Short id; // id pequeno (smallint)

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre; // etiqueta visible

    public TipoObservacion() {}

    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}

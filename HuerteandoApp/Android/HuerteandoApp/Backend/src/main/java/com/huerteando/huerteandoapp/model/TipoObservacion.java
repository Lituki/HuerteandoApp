package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
Tabla: tipo_observacion
Catálogo corto de tipos (Planta, Rincón, Incidencia).
*/
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "tipo_observacion")
public class TipoObservacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Short id; // smallint, son pocos tipos

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;
}

package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/*
Tabla: especie
Especies que se van observando.
Se comparan con el catálogo EEI para detectar invasoras.
*/
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "especie")
public class Especie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especie")
    private Long id;

    @Column(name = "nombre_cientifico", nullable = false, unique = true)
    private String nombreCientifico; // único para evitar duplicados

    @Column(name = "nombre_comun")
    private String nombreComun;

    @Column(name = "familia")
    private String familia;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @OneToOne
    @JoinColumn(name = "id_eei", unique = true)
    private CatalogoEei catalogoEei; // null si no está en el catálogo de invasoras
}

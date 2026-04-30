package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/*
Tabla: catalogo_eei
Referencia oficial de especies invasoras (EEI).
Se usa para contrastar lo que se observa en campo.
*/
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "catalogo_eei")
public class CatalogoEei {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_eei")
    private Long id;

    @Column(name = "nombre_cientifico", nullable = false, unique = true)
    private String nombreCientifico; // clave única del catálogo

    @Column(name = "nombre_comun")
    private String nombreComun;

    @Column(name = "reino")
    private String reino;

    @Column(name = "familia")
    private String familia;

    @Column(name = "normativa_ref", columnDefinition = "text")
    private String normativaRef; // norma legal asociada

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}

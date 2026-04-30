package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/*
Tabla: comentario
Comentario simple: quién comenta, en qué observación y qué dice.
*/
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "comentario")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comentario")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_observacion", nullable = false)
    private Observacion observacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "contenido", nullable = false, columnDefinition = "text")
    private String contenido;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "editado_en")
    private LocalDateTime editadoEn;

    @PrePersist
    void alCrear() { creadoEn = LocalDateTime.now(); }
}

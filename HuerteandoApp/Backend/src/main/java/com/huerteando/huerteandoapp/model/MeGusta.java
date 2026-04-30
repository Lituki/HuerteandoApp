package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/*
Tabla: like
Like de un usuario a una observación.
Usamos id propio para evitar claves compuestas.
*/
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "megusta")
public class MeGusta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_megusta")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_observacion", nullable = false)
    private Observacion observacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void alCrear() { creadoEn = LocalDateTime.now(); }
}

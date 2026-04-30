package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/*
Tabla: imagen
Solo guardamos metadatos: URL y título.
El archivo real vive fuera de la BD.
*/
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "imagen")
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_observacion", nullable = false)
    private Observacion observacion;

    @Column(name = "url_archivo", nullable = false, columnDefinition = "text")
    private String urlArchivo;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void alCrear() { creadoEn = LocalDateTime.now(); }
}

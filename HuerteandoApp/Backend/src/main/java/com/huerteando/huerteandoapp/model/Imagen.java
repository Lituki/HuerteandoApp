package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/*
Tabla: imagen
Aqui solo guardamos metadatos: URL y titulo.
El archivo real vive fuera de la BD.
*/
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

    @Column(name = "url_archivo", nullable = false)
    private String urlArchivo;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public Imagen() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Observacion getObservacion() { return observacion; }
    public void setObservacion(Observacion observacion) { this.observacion = observacion; }

    public String getUrlArchivo() { return urlArchivo; }
    public void setUrlArchivo(String urlArchivo) { this.urlArchivo = urlArchivo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}

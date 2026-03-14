package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
Tabla: comentario
Comentario simple: quien comenta, donde comenta y que dice.
*/
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

    @Column(name = "contenido", nullable = false)
    private String contenido;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "editado_en")
    private LocalDateTime editadoEn;

    public Comentario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Observacion getObservacion() { return observacion; }
    public void setObservacion(Observacion observacion) { this.observacion = observacion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getEditadoEn() { return editadoEn; }
    public void setEditadoEn(LocalDateTime editadoEn) { this.editadoEn = editadoEn; }
}

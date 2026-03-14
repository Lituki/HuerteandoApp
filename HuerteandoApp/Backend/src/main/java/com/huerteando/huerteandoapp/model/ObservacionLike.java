package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
Tabla: observacion_like
Like de usuario a observacion.
Tenemos id propio para no pelear con claves compuestas.
*/
@Entity
@Table(name = "observacion_like")
public class ObservacionLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_like")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_observacion", nullable = false)
    private Observacion observacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public ObservacionLike() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Observacion getObservacion() { return observacion; }
    public void setObservacion(Observacion observacion) { this.observacion = observacion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}

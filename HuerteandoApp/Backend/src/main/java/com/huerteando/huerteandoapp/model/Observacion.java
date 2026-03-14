package com.huerteando.huerteandoapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



/*
Tabla: observacion
Es la entidad central: aqui cae todo lo observado.
Lleva ubicacion y se relaciona con imagenes, comentarios y likes.
*/
@Entity
@Table(name = "observacion")
public class Observacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_observacion")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_tipo_observacion", nullable = false)
    private TipoObservacion tipoObservacion;

    @ManyToOne
    @JoinColumn(name = "id_especie")
    private Especie especie; // puede ir null mientras se identifica

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_observacion", nullable = false)
    private LocalDateTime fechaObservacion;

    @Column(name = "estado", nullable = false)
    private String estado = "ABIERTA";

    @Column(name = "nombre_tradicional")
    private String nombreTradicional;

    @Column(name = "identificacion_propuesta")
    private String identificacionPropuesta;

    @Column(name = "latitud", nullable = false)
    private BigDecimal latitud;

    @Column(name = "longitud", nullable = false)
    private BigDecimal longitud;

    @Column(name = "direccion_txt")
    private String direccionTxt;

    @Column(name = "nombre_zona")
    private String nombreZona;

    @Column(name = "estado_identificacion")
    private String estadoIdentificacion;

    @Column(name = "fuente_identificacion")
    private String fuenteIdentificacion;

    @Column(name = "confianza_ia")
    private BigDecimal confianzaIa;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @OneToMany(mappedBy = "observacion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comentario> comentarios = new ArrayList<>();

    @OneToMany(mappedBy = "observacion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Imagen> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "observacion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ObservacionLike> likes = new ArrayList<>();

    public Observacion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public TipoObservacion getTipoObservacion() { return tipoObservacion; }
    public void setTipoObservacion(TipoObservacion tipoObservacion) { this.tipoObservacion = tipoObservacion; }

    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaObservacion() { return fechaObservacion; }
    public void setFechaObservacion(LocalDateTime fechaObservacion) { this.fechaObservacion = fechaObservacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNombreTradicional() { return nombreTradicional; }
    public void setNombreTradicional(String nombreTradicional) { this.nombreTradicional = nombreTradicional; }

    public String getIdentificacionPropuesta() { return identificacionPropuesta; }
    public void setIdentificacionPropuesta(String identificacionPropuesta) { this.identificacionPropuesta = identificacionPropuesta; }

    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }

    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }

    public String getDireccionTxt() { return direccionTxt; }
    public void setDireccionTxt(String direccionTxt) { this.direccionTxt = direccionTxt; }

    public String getNombreZona() { return nombreZona; }
    public void setNombreZona(String nombreZona) { this.nombreZona = nombreZona; }

    public String getEstadoIdentificacion() { return estadoIdentificacion; }
    public void setEstadoIdentificacion(String estadoIdentificacion) { this.estadoIdentificacion = estadoIdentificacion; }

    public String getFuenteIdentificacion() { return fuenteIdentificacion; }
    public void setFuenteIdentificacion(String fuenteIdentificacion) { this.fuenteIdentificacion = fuenteIdentificacion; }

    public BigDecimal getConfianzaIa() { return confianzaIa; }
    public void setConfianzaIa(BigDecimal confianzaIa) { this.confianzaIa = confianzaIa; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public List<Comentario> getComentarios() { return comentarios; }
    public void setComentarios(List<Comentario> comentarios) { this.comentarios = comentarios; }

    public List<Imagen> getImagenes() { return imagenes; }
    public void setImagenes(List<Imagen> imagenes) { this.imagenes = imagenes; }

    public List<ObservacionLike> getLikes() { return likes; }
    public void setLikes(List<ObservacionLike> likes) { this.likes = likes; }
}

package com.huerteando.huerteandoapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
Tabla: observacion
Entidad central del sistema. Recoge todo lo observado en la huerta.
Lleva ubicación y se relaciona con imágenes, comentarios y likes.
*/
@Getter @Setter @NoArgsConstructor
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
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoObservacion tipoObservacion;

    @ManyToOne
    @JoinColumn(name = "id_especie")
    private Especie especie; // puede ser null mientras se identifica

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "fecha_observacion", nullable = false)
    private LocalDateTime fechaObservacion;

    @Column(name = "estado_observacion", nullable = false)
    private String estadoObservacion = "ABIERTA";

    @Column(name = "nombre_tradicional")
    private String nombreTradicional; // nombre popular local de la planta o lugar

    @Column(name = "identificacion_propuesta")
    private String identificacionPropuesta; // texto libre si no se conoce la especie

    @Column(name = "latitud", nullable = false)
    private BigDecimal latitud;

    @Column(name = "longitud", nullable = false)
    private BigDecimal longitud;

    @Column(name = "direccion_txt", columnDefinition = "text")
    private String direccionTxt;

    @Column(name = "nombre_zona")
    private String nombreZona;

    @Column(name = "estado_identificacion")
    private String estadoIdentificacion;

    @Column(name = "fuente_identificacion")
    private String fuenteIdentificacion;

    @Column(name = "confianza_ia")
    private BigDecimal confianzaIa; // reservado para integración futura con PlantNet

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    // Rellena las fechas automáticamente al crear y actualizar
    @PrePersist
    void alCrear() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = LocalDateTime.now();
    }

    @JsonIgnore
    @OneToMany(mappedBy = "observacion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "observacion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Imagen> imagenes = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "observacion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeGusta> likes = new ArrayList<>();
}

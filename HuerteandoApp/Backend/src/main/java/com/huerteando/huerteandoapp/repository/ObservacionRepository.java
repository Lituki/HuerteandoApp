package com.huerteando.huerteandoapp.repository;

import com.huerteando.huerteandoapp.model.Observacion;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ObservacionRepository extends JpaRepository<Observacion, Long> {

    @EntityGraph(attributePaths = "imagenes", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Observacion> findWithImagenesById(Long id);

    // Redefinimos estos métodos para que, cuando devolvemos Observacion en JSON,
    // la colección imagenes ya venga cargada (open-in-view=false + LAZY).
    @EntityGraph(attributePaths = "imagenes", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Observacion> findById(Long id);

    @EntityGraph(attributePaths = "imagenes", type = EntityGraph.EntityGraphType.LOAD)
    List<Observacion> findAll();

    // Observaciones de un usuario.
    @EntityGraph(attributePaths = "imagenes", type = EntityGraph.EntityGraphType.LOAD)
    List<Observacion> findByUsuario_IdOrderByCreadoEnDesc(Long idUsuario);

    // Observaciones por tipo.
    @EntityGraph(attributePaths = "imagenes", type = EntityGraph.EntityGraphType.LOAD)
    List<Observacion> findByTipoObservacion_IdOrderByFechaObservacionDesc(Long idTipoObservacion);

    // Observaciones de una especie concreta.
    @EntityGraph(attributePaths = "imagenes", type = EntityGraph.EntityGraphType.LOAD)
    List<Observacion> findByEspecie_IdOrderByFechaObservacionDesc(Long idEspecie);

    // Observaciones en un estado concreto (ABIERTA, CERRADA, etc).
    @EntityGraph(attributePaths = "imagenes", type = EntityGraph.EntityGraphType.LOAD)
    List<Observacion> findByEstadoObservacionIgnoreCaseOrderByActualizadoEnDesc(String estadoObservacion);

    // Últimas observaciones creadas.
    @EntityGraph(attributePaths = "imagenes", type = EntityGraph.EntityGraphType.LOAD)
    List<Observacion> findTop20ByOrderByCreadoEnDesc();
}

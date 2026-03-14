package com.huerteando.huerteandoapp.repository;

import com.huerteando.huerteandoapp.model.Observacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObservacionRepository extends JpaRepository<Observacion, Long> {

    // Observaciones de un usuario.
    List<Observacion> findByUsuario_IdOrderByCreadoEnDesc(Long idUsuario);

    // Observaciones por tipo.
    List<Observacion> findByTipoObservacion_IdOrderByFechaObservacionDesc(Long idTipoObservacion);

    // Observaciones de una especie concreta.
    List<Observacion> findByEspecie_IdOrderByFechaObservacionDesc(Long idEspecie);

    // Observaciones en un estado concreto (ABIERTA, CERRADA, etc).
    List<Observacion> findByEstadoIgnoreCaseOrderByActualizadoEnDesc(String estado);

    // Últimas observaciones creadas.
    List<Observacion> findTop20ByOrderByCreadoEnDesc();
}

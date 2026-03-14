package com.huerteando.huerteandoapp.repository;

import com.huerteando.huerteandoapp.model.ObservacionLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ObservacionLikeRepository extends JpaRepository<ObservacionLike, Long> {

    // ¿Este usuario ya dio like a esta observación?
    boolean existsByObservacion_IdAndUsuario_Id(Long idObservacion, Long idUsuario);

    // Cuenta likes de una observación
    long countByObservacion_Id(Long idObservacion);

    // Lista likes de una observación (por fecha)
    List<ObservacionLike> findByObservacion_IdOrderByCreadoEnDesc(Long idObservacion);

    // Lista likes de un usuario (por fecha)
    List<ObservacionLike> findByUsuario_IdOrderByCreadoEnDesc(Long idUsuario);

    // Lista likes desde una fecha (para estadísticas)
    List<ObservacionLike> findByCreadoEnAfter(LocalDateTime desde);

    // Quita el like de un usuario a una observación (unlike)
    long deleteByObservacion_IdAndUsuario_Id(Long idObservacion, Long idUsuario);
}

package com.huerteando.huerteandoapp.repository;

import com.huerteando.huerteandoapp.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    // Lista los comentarios de una observación (antiguos primero)
    List<Comentario> findByObservacion_IdOrderByCreadoEnAsc(Long idObservacion);

    // Lista los comentarios de un usuario (recientes primero)
    List<Comentario> findByUsuario_IdOrderByCreadoEnDesc(Long idUsuario);

    // Cuenta comentarios de una observación
    long countByObservacion_Id(Long idObservacion);

    // Lista comentarios creados después de una fecha
    List<Comentario> findByCreadoEnAfter(LocalDateTime desde);
}

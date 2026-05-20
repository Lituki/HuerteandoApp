package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.ObservacionLike;

import java.time.LocalDateTime;
import java.util.List;

public interface IObservacionLikeService {

    // Da like (si ya existe, no hace nada)
    ObservacionLike darLike(Long idObservacion, Long idUsuario);

    // Quita like (si no existe, no hace nada)
    void quitarLike(Long idObservacion, Long idUsuario);

    // ¿Existe like?
    boolean existeLike(Long idObservacion, Long idUsuario);

    // Cuenta likes de una observación
    long contarLikes(Long idObservacion);

    // Lista likes de una observación
    List<ObservacionLike> listarPorObservacion(Long idObservacion);

    // Lista likes de un usuario
    List<ObservacionLike> listarPorUsuario(Long idUsuario);

    // Lista likes desde una fecha
    List<ObservacionLike> listarDesde(LocalDateTime desde);
}

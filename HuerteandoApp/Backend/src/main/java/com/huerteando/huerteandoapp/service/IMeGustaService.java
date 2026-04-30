package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.MeGusta;

import java.time.LocalDateTime;
import java.util.List;

public interface IMeGustaService {

    // Da like (si ya existe, no hace nada)
    MeGusta darLike(Long idObservacion, Long idUsuario);

    // Quita like (si no existe, no hace nada)
    void quitarLike(Long idObservacion, Long idUsuario);

    // ¿Existe like?
    boolean existeLike(Long idObservacion, Long idUsuario);

    // Cuenta likes de una observación
    long contarLikes(Long idObservacion);

    // Lista likes de una observación
    List<MeGusta> listarPorObservacion(Long idObservacion);

    // Lista likes de un usuario
    List<MeGusta> listarPorUsuario(Long idUsuario);

    // Lista likes desde una fecha
    List<MeGusta> listarDesde(LocalDateTime desde);
}

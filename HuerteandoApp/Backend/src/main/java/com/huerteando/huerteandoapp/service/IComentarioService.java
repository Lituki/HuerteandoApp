package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Comentario;

import java.time.LocalDateTime;
import java.util.List;

public interface IComentarioService {

    // Guarda un comentario
    Comentario guardar(Comentario comentario);

    // Busca un comentario por id
    Comentario buscarPorId(Long idComentario);

    // Lista comentarios de una observación
    List<Comentario> listarPorObservacion(Long idObservacion);

    // Lista comentarios de un usuario
    List<Comentario> listarPorUsuario(Long idUsuario);

    // Cuenta comentarios de una observación
    long contarPorObservacion(Long idObservacion);

    // Lista comentarios desde una fecha
    List<Comentario> listarDesde(LocalDateTime desde);

    // Borra un comentario
    void borrar(Long idComentario);
}

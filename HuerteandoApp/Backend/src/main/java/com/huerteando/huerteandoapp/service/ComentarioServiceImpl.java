package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Comentario;
import com.huerteando.huerteandoapp.repository.ComentarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComentarioServiceImpl implements IComentarioService {

    private final ComentarioRepository comentarioRepository;

    public ComentarioServiceImpl(ComentarioRepository comentarioRepository) {
        this.comentarioRepository = comentarioRepository;
    }

    @Override
    @Transactional
    public Comentario guardar(Comentario comentario) {
        return comentarioRepository.save(comentario);
    }

    @Override
    @Transactional
    public Comentario buscarPorId(Long idComentario) {
        if (idComentario == null) return null;
        return comentarioRepository.findById(idComentario).orElse(null);
    }

    @Override
    @Transactional
    public List<Comentario> listarPorObservacion(Long idObservacion) {
        if (idObservacion == null) return List.of();
        return comentarioRepository.findByObservacion_IdOrderByCreadoEnAsc(idObservacion);
    }

    @Override
    @Transactional
    public List<Comentario> listarPorUsuario(Long idUsuario) {
        if (idUsuario == null) return List.of();
        return comentarioRepository.findByUsuario_IdOrderByCreadoEnDesc(idUsuario);
    }

    @Override
    @Transactional
    public long contarPorObservacion(Long idObservacion) {
        if (idObservacion == null) return 0;
        return comentarioRepository.countByObservacion_Id(idObservacion);
    }

    @Override
    @Transactional
    public List<Comentario> listarDesde(LocalDateTime desde) {
        if (desde == null) return List.of();
        return comentarioRepository.findByCreadoEnAfter(desde);
    }

    @Override
    @Transactional
    public void borrar(Long idComentario) {
        if (idComentario == null) return;
        comentarioRepository.deleteById(idComentario);
    }
}

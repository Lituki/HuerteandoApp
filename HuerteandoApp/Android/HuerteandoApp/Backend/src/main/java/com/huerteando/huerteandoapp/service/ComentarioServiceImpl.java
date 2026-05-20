package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Comentario;
import com.huerteando.huerteandoapp.repository.ComentarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        // save() sirve tanto para crear como para actualizar.
        // Si el comentario tiene id, actualiza. Si no, crea uno nuevo.
        return comentarioRepository.save(comentario);
    }

    @Override
    @Transactional(readOnly = true)
    public Comentario buscarPorId(Long idComentario) {
        if (idComentario == null) return null;
        return comentarioRepository.findById(idComentario).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comentario> listarPorObservacion(Long idObservacion) {
        if (idObservacion == null) return List.of();

        // Ordenados del más antiguo al más nuevo (Asc) para que se lean como una conversación.
        return comentarioRepository.findByObservacion_IdOrderByCreadoEnAsc(idObservacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comentario> listarPorUsuario(Long idUsuario) {
        if (idUsuario == null) return List.of();

        // Los del usuario se ordenan del más reciente al más antiguo (Desc).
        return comentarioRepository.findByUsuario_IdOrderByCreadoEnDesc(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorObservacion(Long idObservacion) {
        if (idObservacion == null) return 0;
        return comentarioRepository.countByObservacion_Id(idObservacion);
    }

    @Override
    @Transactional(readOnly = true)
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

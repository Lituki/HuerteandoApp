package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Observacion;
import com.huerteando.huerteandoapp.model.ObservacionLike;
import com.huerteando.huerteandoapp.model.Usuario;
import com.huerteando.huerteandoapp.repository.ObservacionLikeRepository;
import com.huerteando.huerteandoapp.repository.ObservacionRepository;
import com.huerteando.huerteandoapp.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ObservacionLikeServiceImpl implements IObservacionLikeService {

    private final ObservacionLikeRepository likeRepository;
    private final ObservacionRepository observacionRepository;
    private final UsuarioRepository usuarioRepository;

    public ObservacionLikeServiceImpl(
            ObservacionLikeRepository likeRepository,
            ObservacionRepository observacionRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.likeRepository = likeRepository;
        this.observacionRepository = observacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public ObservacionLike darLike(Long idObservacion, Long idUsuario) {
        if (idObservacion == null || idUsuario == null) return null;

        // Si ya existe, no duplico
        boolean yaExiste = likeRepository.existsByObservacion_IdAndUsuario_Id(idObservacion, idUsuario);
        if (yaExiste) return null;

        // Cargo las FK. Si no existen, devuelvo null y fuera.
        Observacion obs = observacionRepository.findById(idObservacion).orElse(null);
        if (obs == null) return null;

        Usuario usu = usuarioRepository.findById(idUsuario).orElse(null);
        if (usu == null) return null;

        ObservacionLike like = new ObservacionLike();
        like.setObservacion(obs);
        like.setUsuario(usu);
        like.setCreadoEn(LocalDateTime.now());

        return likeRepository.save(like);
    }

    @Override
    @Transactional
    public void quitarLike(Long idObservacion, Long idUsuario) {
        if (idObservacion == null || idUsuario == null) return;
        likeRepository.deleteByObservacion_IdAndUsuario_Id(idObservacion, idUsuario);
    }

    @Override
    @Transactional
    public boolean existeLike(Long idObservacion, Long idUsuario) {
        if (idObservacion == null || idUsuario == null) return false;
        return likeRepository.existsByObservacion_IdAndUsuario_Id(idObservacion, idUsuario);
    }

    @Override
    @Transactional
    public long contarLikes(Long idObservacion) {
        if (idObservacion == null) return 0;
        return likeRepository.countByObservacion_Id(idObservacion);
    }

    @Override
    @Transactional
    public List<ObservacionLike> listarPorObservacion(Long idObservacion) {
        if (idObservacion == null) return List.of();
        return likeRepository.findByObservacion_IdOrderByCreadoEnDesc(idObservacion);
    }

    @Override
    @Transactional
    public List<ObservacionLike> listarPorUsuario(Long idUsuario) {
        if (idUsuario == null) return List.of();
        return likeRepository.findByUsuario_IdOrderByCreadoEnDesc(idUsuario);
    }

    @Override
    @Transactional
    public List<ObservacionLike> listarDesde(LocalDateTime desde) {
        if (desde == null) return List.of();
        return likeRepository.findByCreadoEnAfter(desde);
    }
}

package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Observacion;
import com.huerteando.huerteandoapp.model.MeGusta;
import com.huerteando.huerteandoapp.model.Usuario;
import com.huerteando.huerteandoapp.repository.MeGustaRepository;
import com.huerteando.huerteandoapp.repository.ObservacionRepository;
import com.huerteando.huerteandoapp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

// Este servicio necesita tres repositorios porque al dar like
// tenemos que verificar que tanto la observación como el usuario existen en la BD.
@Service
public class MeGustaServiceImpl implements IMeGustaService {

    private final MeGustaRepository likeRepository;
    private final ObservacionRepository observacionRepository;
    private final UsuarioRepository usuarioRepository;

    public MeGustaServiceImpl(
            MeGustaRepository likeRepository,
            ObservacionRepository observacionRepository,
            UsuarioRepository usuarioRepository) {
        this.likeRepository = likeRepository;
        this.observacionRepository = observacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public MeGusta darLike(Long idObservacion, Long idUsuario) {
        if (idObservacion == null || idUsuario == null) return null;

        // Si ya existe ese like, no lo duplicamos. Devolvemos null para que el controller
        // responda 409 Conflict.
        if (likeRepository.existsByObservacion_IdAndUsuario_Id(idObservacion, idUsuario)) return null;

        // Cargamos los objetos completos desde la BD para que Hibernate los gestione bien.
        // No vale con crear un objeto vacío con solo el id: Hibernate necesita el objeto real.
        Observacion obs = observacionRepository.findById(idObservacion).orElse(null);
        if (obs == null) return null;

        Usuario usu = usuarioRepository.findById(idUsuario).orElse(null);
        if (usu == null) return null;

        // Construimos el like y lo guardamos.
        MeGusta like = new MeGusta();
        like.setObservacion(obs);
        like.setUsuario(usu);
        like.setCreadoEn(LocalDateTime.now());

        return likeRepository.save(like);
    }

    @Override
    @Transactional
    public void quitarLike(Long idObservacion, Long idUsuario) {
        if (idObservacion == null || idUsuario == null) return;

        // deleteBy... borra directamente por los dos campos. Si no existe, no hace nada.
        likeRepository.deleteByObservacion_IdAndUsuario_Id(idObservacion, idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeLike(Long idObservacion, Long idUsuario) {
        if (idObservacion == null || idUsuario == null) return false;
        return likeRepository.existsByObservacion_IdAndUsuario_Id(idObservacion, idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarLikes(Long idObservacion) {
        if (idObservacion == null) return 0;
        return likeRepository.countByObservacion_Id(idObservacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeGusta> listarPorObservacion(Long idObservacion) {
        if (idObservacion == null) return List.of();
        return likeRepository.findByObservacion_IdOrderByCreadoEnDesc(idObservacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeGusta> listarPorUsuario(Long idUsuario) {
        if (idUsuario == null) return List.of();
        return likeRepository.findByUsuario_IdOrderByCreadoEnDesc(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeGusta> listarDesde(LocalDateTime desde) {
        if (desde == null) return List.of();
        return likeRepository.findByCreadoEnAfter(desde);
    }
}

package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Imagen;
import com.huerteando.huerteandoapp.repository.ImagenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImagenServiceImpl implements IImagenService {

    private final ImagenRepository imagenRepository;

    public ImagenServiceImpl(ImagenRepository imagenRepository) {
        this.imagenRepository = imagenRepository;
    }

    @Override
    @Transactional
    public Imagen crear(Imagen imagen) {
        if (imagen == null) return null;

        // Sin observación no tiene sentido.
        if (imagen.getObservacion() == null) return null;

        // Si no hay URL/archivo, tampoco.
        if (imagen.getUrlArchivo() == null || imagen.getUrlArchivo().isBlank()) return null;

        return imagenRepository.save(imagen);
    }

    @Override
    @Transactional
    public Imagen actualizar(Imagen imagen) {
        if (imagen == null) return null;
        if (imagen.getId() == null) return null;

        boolean existe = imagenRepository.existsById(imagen.getId());
        if (!existe) return null;

        return imagenRepository.save(imagen);
    }

    @Override
    @Transactional
    public void eliminar(Long idImagen) {
        if (idImagen == null) return;
        imagenRepository.deleteById(idImagen);
    }

    @Override
    @Transactional(readOnly = true)
    public Imagen buscarPorId(Long idImagen) {
        if (idImagen == null) return null;
        return imagenRepository.findById(idImagen).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> listarTodas() {
        return imagenRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> listarPorObservacion(Long idObservacion) {
        if (idObservacion == null) return List.of();
        return imagenRepository.findByObservacionIdOrderByFecha(idObservacion);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorObservacion(Long idObservacion) {
        if (idObservacion == null) return 0;
        return imagenRepository.countByObservacion_Id(idObservacion);
    }

    @Override
    @Transactional
    public void borrarPorObservacion(Long idObservacion) {
        if (idObservacion == null) return;
        imagenRepository.deleteByObservacion_Id(idObservacion);
    }
}

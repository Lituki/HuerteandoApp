package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Imagen;
import com.huerteando.huerteandoapp.repository.ImagenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ImagenServiceImpl implements IImagenService {

    private final ImagenRepository imagenRepository;
    private final IObservacionService observacionService;
    private final SupabaseStorageService supabaseStorageService;

    public ImagenServiceImpl(
            ImagenRepository imagenRepository,
            IObservacionService observacionService,
            SupabaseStorageService supabaseStorageService) {
        this.imagenRepository = imagenRepository;
        this.observacionService = observacionService;
        this.supabaseStorageService = supabaseStorageService;
    }

  

    @Override
    @Transactional
    public Imagen crear(Imagen imagen) {
        if (imagen == null)
            return null;

        // Sin observación no tiene sentido.
        if (imagen.getObservacion() == null)
            return null;

        // Si no hay URL/archivo, tampoco.
        if (imagen.getUrlArchivo() == null || imagen.getUrlArchivo().isBlank())
            return null;

        return imagenRepository.save(imagen);
    }

    @Override
    @Transactional
    public Imagen actualizar(Imagen imagen) {
        if (imagen == null)
            return null;
        if (imagen.getId() == null)
            return null;

        boolean existe = imagenRepository.existsById(imagen.getId());
        if (!existe)
            return null;

        return imagenRepository.save(imagen);
    }

    @Override
    @Transactional
    public void eliminar(Long idImagen) {
        if (idImagen == null)
            return;
        imagenRepository.deleteById(idImagen);
    }

    @Override
    @Transactional(readOnly = true)
    public Imagen buscarPorId(Long idImagen) {
        if (idImagen == null)
            return null;
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
        if (idObservacion == null)
            return List.of();
        return imagenRepository.findByObservacionIdOrderByFecha(idObservacion);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorObservacion(Long idObservacion) {
        if (idObservacion == null)
            return 0;
        return imagenRepository.countByObservacion_Id(idObservacion);
    }

    @Override
    @Transactional
    public void borrarPorObservacion(Long idObservacion) {
        if (idObservacion == null)
            return;
        imagenRepository.deleteByObservacion_Id(idObservacion);
    }

    @Override
    @Transactional
    public Imagen subirImagen(Long idObservacion, MultipartFile file, String titulo) {
        if (idObservacion == null || file == null || file.isEmpty())
            return null;

        var observacion = observacionService.buscarPorId(idObservacion);
        if (observacion == null)
            return null;

        long total = imagenRepository.countByObservacion_Id(idObservacion);
        if (total >= 5) {
            throw new RuntimeException("Máximo 5 imágenes por observación");
        }

        String urlArchivo = supabaseStorageService.subirImagen(file);

        Imagen imagen = new Imagen();
        imagen.setObservacion(observacion);
        imagen.setUrlArchivo(urlArchivo);
        imagen.setTitulo(titulo);

        return imagenRepository.save(imagen);
    }
}

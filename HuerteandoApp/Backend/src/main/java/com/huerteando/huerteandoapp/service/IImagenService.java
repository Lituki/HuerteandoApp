package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Imagen;

import java.util.List;

public interface IImagenService {

    // CRUD básico
    Imagen crear(Imagen imagen);
    Imagen actualizar(Imagen imagen);
    void eliminar(Long idImagen);

    Imagen buscarPorId(Long idImagen);
    List<Imagen> listarTodas();

    // Lógica de negocio típica
    List<Imagen> listarPorObservacion(Long idObservacion);
    long contarPorObservacion(Long idObservacion);
    void borrarPorObservacion(Long idObservacion);
}

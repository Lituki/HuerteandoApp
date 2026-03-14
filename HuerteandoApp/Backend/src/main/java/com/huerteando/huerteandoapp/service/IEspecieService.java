package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Especie;

import java.util.List;

public interface IEspecieService {

    // CRUD básico
    Especie crear(Especie especie);
    Especie actualizar(Especie especie);
    void eliminar(Long idEspecie);

    Especie buscarPorId(Long idEspecie);
    List<Especie> listarTodas();

    // Búsquedas típicas
    Especie buscarPorNombreCientifico(String nombreCientifico);
    boolean existeNombreCientifico(String nombreCientifico);

    // Una especie es invasora si tiene entrada en el catálogo EEI
    boolean esInvasora(String nombreCientifico);
    List<Especie> listarInvasoras();
    List<Especie> listarPorCatalogo(Long idCatalogoEei);
    long contarInvasoras();
}

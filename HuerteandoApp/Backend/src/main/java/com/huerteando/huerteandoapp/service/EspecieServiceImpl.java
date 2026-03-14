package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Especie;
import com.huerteando.huerteandoapp.repository.EspecieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EspecieServiceImpl implements IEspecieService {

    private final EspecieRepository especieRepository;

    public EspecieServiceImpl(EspecieRepository especieRepository) {
        this.especieRepository = especieRepository;
    }

    @Override
    @Transactional
    public Especie crear(Especie especie) {
        if (especie == null) return null;

        // En tu SQL nombre_cientifico es NOT NULL y UNIQUE.
        if (especie.getNombreCientifico() == null || especie.getNombreCientifico().isBlank()) return null;

        boolean yaExiste = especieRepository.existsByNombreCientificoIgnoreCase(especie.getNombreCientifico());
        if (yaExiste) return null;

        return especieRepository.save(especie);
    }

    @Override
    @Transactional
    public Especie actualizar(Especie especie) {
        if (especie == null) return null;
        if (especie.getId() == null) return null;

        boolean existe = especieRepository.existsById(especie.getId());
        if (!existe) return null;

        // Ojo: si cambias nombreCientifico a uno ya existente, te petará por UNIQUE.
        return especieRepository.save(especie);
    }

    @Override
    @Transactional
    public void eliminar(Long idEspecie) {
        if (idEspecie == null) return;
        especieRepository.deleteById(idEspecie);
    }

    @Override
    @Transactional(readOnly = true)
    public Especie buscarPorId(Long idEspecie) {
        if (idEspecie == null) return null;
        return especieRepository.findById(idEspecie).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Especie> listarTodas() {
        return especieRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Especie buscarPorNombreCientifico(String nombreCientifico) {
        if (nombreCientifico == null || nombreCientifico.isBlank()) return null;
        return especieRepository.findByNombreCientificoIgnoreCase(nombreCientifico.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNombreCientifico(String nombreCientifico) {
        if (nombreCientifico == null || nombreCientifico.isBlank()) return false;
        return especieRepository.existsByNombreCientificoIgnoreCase(nombreCientifico.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esInvasora(String nombreCientifico) {
        if (nombreCientifico == null || nombreCientifico.isBlank()) return false;

        Especie especie = especieRepository.findByNombreCientificoIgnoreCase(nombreCientifico.trim());

        // Una especie es invasora si tiene entrada en el catálogo EEI.
        if (especie == null) return false;

        return especie.getCatalogoEei() != null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Especie> listarInvasoras() {
        return especieRepository.findByCatalogoEeiIsNotNullOrderByNombreCientificoAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Especie> listarPorCatalogo(Long idCatalogoEei) {
        if (idCatalogoEei == null) return List.of();
        return especieRepository.findByCatalogoEei_IdOrderByNombreCientificoAsc(idCatalogoEei);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarInvasoras() {
        return especieRepository.countByCatalogoEeiIsNotNull();
    }
}

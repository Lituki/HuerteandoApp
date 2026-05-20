package com.huerteando.huerteandoapp.repository;

import com.huerteando.huerteandoapp.model.Especie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EspecieRepository extends JpaRepository<Especie, Long> {

    // Busca por nombre científico, ignorando mayúsculas/minúsculas.
    // Si no existe, devuelve null.
    Especie findByNombreCientificoIgnoreCase(String nombreCientifico);

    // Para validar si ya existe (por el UNIQUE del SQL).
    boolean existsByNombreCientificoIgnoreCase(String nombreCientifico);

    // Especies que tienen entrada en el catálogo EEI (invasoras).
    List<Especie> findByCatalogoEeiIsNotNullOrderByNombreCientificoAsc();

    // Especies del catálogo EEI concreto.
    List<Especie> findByCatalogoEei_IdOrderByNombreCientificoAsc(Long idCatalogoEei);

    // Cuenta cuántas especies están en el catálogo EEI (invasoras).
    long countByCatalogoEeiIsNotNull();
}

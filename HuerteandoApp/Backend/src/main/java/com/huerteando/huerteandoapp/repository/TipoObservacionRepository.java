package com.huerteando.huerteandoapp.repository;

import com.huerteando.huerteandoapp.model.TipoObservacion;
import org.springframework.data.jpa.repository.JpaRepository;

/*
Repo de tipo_observacion.
Normalmente es tabla "catálogo": pocos registros y casi no cambia.
*/
public interface TipoObservacionRepository extends JpaRepository<TipoObservacion, Short> {

    // Busca por nombre (ej: Planta, Rincón, Incidencia).
    TipoObservacion findByNombreIgnoreCase(String nombre);
}

package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.TipoObservacion;

import java.util.List;

/*
Servicios de TipoObservacion.
Aquí va la lógica mínima y ya.
*/
public interface ITipoObservacionService {

    // Lista todo el catálogo.
    List<TipoObservacion> listar();

    // Busca por id (PK).
    TipoObservacion buscarPorId(Short id);

    // Busca por nombre.
    TipoObservacion buscarPorNombre(String nombre);
}

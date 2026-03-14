package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Observacion;

import java.util.List;

public interface IObservacionService {

    // CRUD básico
    Observacion crear(Observacion observacion);
    Observacion actualizar(Observacion observacion);
    void eliminar(Long idObservacion);

    Observacion buscarPorId(Long idObservacion);
    List<Observacion> listarTodas();

    // Listados típicos
    List<Observacion> listarPorUsuario(Long idUsuario);
    List<Observacion> listarPorTipo(Long idTipoObservacion);
    List<Observacion> listarPorEspecie(Long idEspecie);
    List<Observacion> listarPorEstado(String estado);

    // Home / feed
    List<Observacion> ultimas20();
}

package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Observacion;
import java.util.List;

// Interfaz del servicio de observaciones.
// Definimos aquí los métodos disponibles sin entrar en cómo se implementan.
// La implementación real está en ObservacionServiceImpl.
// Trabajar con la interfaz hace el código más flexible y fácil de cambiar.
public interface IObservacionService {

    // CRUD básico: crear, actualizar, eliminar y buscar por id
    Observacion crear(Observacion observacion);
    Observacion actualizar(Observacion observacion);
    void eliminar(Long idObservacion);
    Observacion buscarPorId(Long idObservacion);

    // Devuelve todas las observaciones sin filtro
    List<Observacion> listarTodas();

    // Filtros para la pantalla principal de la app
    List<Observacion> listarPorUsuario(Long idUsuario);
    List<Observacion> listarPorTipo(Long idTipoObservacion);
    List<Observacion> listarPorEspecie(Long idEspecie);
    List<Observacion> listarPorEstadoObservacion(String estadoObservacion);

    // Las 20 más recientes, para el feed de inicio
    List<Observacion> ultimas20();
}

package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Observacion;
import com.huerteando.huerteandoapp.repository.ObservacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service = lógica de negocio. Aquí validamos lo mínimo y llamamos al repositorio.
@Service
public class ObservacionServiceImpl implements IObservacionService {

    private final ObservacionRepository observacionRepository;

    public ObservacionServiceImpl(ObservacionRepository observacionRepository) {
        this.observacionRepository = observacionRepository;
    }

    // Crear una observación. Si faltan campos básicos, devolvemos null (controller -> 400).
    @Override
    @Transactional
    public Observacion crear(Observacion observacion) {
        if (observacion == null) return null;

        // Mínimos obligatorios para que la BD no reviente y tenga sentido.
        if (observacion.getUsuario() == null) return null;
        if (observacion.getTipoObservacion() == null) return null;
        if (observacion.getLatitud() == null || observacion.getLongitud() == null) return null;

        return observacionRepository.save(observacion);
    }

    // Actualizar: si no existe, devolvemos null (controller -> 404).
    @Override
    @Transactional
    public Observacion actualizar(Observacion observacion) {
        if (observacion == null) return null;
        if (observacion.getId() == null) return null;

        // Evita que un "update" acabe haciendo un "insert" por error.
        if (!observacionRepository.existsById(observacion.getId())) return null;

        return observacionRepository.save(observacion);
    }

    // Borrado simple por id.
    @Override
    @Transactional
    public void eliminar(Long idObservacion) {
        if (idObservacion == null) return;
        observacionRepository.deleteById(idObservacion);
    }

    // Detalle por id. Traemos también las imágenes con EntityGraph.
    // Motivo: con open-in-view=false, lo LAZY puede fallar al serializar a JSON.
    @Override
    @Transactional(readOnly = true)
    public Observacion buscarPorId(Long idObservacion) {
        if (idObservacion == null) return null;
        return observacionRepository.findWithImagenesById(idObservacion).orElse(null);
    }

    // Listado sin filtros.
    @Override
    @Transactional(readOnly = true)
    public List<Observacion> listarTodas() {
        return observacionRepository.findAll();
    }

    // Observaciones de un usuario (últimas primero).
    @Override
    @Transactional(readOnly = true)
    public List<Observacion> listarPorUsuario(Long idUsuario) {
        if (idUsuario == null) return List.of();
        return observacionRepository.findByUsuario_IdOrderByCreadoEnDesc(idUsuario);
    }

    // Por tipo (fecha desc).
    @Override
    @Transactional(readOnly = true)
    public List<Observacion> listarPorTipo(Long idTipoObservacion) {
        if (idTipoObservacion == null) return List.of();
        return observacionRepository.findByTipoObservacion_IdOrderByFechaObservacionDesc(idTipoObservacion);
    }

    // Por especie (fecha desc).
    @Override
    @Transactional(readOnly = true)
    public List<Observacion> listarPorEspecie(Long idEspecie) {
        if (idEspecie == null) return List.of();
        return observacionRepository.findByEspecie_IdOrderByFechaObservacionDesc(idEspecie);
    }

    // Por estado (ABIERTA, CERRADA, ...).
    @Override
    @Transactional(readOnly = true)
    public List<Observacion> listarPorEstadoObservacion(String estadoObservacion) {
        if (estadoObservacion == null || estadoObservacion.isBlank()) return List.of();
        return observacionRepository.findByEstadoObservacionIgnoreCaseOrderByActualizadoEnDesc(estadoObservacion);
    }

    // Para el feed: las 20 últimas creadas.
    @Override
    @Transactional(readOnly = true)
    public List<Observacion> ultimas20() {
        return observacionRepository.findTop20ByOrderByCreadoEnDesc();
    }
}
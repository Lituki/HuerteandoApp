package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Observacion;
import com.huerteando.huerteandoapp.repository.ObservacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObservacionServiceImpl implements IObservacionService {

    private final ObservacionRepository observacionRepository;

    public ObservacionServiceImpl(ObservacionRepository observacionRepository) {
        this.observacionRepository = observacionRepository;
    }

    @Override
    @Transactional
    public Observacion crear(Observacion observacion) {
        if (observacion == null) return null;

        // Esto lo mínimo para no guardar basura.
        if (observacion.getUsuario() == null) return null;
        if (observacion.getTipoObservacion() == null) return null;
        if (observacion.getLatitud() == null || observacion.getLongitud() == null) return null;

        return observacionRepository.save(observacion);
    }

    @Override
    @Transactional
    public Observacion actualizar(Observacion observacion) {
        if (observacion == null) return null;
        if (observacion.getId() == null) return null;

        boolean existe = observacionRepository.existsById(observacion.getId());
        if (!existe) return null;

        return observacionRepository.save(observacion);
    }

    @Override
    @Transactional
    public void eliminar(Long idObservacion) {
        if (idObservacion == null) return;
        observacionRepository.deleteById(idObservacion);
    }

    @Override
    @Transactional(readOnly = true)
    public Observacion buscarPorId(Long idObservacion) {
        if (idObservacion == null) return null;
        return observacionRepository.findById(idObservacion).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Observacion> listarTodas() {
        return observacionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Observacion> listarPorUsuario(Long idUsuario) {
        if (idUsuario == null) return List.of();
        return observacionRepository.findByUsuario_IdOrderByCreadoEnDesc(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Observacion> listarPorTipo(Long idTipoObservacion) {
        if (idTipoObservacion == null) return List.of();
        return observacionRepository.findByTipoObservacion_IdOrderByFechaObservacionDesc(idTipoObservacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Observacion> listarPorEspecie(Long idEspecie) {
        if (idEspecie == null) return List.of();
        return observacionRepository.findByEspecie_IdOrderByFechaObservacionDesc(idEspecie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Observacion> listarPorEstado(String estado) {
        if (estado == null || estado.isBlank()) return List.of();
        return observacionRepository.findByEstadoIgnoreCaseOrderByActualizadoEnDesc(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Observacion> ultimas20() {
        return observacionRepository.findTop20ByOrderByCreadoEnDesc();
    }
}

package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Observacion;
import com.huerteando.huerteandoapp.repository.ObservacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

// @Service le dice a Spring que esta clase es un componente de lógica de negocio.
// Spring la detecta sola y la registra para poder inyectarla donde haga falta.
@Service
public class ObservacionServiceImpl implements IObservacionService {

    private final ObservacionRepository observacionRepository;

    public ObservacionServiceImpl(ObservacionRepository observacionRepository) {
        this.observacionRepository = observacionRepository;
    }

    // @Transactional significa que si algo falla dentro del método,
    // la BD vuelve al estado anterior (rollback automático).
    // Es importante en métodos que escriben en la BD.
    @Override
    @Transactional
    public Observacion crear(Observacion observacion) {
        if (observacion == null) return null;

        // Validamos los campos mínimos obligatorios antes de intentar guardar.
        // Si falta alguno, devolvemos null y el controller responde 400.
        if (observacion.getUsuario() == null) return null;
        if (observacion.getTipoObservacion() == null) return null;
        if (observacion.getLatitud() == null || observacion.getLongitud() == null) return null;

        // save() de JpaRepository hace el INSERT en la BD y devuelve el objeto con el id generado.
        return observacionRepository.save(observacion);
    }

    @Override
    @Transactional
    public Observacion actualizar(Observacion observacion) {
        if (observacion == null) return null;
        if (observacion.getId() == null) return null;

        // Comprobamos que existe antes de actualizar.
        // Si no existe, save() haría un INSERT en vez de un UPDATE, lo cual sería un bug.
        if (!observacionRepository.existsById(observacion.getId())) return null;

        return observacionRepository.save(observacion);
    }

    @Override
    @Transactional
    public void eliminar(Long idObservacion) {
        if (idObservacion == null) return;
        observacionRepository.deleteById(idObservacion);
    }

    // readOnly = true le dice a Hibernate que esta transacción solo lee.
    // Hibernate puede optimizarla internamente (no hace flush al final).
    @Override
    @Transactional(readOnly = true)
    public Observacion buscarPorId(Long idObservacion) {
        if (idObservacion == null) return null;

        // findById devuelve un Optional. orElse(null) lo convierte a null si no existe.
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
        if (idUsuario == null) return List.of(); // lista vacía, no null
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
    public List<Observacion> listarPorEstadoObservacion(String estadoObservacion) {
        if (estadoObservacion == null || estadoObservacion.isBlank()) return List.of();
        return observacionRepository.findByEstadoObservacionIgnoreCaseOrderByActualizadoEnDesc(estadoObservacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Observacion> ultimas20() {
        return observacionRepository.findTop20ByOrderByCreadoEnDesc();
    }
}

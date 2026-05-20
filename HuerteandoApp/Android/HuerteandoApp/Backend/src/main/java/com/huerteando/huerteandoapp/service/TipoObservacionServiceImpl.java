package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.TipoObservacion;
import com.huerteando.huerteandoapp.repository.TipoObservacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
Implementación del service de TipoObservacion.
Ojo: readOnly porque aquí casi siempre solo se consulta.
*/
@Service
public class TipoObservacionServiceImpl implements ITipoObservacionService {

    private final TipoObservacionRepository tipoObservacionRepository;

    public TipoObservacionServiceImpl(TipoObservacionRepository tipoObservacionRepository) {
        this.tipoObservacionRepository = tipoObservacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoObservacion> listar() {
        return tipoObservacionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoObservacion buscarPorId(Short id) {
        if (id == null) return null;
        return tipoObservacionRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public TipoObservacion buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        return tipoObservacionRepository.findByNombreIgnoreCase(nombre.trim());
    }
}

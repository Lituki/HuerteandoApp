package com.huerteando.huerteandoapp.controller;

import com.huerteando.huerteandoapp.model.TipoObservacion;
import com.huerteando.huerteandoapp.service.ITipoObservacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Los tipos de observación son un catálogo fijo: Planta, Rincón de interés, Incidencia ambiental.
// Solo necesitamos GET, no tiene sentido que la app Android cree o borre tipos.
// La app los usa al crear una observación para mostrar el desplegable de tipos.
@RestController
@RequestMapping("/api/tipos-observacion")
public class TipoObservacionController {

    private final ITipoObservacionService tipoService;

    public TipoObservacionController(ITipoObservacionService tipoService) {
        this.tipoService = tipoService;
    }

    // GET /api/tipos-observacion
    // Devuelve todos los tipos. La app los carga una vez al arrancar.
    @GetMapping
    public ResponseEntity<List<TipoObservacion>> listar() {
        return ResponseEntity.ok(tipoService.listar());
    }

    // GET /api/tipos-observacion/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TipoObservacion> buscarPorId(@PathVariable Short id) {
        TipoObservacion tipo = tipoService.buscarPorId(id);
        if (tipo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(tipo);
    }
}

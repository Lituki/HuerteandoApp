package com.huerteando.huerteandoapp.controller;
import com.huerteando.huerteandoapp.model.Observacion;
import com.huerteando.huerteandoapp.service.IObservacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * Endpoints REST de observaciones.
 * CRUD basico y directo.
 */
@RestController
@RequestMapping("/api/observaciones")
public class ObservacionController {
    private final IObservacionService observacionService;
    public ObservacionController(IObservacionService observacionService) {
        this.observacionService = observacionService;
    }
    /**
     * GET /api/observaciones
        * Devuelve todas las observaciones.
     */
    @GetMapping({"", "/"})
    public ResponseEntity<List<Observacion>> listarTodas() {
        List<Observacion> observaciones = observacionService.listarTodas();
        return ResponseEntity.ok(observaciones);
    }
    /**
     * GET /api/observaciones/{id}
        * Busca una observacion por id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Observacion> obtenerPorId(@PathVariable Long id) {
        Observacion observacion = observacionService.buscarPorId(id);
        if (observacion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(observacion);
    }
    /**
     * POST /api/observaciones
        * Crea una observacion nueva.
     */
    @PostMapping
    public ResponseEntity<Observacion> crear(@RequestBody Observacion observacion) {
        Observacion nuevaObservacion = observacionService.crear(observacion);
        if (nuevaObservacion == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaObservacion);
    }
    /**
     * PUT /api/observaciones/{id}
        * Actualiza una observacion existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Observacion> actualizar(@PathVariable Long id, @RequestBody Observacion observacion) {
        // Forzamos el id del path para evitar lios.
        observacion.setId(id);
        Observacion observacionActualizada = observacionService.actualizar(observacion);
        if (observacionActualizada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(observacionActualizada);
    }
    /**
     * DELETE /api/observaciones/{id}
        * Borra una observacion por id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Observacion observacion = observacionService.buscarPorId(id);
        if (observacion == null) {
            return ResponseEntity.notFound().build();
        }
        observacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

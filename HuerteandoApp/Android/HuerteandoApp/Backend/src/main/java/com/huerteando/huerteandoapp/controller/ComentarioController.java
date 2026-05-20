package com.huerteando.huerteandoapp.controller;

import com.huerteando.huerteandoapp.model.Comentario;
import com.huerteando.huerteandoapp.service.IComentarioService;
import com.huerteando.huerteandoapp.service.IObservacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Los comentarios están anidados bajo una observación.
// La URL base incluye el id de la observación padre: /api/observaciones/{idObservacion}/comentarios
// Esto es REST: los recursos hijos van dentro de la ruta del padre.
@RestController
@RequestMapping("/api/observaciones/{idObservacion}/comentarios")
public class ComentarioController {

    private final IComentarioService comentarioService;

    // Necesitamos también el servicio de observaciones para verificar
    // que la observación existe antes de añadir un comentario.
    private final IObservacionService observacionService;

    public ComentarioController(IComentarioService comentarioService, IObservacionService observacionService) {
        this.comentarioService = comentarioService;
        this.observacionService = observacionService;
    }

    // GET /api/observaciones/{idObservacion}/comentarios
    // Devuelve todos los comentarios de una observación, ordenados del más antiguo al más nuevo.
    @GetMapping
    public ResponseEntity<List<Comentario>> listar(@PathVariable Long idObservacion) {
        return ResponseEntity.ok(comentarioService.listarPorObservacion(idObservacion));
    }

    // POST /api/observaciones/{idObservacion}/comentarios
    // Crea un comentario nuevo en esa observación.
    @PostMapping
    public ResponseEntity<Comentario> crear(@PathVariable Long idObservacion, @RequestBody Comentario comentario) {

        // Antes de guardar, comprobamos que la observación existe.
        // Si no existe, no tiene sentido crear un comentario huérfano.
        var observacion = observacionService.buscarPorId(idObservacion);
        if (observacion == null) return ResponseEntity.notFound().build();

        // Asignamos la observación real al comentario.
        // El id de observación viene de la URL, no del body, para evitar inconsistencias.
        comentario.setObservacion(observacion);

        Comentario nuevo = comentarioService.guardar(comentario);
        if (nuevo == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // DELETE /api/observaciones/{idObservacion}/comentarios/{idComentario}
    @DeleteMapping("/{idComentario}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idObservacion, @PathVariable Long idComentario) {

        // Comprobamos que el comentario existe antes de intentar borrarlo.
        if (comentarioService.buscarPorId(idComentario) == null) return ResponseEntity.notFound().build();

        comentarioService.borrar(idComentario);
        return ResponseEntity.noContent().build();
    }
}

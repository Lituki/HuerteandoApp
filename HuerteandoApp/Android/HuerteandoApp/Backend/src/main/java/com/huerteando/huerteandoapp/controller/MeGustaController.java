package com.huerteando.huerteandoapp.controller;

import com.huerteando.huerteandoapp.service.IMeGustaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// Los likes no necesitan un body con datos complejos.
// Solo necesitamos saber qué usuario le da like a qué observación.
// El idObservacion va en la URL y el idUsuario como parámetro de consulta (?idUsuario=1).
@RestController
@RequestMapping("/api/observaciones/{idObservacion}/megustas")
public class MeGustaController {

    private final IMeGustaService likeService;

    public MeGustaController(IMeGustaService likeService) {
        this.likeService = likeService;
    }

    // GET /api/observaciones/{idObservacion}/megustas/count
    // Devuelve el número total de likes de una observación.
    // La app Android lo usa para mostrar el contador en la tarjeta y en el detalle.
    // Devuelve un Map para que el JSON sea: { "megustas": 42 }
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> contar(@PathVariable Long idObservacion) {
        long total = likeService.contarLikes(idObservacion);
        return ResponseEntity.ok(Map.of("megustas", total));
    }

    // GET /api/observaciones/{idObservacion}/megustas/existe?idUsuario=1
    // Comprueba si un usuario concreto ya dio like a esta observación.
    // La app lo usa para saber si pintar el corazón relleno o vacío.
    // Devuelve: { "yaMeGusta": true } o { "yaMeGusta": false }
    @GetMapping("/existe")
    public ResponseEntity<Map<String, Boolean>> existe(
            @PathVariable Long idObservacion,
            @RequestParam Long idUsuario) {
        boolean existe = likeService.existeLike(idObservacion, idUsuario);
        return ResponseEntity.ok(Map.of("yaMeGusta", existe));
    }

    // POST /api/observaciones/{idObservacion}/megustas?idUsuario=1
    // Da like. Si el usuario ya había dado like antes, devuelve 409 Conflict.
    // No devuelve body, solo el código de estado.
    @PostMapping
    public ResponseEntity<Void> darLike(
            @PathVariable Long idObservacion,
            @RequestParam Long idUsuario) {

        var like = likeService.darLike(idObservacion, idUsuario);

        // El servicio devuelve null si el like ya existía (evita duplicados).
        if (like == null) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // DELETE /api/observaciones/{idObservacion}/megustas?idUsuario=1
    // Quita el like. Si no existía, no pasa nada (el servicio lo ignora).
    @DeleteMapping
    public ResponseEntity<Void> quitarLike(
            @PathVariable Long idObservacion,
            @RequestParam Long idUsuario) {
        likeService.quitarLike(idObservacion, idUsuario);
        return ResponseEntity.noContent().build();
    }
}

package com.huerteando.huerteandoapp.controller;

import com.huerteando.huerteandoapp.model.Imagen;
import com.huerteando.huerteandoapp.service.IImagenService;
import com.huerteando.huerteandoapp.service.IObservacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Igual que los comentarios, las imágenes van anidadas bajo la observación.
// Primero guardas la observación, obtienes su id, y luego subes las imágenes.
// No puede existir una imagen sin observación (FK NOT NULL en la BD).
@RestController
@RequestMapping("/api/observaciones/{idObservacion}/imagenes")
public class ImagenController {

    private final IImagenService imagenService;
    private final IObservacionService observacionService;

    public ImagenController(IImagenService imagenService, IObservacionService observacionService) {
        this.imagenService = imagenService;
        this.observacionService = observacionService;
    }

    // GET /api/observaciones/{idObservacion}/imagenes
    // Devuelve todas las imágenes de una observación, ordenadas por fecha de subida.
    @GetMapping
    public ResponseEntity<List<Imagen>> listar(@PathVariable Long idObservacion) {
        return ResponseEntity.ok(imagenService.listarPorObservacion(idObservacion));
    }

    // POST /api/observaciones/{idObservacion}/imagenes
    // Añade una imagen a una observación existente.
    // El body lleva: urlArchivo (obligatorio) y titulo (opcional).
    @PostMapping
    public ResponseEntity<Imagen> crear(@PathVariable Long idObservacion, @RequestBody Imagen imagen) {

        // Verificamos que la observación padre existe.
        var observacion = observacionService.buscarPorId(idObservacion);
        if (observacion == null) return ResponseEntity.notFound().build();

        // Asignamos la observación real. El id viene de la URL, no del body.
        imagen.setObservacion(observacion);

        // El servicio valida que urlArchivo no sea null ni vacío.
        Imagen nueva = imagenService.crear(imagen);
        if (nueva == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // DELETE /api/observaciones/{idObservacion}/imagenes/{idImagen}
    @DeleteMapping("/{idImagen}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idObservacion, @PathVariable Long idImagen) {

        if (imagenService.buscarPorId(idImagen) == null) return ResponseEntity.notFound().build();

        imagenService.eliminar(idImagen);
        return ResponseEntity.noContent().build();
    }
}

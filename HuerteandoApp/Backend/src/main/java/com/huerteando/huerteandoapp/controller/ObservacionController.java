package com.huerteando.huerteandoapp.controller;

import com.huerteando.huerteandoapp.model.Observacion;
import com.huerteando.huerteandoapp.service.IObservacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// @RestController le dice a Spring que esta clase atiende peticiones HTTP
// y que las respuestas van en formato JSON automáticamente.
// @RequestMapping define la URL base para todos los métodos de aquí dentro.
@RestController
@RequestMapping("/api/observaciones")
public class ObservacionController {

    // Usamos la interfaz, no la implementación directa.
    // Así si mañana cambiamos cómo funciona el servicio, aquí no tocamos nada.
    private final IObservacionService observacionService;

    // Spring inyecta el servicio por constructor automáticamente.
    // Es la forma recomendada, más limpia que @Autowired en el campo.
    public ObservacionController(IObservacionService observacionService) {
        this.observacionService = observacionService;
    }

    // GET /api/observaciones             → devuelve todas
    // GET /api/observaciones?tipo=1      → filtra por tipo de observación
    // GET /api/observaciones?usuario=1   → filtra por usuario
    // GET /api/observaciones?estado_observacion=ABIERTA → filtra por estado
    // Los @RequestParam son opcionales (required = false), si no llegan son null.
    @GetMapping({"", "/"})
    public ResponseEntity<List<Observacion>> listar(
            @RequestParam(required = false) Long tipo,
            @RequestParam(required = false) Long usuario,
            @RequestParam(name = "estado_observacion", required = false) String estadoObservacion,
            @RequestParam(name = "estado", required = false) String estadoLegacy) {

        // Comprobamos cuál filtro llegó y llamamos al método correspondiente.
        // Si no llega ninguno, devolvemos todo.
        if (tipo != null)    return ResponseEntity.ok(observacionService.listarPorTipo(tipo));
        if (usuario != null) return ResponseEntity.ok(observacionService.listarPorUsuario(usuario));
        String estadoFiltro = estadoObservacion != null ? estadoObservacion : estadoLegacy;
        if (estadoFiltro != null)  return ResponseEntity.ok(observacionService.listarPorEstadoObservacion(estadoFiltro));

        return ResponseEntity.ok(observacionService.listarTodas());
    }

    // GET /api/observaciones/{id}
    // @PathVariable coge el {id} de la URL y lo mete en la variable id.
    @GetMapping("/{id}")
    public ResponseEntity<Observacion> obtenerPorId(@PathVariable Long id) {
        Observacion observacion = observacionService.buscarPorId(id);

        // Si no existe, devolvemos 404 con un mensaje claro.
        if (observacion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe observación con id=" + id);
        }

        return ResponseEntity.ok(observacion);
    }

    // POST /api/observaciones
    // @RequestBody convierte el JSON que llega en el body a un objeto Observacion.
    @PostMapping
    public ResponseEntity<Observacion> crear(@RequestBody Observacion observacion) {
        Observacion nueva = observacionService.crear(observacion);

        // El servicio devuelve null si faltan campos obligatorios (usuario, tipo, coordenadas).
        // Respondemos 400 Bad Request.
        if (nueva == null) return ResponseEntity.badRequest().build();

        // 201 Created es el código correcto cuando se crea algo nuevo, no 200.
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // PUT /api/observaciones/{id}
    // Se usa para actualizar una observación existente.
    @PutMapping("/{id}")
    public ResponseEntity<Observacion> actualizar(@PathVariable Long id, @RequestBody Observacion observacion) {

        // Forzamos el id desde la URL para que no haya lío si el body trae otro id.
        observacion.setId(id);

        Observacion actualizada = observacionService.actualizar(observacion);

        // Si devuelve null es que ese id no existe en la BD.
        if (actualizada == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(actualizada);
    }

    // DELETE /api/observaciones/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        // Primero comprobamos que existe antes de intentar borrar.
        if (observacionService.buscarPorId(id) == null) return ResponseEntity.notFound().build();

        observacionService.eliminar(id);

        // 204 No Content: operación correcta pero sin nada que devolver.
        return ResponseEntity.noContent().build();
    }
}

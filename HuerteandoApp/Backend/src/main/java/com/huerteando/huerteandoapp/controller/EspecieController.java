package com.huerteando.huerteandoapp.controller;

import com.huerteando.huerteandoapp.model.Especie;
import com.huerteando.huerteandoapp.service.IEspecieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// CRUD completo de especies.
// Las especies se usan para identificar plantas en las observaciones.
// Una observación puede tener especie null si aún no se ha identificado.
@RestController
@RequestMapping("/api/especies")
public class EspecieController {

    private final IEspecieService especieService;

    public EspecieController(IEspecieService especieService) {
        this.especieService = especieService;
    }

    // GET /api/especies
    @GetMapping
    public ResponseEntity<List<Especie>> listar() {
        return ResponseEntity.ok(especieService.listarTodas());
    }

    // GET /api/especies/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Especie> buscarPorId(@PathVariable Long id) {
        Especie especie = especieService.buscarPorId(id);
        if (especie == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(especie);
    }

    // POST /api/especies
    // El servicio rechaza la creación si el nombre científico ya existe (es UNIQUE en BD).
    @PostMapping
    public ResponseEntity<Especie> crear(@RequestBody Especie especie) {
        Especie nueva = especieService.crear(especie);

        // null significa que faltaba el nombre científico o ya existía.
        if (nueva == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // PUT /api/especies/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Especie> actualizar(@PathVariable Long id, @RequestBody Especie especie) {

        // Igual que en observaciones: forzamos el id desde la URL.
        especie.setId(id);

        Especie actualizada = especieService.actualizar(especie);
        if (actualizada == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(actualizada);
    }

    // DELETE /api/especies/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (especieService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        especieService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

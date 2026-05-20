package com.huerteando.huerteandoapp.controller;

import com.huerteando.huerteandoapp.model.Usuario;
import com.huerteando.huerteandoapp.service.IUsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

// Este controller no tiene @RequestMapping en la clase porque los endpoints
// de auth y usuarios tienen rutas distintas (/api/auth y /api/usuarios).
// Cada método define su propia ruta completa.
@RestController
public class UsuarioController {

    private final IUsuarioService usuarioService;

    public UsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // POST /api/auth/register
    // Recibe un objeto Usuario en el body con los datos del formulario de registro.
    @PostMapping("/api/auth/register")
    public ResponseEntity<Usuario> register(@RequestBody Usuario usuario) {

        // El nick es obligatorio y tiene que ser único en la BD.
        if (usuario.getNick() == null || usuario.getNick().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Preguntamos al servicio si ese nick ya está pillado.
        if (usuarioService.existeNick(usuario.getNick())) {
            // 409 Conflict: el recurso ya existe, no es un error del cliente ni del servidor.
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // El email es opcional, pero si llega tiene que ser único también.
        if (usuario.getEmail() != null && usuarioService.existeEmail(usuario.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Estos campos los pone el backend, no el usuario que se registra.
        // Si dejáramos que el cliente los mandara, podría poner lo que quisiera.
        usuario.setRol("USER");
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());

        Usuario nuevo = usuarioService.guardar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // POST /api/auth/login
    // Recibe un Map con "nick" y "password" en el body.
    // Nota: esto es un login básico sin tokens JWT.
    // En producción real habría que usar Spring Security con JWT,
    // pero para el proyecto de DAM esto es suficiente.
    @PostMapping("/api/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credenciales) {

        String nick = credenciales.get("nick");
        String password = credenciales.get("password");

        // Si falta alguno de los dos campos, petición incorrecta.
        if (nick == null || password == null) return ResponseEntity.badRequest().build();

        // Buscamos el usuario por nick.
        Usuario usuario = usuarioService.buscarPorNick(nick);

        // Si no existe o la contraseña no coincide, devolvemos 401 Unauthorized.
        // Ponemos los dos casos juntos a propósito: no queremos decirle al atacante
        // si el nick existe o no.
        if (usuario == null || !usuario.getPasswordHash().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Si la cuenta está desactivada, 403 Forbidden.
        if (!usuario.getActivo()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Guardamos cuándo fue el último acceso.
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioService.guardar(usuario);

        // Devolvemos solo los datos que necesita la app Android.
        // El passwordHash nunca sale gracias al @JsonIgnore del modelo,
        // pero igualmente construimos la respuesta a mano para ser explícitos.
        return ResponseEntity.ok(Map.of(
                "id",        usuario.getId(),
                "nick",      usuario.getNick(),
                "nombre",    usuario.getNombre(),
                "rol",       usuario.getRol(),
                "avatarUrl", usuario.getAvatarUrl() != null ? usuario.getAvatarUrl() : ""
        ));
    }

    // GET /api/usuarios/{id}
    // Devuelve el perfil público de un usuario.
    @GetMapping("/api/usuarios/{id}")
    public ResponseEntity<Usuario> perfil(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(usuario);
    }
}

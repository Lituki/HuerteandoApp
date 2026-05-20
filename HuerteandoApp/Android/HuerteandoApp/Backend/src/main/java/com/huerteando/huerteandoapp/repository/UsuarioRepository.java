package com.huerteando.huerteandoapp.repository;

import com.huerteando.huerteandoapp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por nick (debería ser único). Si no existe, devuelve null.
    // Esto es justo por lo que en el service NO puedes hacer .orElse(null)
    // (porque aquí NO hay Optional).
    Usuario findByNick(String nick);

    // Igual pero ignorando mayúsculas/minúsculas.
    // Útil si quieres permitir "Sergio" = "sergio".
    Usuario findByNickIgnoreCase(String nick);

    // Buscar por email (si lo usas). Puede ser null si no existe.
    Usuario findByEmail(String email);

    // Comprobar rápido si existe un nick (para validar registro).
    boolean existsByNickIgnoreCase(String nick);

    // Comprobar si existe un email (para validar registro).
    boolean existsByEmailIgnoreCase(String email);

    // Listar usuarios activos (por ejemplo para admin). Ordenados por fecha de registro (últimos primero).
    List<Usuario> findByActivoTrueOrderByFechaRegistroDesc();

    // Sacar los últimos X registrados (top 5 por ejemplo).
    List<Usuario> findTop5ByOrderByFechaRegistroDesc();
}

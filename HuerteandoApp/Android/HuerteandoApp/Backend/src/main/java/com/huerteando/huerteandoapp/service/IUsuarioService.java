package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Usuario;
import java.util.List;

// Interfaz del servicio de usuarios.
// Aquí están los métodos que necesita el controller para registro, login y perfil.
public interface IUsuarioService {

    // Busca por id (clave primaria)
    Usuario buscarPorId(Long idUsuario);

    // Busca por nick, que es único en la BD. Se usa en el login.
    Usuario buscarPorNick(String nick);

    // Comprueba si un nick ya está en uso. Se usa en el registro.
    boolean existeNick(String nick);

    // Comprueba si un email ya está en uso. Se usa en el registro.
    boolean existeEmail(String email);

    // Guarda o actualiza un usuario. save() de JPA hace INSERT o UPDATE según si tiene id o no.
    Usuario guardar(Usuario usuario);

    // Lista usuarios activos, útil para administración
    List<Usuario> listarActivos();

    // Los 5 últimos registrados
    List<Usuario> listarUltimos();
}

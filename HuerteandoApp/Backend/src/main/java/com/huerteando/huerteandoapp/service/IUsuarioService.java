package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Usuario;

import java.util.List;

public interface IUsuarioService {

    // Buscar usuario por id
    Usuario buscarPorId(Long idUsuario);

    // Buscar usuario por nick
    Usuario buscarPorNick(String nick);

    // Comprobar si existe un nick
    boolean existeNick(String nick);

    // Comprobar si existe un email
    boolean existeEmail(String email);

    // Guardar o actualizar usuario
    Usuario guardar(Usuario usuario);

    // Listar usuarios activos
    List<Usuario> listarActivos();

    // Listar últimos usuarios registrados (ej: top 5)
    List<Usuario> listarUltimos();
}

package com.huerteando.huerteandoapp.service;

import com.huerteando.huerteandoapp.model.Usuario;
import com.huerteando.huerteandoapp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long idUsuario) {
        if (idUsuario == null) return null;
        return usuarioRepository.findById(idUsuario).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorNick(String nick) {
        if (nick == null || nick.isBlank()) return null;

        // IgnoreCase para que "Sergio" y "sergio" sean el mismo nick.
        return usuarioRepository.findByNickIgnoreCase(nick);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNick(String nick) {
        if (nick == null || nick.isBlank()) return false;
        return usuarioRepository.existsByNickIgnoreCase(nick);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return usuarioRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        if (usuario == null) return null;

        // save() de JpaRepository hace INSERT si el objeto no tiene id,
        // o UPDATE si ya tiene id. Sirve para crear y para actualizar.
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrueOrderByFechaRegistroDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarUltimos() {
        return usuarioRepository.findTop5ByOrderByFechaRegistroDesc();
    }
}

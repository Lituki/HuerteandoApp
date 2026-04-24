package com.huerteando.app.clases;

/**
 * Representa la respuesta del login básico del backend.
 * Coincide con los campos devueltos por el UsuarioController (id, nick, nombre, rol, avatarUrl).
 * Se ha eliminado el campo 'token' ya que el backend no utiliza JWT.
 */
public class LoginResponse {

    private Long id;
    private String nick;
    private String nombre;
    private String rol;
    private String avatarUrl;

    // Constructor vacío requerido por GSON
    public LoginResponse() {}

    /* 
    // --- 🚧 CONSTRUCTOR PARA BYPASS (DESCOMENTAR SI SE USA EN LOGINACTIVITY) ---
    public LoginResponse(Long id, String nick, String nombre, String rol) {
        this.id = id;
        this.nick = nick;
        this.nombre = nombre;
        this.rol = rol;
    }
    // --------------------------------------------------------------------------
    */

    // Getters
    public Long getId() { return id; }
    public String getNick() { return nick; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
    public String getAvatarUrl() { return avatarUrl; }

    // Setters (Necesarios para el mapeo de GSON y posibles pruebas de bypass)
    public void setId(Long id) { this.id = id; }
    public void setNick(String nick) { this.nick = nick; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setRol(String rol) { this.rol = rol; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}

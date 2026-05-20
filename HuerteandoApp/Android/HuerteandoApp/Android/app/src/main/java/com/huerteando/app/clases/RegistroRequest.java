package com.huerteando.app.clases;


/**
 * Clase para enviar datos al servidor al registrar un nuevo usuario.
 * Se envía como JSON al API.
 */
public class RegistroRequest {
    private final String nick;
    private final String password;
    private final String nombre;
    private final String apellidos;
    private final String email;
    private final String avatarUrl;

    public RegistroRequest(String nick, String password, String nombre, String apellidos, String email, String avatarUrl) {
        this.nick = nick;
        this.password = password;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    public String getNick() { return nick; }
    public String getPassword() { return password; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
}

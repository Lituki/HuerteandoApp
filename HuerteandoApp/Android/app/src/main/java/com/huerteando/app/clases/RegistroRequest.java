package com.huerteando.app.clases;

/**
 * Clase para enviar datos al servidor al registrar un nuevo usuario.
 * Se envía como JSON al API.
 */
public class RegistroRequest {
    private String nick;
    private String password;
    private String nombre;
    private String apellidos;
    private String email;

    public RegistroRequest(String nick, String password, String nombre, String apellidos, String email) {
        this.nick = nick;
        this.password = password;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
    }

    public String getNick() { return nick; }
    public String getPassword() { return password; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
}

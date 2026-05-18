package com.huerteando.huerteandoapp.dto;

/**
 * DTO sencillo para el registro.
 *
 * La app Android envía "password" (texto plano) y el backend lo guarda en passwordHash.
 * (En un proyecto real se debería hashear con Spring Security.)
 */
public class RegistroRequest {

    private String nick;
    private String nombre;
    private String apellidos;
    private String email;
    private String avatarUrl;
    private String password;

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

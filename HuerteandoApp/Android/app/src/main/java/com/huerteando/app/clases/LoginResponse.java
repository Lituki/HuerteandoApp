package com.huerteando.app.clases;

// Lo que nos devuelve el servidor si el login es correcto
public class LoginResponse {
    private String token;   // El JWT o token de sesión
    private String nick;
    private String rol;     // "USER" o "ADMIN"
    private Long id;

    public String getToken() {
        return token;
    }
    public String getNick() {
        return nick;
    }
    public String getRol() {
        return rol;
    }
    public Long getId() {
        return id;
    }
}
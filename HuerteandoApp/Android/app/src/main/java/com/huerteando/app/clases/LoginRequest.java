package com.huerteando.app.clases;

// Lo que enviamos al servidor cuando el usuario pulsa "Entrar"
public class LoginRequest {
    private final String nick;
    private final String password;

    public LoginRequest(String nick, String password) {
        this.nick = nick;
        this.password = password;
    }

    // Retrofit necesita los getters para serializar a JSON
    public String getNick() { return nick; }
    public String getPassword() { return password; }
}
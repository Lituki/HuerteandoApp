package com.huerteando.app.clases;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private Long id;
    private String nick;
    private String token;
    private String rol;

    // Usamos SerializedName por si el backend devuelve "nombre_usuario" o similar,
    // pero según el manual el campo es "nick".

    public Long getId() { return id; }
    public String getNick() { return nick; }
    public String getToken() { return token; }
    public String getRol() { return rol; }
}
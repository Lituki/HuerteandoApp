package com.huerteando.app.clases;

/**
 * Clase para actualizar el perfil del usuario.
 */
public class ActualizarPerfilRequest {
    private String nombre;
    private String apellidos;
    private String email;
    private String avatarUrl;  // URL de la nueva foto de perfil

    public ActualizarPerfilRequest(String nombre, String apellidos, String email, String avatarUrl) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
}

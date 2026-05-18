package com.huerteando.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager — Gestiona la sesión local del usuario (id, nick, nombre, rol).
 */
public class SessionManager {

    private static final String PREF_NAME = "HuerteandoSession";
    private static final String KEY_ID     = "userId";
    private static final String KEY_NICK   = "nick";
    private static final String KEY_NOMBRE    = "nombre";
    private static final String KEY_APELLIDOS = "apellidos";
    private static final String KEY_EMAIL     = "email";
    private static final String KEY_FECHA     = "fechaRegistro";
    private static final String KEY_ROL       = "rol";
    private static final String KEY_AVATAR = "avatarUrl";
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private static final String TOKEN_KEY = "jwt_token";

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences("sesion", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }
    public void guardarToken(String token) {
        prefs.edit().putString(TOKEN_KEY, token).apply();
    }

    public String getToken() {
        return prefs.getString(TOKEN_KEY, null);
    }

    public boolean haySesion() {
        return getToken() != null;
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }


    /**
     * Guarda los datos del usuario recibidos en el login.
     */
    public void guardarDatosCompletos(Long id, String nick, String nombre, String apellidos, String email, String fecha, String rol, String avatarUrl) {
        editor.putLong(KEY_ID, id);
        editor.putString(KEY_NICK, nick);
        editor.putString(KEY_NOMBRE, nombre);
        editor.putString(KEY_APELLIDOS, apellidos);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FECHA, fecha);
        editor.putString(KEY_ROL, rol);
        editor.putString(KEY_AVATAR, avatarUrl);
        editor.apply();
    }

    public Long   getUserId() { return prefs.getLong(KEY_ID, -1); }
    public String getNick()   { return prefs.getString(KEY_NICK, null); }
    public String getNombre() { return prefs.getString(KEY_NOMBRE, null); }
    public String getApellidos() { return prefs.getString(KEY_APELLIDOS, null); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, null); }
    public String getFechaRegistro() { return prefs.getString(KEY_FECHA, null); }
    public String getRol()    { return prefs.getString(KEY_ROL, null); }
    public String getAvatarUrl() { return prefs.getString(KEY_AVATAR, "default_avatar"); }
}
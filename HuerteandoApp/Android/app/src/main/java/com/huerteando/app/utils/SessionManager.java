package com.huerteando.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager — Gestiona la sesión local del usuario (id, nick, nombre, rol).
 */
public class SessionManager {

    private static final String PREF_NAME = "HuerteandoSession";

    // Claves para el almacenamiento local
    private static final String KEY_ID     = "userId";
    private static final String KEY_NICK   = "nick";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_ROL    = "rol";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Guarda los datos del usuario recibidos en el login.
     */
    public void guardarDatosSimples(Long id, String nick, String nombre) {
        editor.putLong(KEY_ID, id);
        editor.putString(KEY_NICK, nick);
        editor.putString(KEY_NOMBRE, nombre);
        editor.apply();
    }

    public Long   getUserId() { return prefs.getLong(KEY_ID, -1); }
    public String getNick()   { return prefs.getString(KEY_NICK, null); }
    public String getNombre() { return prefs.getString(KEY_NOMBRE, null); }
    public String getRol()    { return prefs.getString(KEY_ROL, null); }

    public boolean haySesion() {
        return getUserId() != -1;
    }

    public void cerrarSesion() {
        editor.clear().apply();
    }
}
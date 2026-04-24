package com.huerteando.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.huerteando.app.clases.LoginResponse;

/**
 * SessionManager — Gestiona la sesión local del usuario (id, nick, nombre, rol).
 * 
 * Se ha eliminado toda la lógica relacionada con tokens JWT para adaptarla
 * al sistema de login básico del backend.
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
     * Guarda los datos del usuario tras un login exitoso.
     * Ya no se manejan ni guardan tokens.
     */
    public void guardarSesion(LoginResponse response) {
        editor.putLong(KEY_ID,      response.getId());
        editor.putString(KEY_NICK,  response.getNick());
        editor.putString(KEY_NOMBRE, response.getNombre());
        editor.putString(KEY_ROL,   response.getRol());
        editor.apply();
    }

    // --- 🚧 CÓDIGO DE PRUEBAS (BYPASS) ---
    public void guardarSesionManual() {
        editor.putLong(KEY_ID, 1L);
        editor.putString(KEY_NICK, "admin");
        editor.putString(KEY_NOMBRE, "Administrador Test");
        editor.putString(KEY_ROL, "ADMIN");
        editor.apply();
    }
    // ------------------------------------

    public Long   getUserId() { return prefs.getLong(KEY_ID, -1); }
    public String getNick()   { return prefs.getString(KEY_NICK, null); }
    public String getNombre() { return prefs.getString(KEY_NOMBRE, null); }
    public String getRol()    { return prefs.getString(KEY_ROL, null); }

    /**
     * Comprueba si hay una sesión activa verificando si el ID de usuario existe.
     */
    public boolean haySesion() {
        return getUserId() != -1;
    }

    /**
     * Cierra la sesión eliminando todos los datos del almacenamiento local.
     */
    public void cerrarSesion() {
        editor.clear().apply();
    }
}

package com.huerteando.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.huerteando.app.clases.LoginResponse;

// Guarda y recupera los datos de sesión usando SharedPreferences
// (almacenamiento clave-valor local en el dispositivo)
public class SessionManager {

    // Nombre del archivo de preferencias
    private static final String PREF_NAME = "HuerteandoSession";

    // Claves para cada dato guardado
    private static final String KEY_TOKEN = "token";
    private static final String KEY_NICK  = "nick";
    private static final String KEY_ROL   = "rol";
    private static final String KEY_ID    = "userId";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        // MODE_PRIVATE = solo esta app puede leer este archivo
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Guarda la sesión tras un login exitoso
    public void guardarSesion(LoginResponse response) {
        editor.putString(KEY_TOKEN, response.getToken());
        editor.putString(KEY_NICK,  response.getNick());
        editor.putString(KEY_ROL,   response.getRol());
        editor.putLong(KEY_ID,      response.getId());
        editor.apply(); // apply() es asíncrono, más eficiente que commit()
    }

    public String getToken()  {
        return prefs.getString(KEY_TOKEN, null);
    }
    public String getNick()   {
        return prefs.getString(KEY_NICK, null);
    }
    public String getRol()    {
        return prefs.getString(KEY_ROL, null);
    }
    public Long   getUserId() {
        return prefs.getLong(KEY_ID, -1);
    }

    // ¿Hay sesión activa? Si hay token, sí
    public boolean haySesion() {
        return getToken() != null;
    }

    // Cierra sesión borrando todos los datos guardados
    public void cerrarSesion() {
        editor.clear().apply();
    }
}
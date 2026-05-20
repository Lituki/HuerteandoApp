package com.huerteando.app.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Utilidad centralizada para manejar mensajes de error de la API.
 * Ideal para proyectos DAM: evita repetir código en cada Activity.
 */
public class ErrorUtils {

    /**
     * Muestra un mensaje amigable basado en el código de error HTTP.
     */
    public static String getMensajeError(int code) {
        switch (code) {
            case 400: return "Datos inválidos. Revisa el formato.";
            case 401: return "Credenciales incorrectas.";
            case 403: return "Acceso denegado. No tienes permisos.";
            case 404: return "Recurso no encontrado en el servidor.";
            case 409: return "Conflicto: El dato (nick o email) ya está en uso.";
            case 500: return "Error interno del servidor. Inténtalo más tarde.";
            default: return "Error inesperado (" + code + ").";
        }
    }

    /**
     * Muestra un Toast con el mensaje de error correspondiente.
     */
    public static void mostrarToastError(Context context, int code) {
        Toast.makeText(context, getMensajeError(code), Toast.LENGTH_LONG).show();
    }
}

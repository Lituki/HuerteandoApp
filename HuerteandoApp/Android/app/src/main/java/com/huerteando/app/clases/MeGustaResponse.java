package com.huerteando.app.clases;

/**
 * Respuesta del servidor al dar o quitar me gusta.
 */
public class MeGustaResponse {
    private boolean success;
    private int MeGustaTotales;
    private boolean tieneMeGusta;  // Si el usuario actual tiene me gusta

    public boolean isSuccess() { return success; }
    public int getMeGustaTotales() { return MeGustaTotales; }
    public boolean isTieneMeGusta() { return tieneMeGusta; }
}
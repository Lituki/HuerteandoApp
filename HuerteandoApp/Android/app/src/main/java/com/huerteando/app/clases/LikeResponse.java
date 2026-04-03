package com.huerteando.app.clases;

/**
 * Respuesta del servidor al dar o quitar like.
 */
public class LikeResponse {
    private boolean success;
    private int likesTotales;
    private boolean tieneLike;  // Si el usuario actual tiene like

    public boolean isSuccess() { return success; }
    public int getLikesTotales() { return likesTotales; }
    public boolean isTieneLike() { return tieneLike; }
}

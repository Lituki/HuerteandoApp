package com.huerteando.app.api;

import com.huerteando.app.clases.Comentario;
import com.huerteando.app.clases.ComentarioRequest;
import com.huerteando.app.clases.Imagen;
import com.huerteando.app.clases.LoginRequest;
import com.huerteando.app.clases.LoginResponse;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.clases.ObservacionRequest;
import com.huerteando.app.clases.RegistroRequest;
import com.huerteando.app.clases.Usuario;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * ApiService — Definición de endpoints sincronizada con el Backend.
 * Se ha eliminado el soporte para Multipart en favor de JSON con Base64.
 */
public interface ApiService {

    // ==================== AUTENTICACIÓN ====================

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<Usuario> registrar(@Body RegistroRequest request);

    // ==================== OBSERVACIONES ====================

    @GET("api/observaciones")
    Call<List<Observacion>> getObservaciones(
            @Query("tipo")     String idTipo,
            @Query("usuario")  Long idUsuario,
            @Query("orden")    String orden,
            @Query("busqueda") String busqueda
    );

    @GET("api/observaciones/{id}")
    Call<Observacion> getObservacion(@Path("id") Long id);

    @POST("api/observaciones")
    Call<Observacion> crearObservacion(@Body ObservacionRequest request);

    @PUT("api/observaciones/{id}")
    Call<Observacion> actualizarObservacion(
            @Path("id") Long id,
            @Body ObservacionRequest request
    );

    @DELETE("api/observaciones/{id}")
    Call<Void> eliminarObservacion(@Path("id") Long id);

    /**
     * Sube una imagen vinculada a una observación.
     * Envía un objeto Imagen en formato JSON con la imagen codificada en Base64.
     */
    @POST("api/observaciones/{id}/imagenes")
    Call<Imagen> subirImagen(
            @Path("id") Long idObservacion,
            @Body Imagen imagen
    );

    // ==================== LIKES ====================

    @POST("api/observaciones/{id}/likes")
    Call<Void> darLike(
            @Path("id") Long idObservacion,
            @Query("idUsuario") Long idUsuario
    );

    @DELETE("api/observaciones/{id}/likes")
    Call<Void> quitarLike(
            @Path("id") Long idObservacion,
            @Query("idUsuario") Long idUsuario
    );

    @GET("api/observaciones/{id}/likes/count")
    Call<Map<String, Long>> getLikeCount(@Path("id") Long idObservacion);

    @GET("api/observaciones/{id}/likes/existe")
    Call<Map<String, Boolean>> checkLikeExiste(
            @Path("id") Long idObservacion,
            @Query("idUsuario") Long idUsuario
    );

    // ==================== COMENTARIOS ====================

    @GET("api/observaciones/{id}/comentarios")
    Call<List<Comentario>> getComentarios(@Path("id") Long idObservacion);

    @POST("api/observaciones/{id}/comentarios")
    Call<Comentario> addComentario(
            @Path("id") Long idObservacion,
            @Body ComentarioRequest request
    );

    @DELETE("api/observaciones/{idObs}/comentarios/{idCom}")
    Call<Void> eliminarComentario(
            @Path("idObs") Long idObservacion,
            @Path("idCom") Long idComentario
    );

    // ==================== USUARIOS ====================

    @GET("api/usuarios/{id}")
    Call<Usuario> getUsuario(@Path("id") Long id);
}

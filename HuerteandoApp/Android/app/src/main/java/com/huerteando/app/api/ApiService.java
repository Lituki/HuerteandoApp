package com.huerteando.app.api;

import com.huerteando.app.clases.Comentario;
import com.huerteando.app.clases.Especie;
import com.huerteando.app.clases.Imagen;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.clases.RegistroRequest;
import com.huerteando.app.clases.TipoObservacion;
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

public interface ApiService {

    // ── TIPOS DE OBSERVACIÓN ──────────────────────────────────────────
    @GET("api/tipos-observacion")
    Call<List<TipoObservacion>> getTipos();

    @GET("api/tipos-observacion/{id}")
    Call<TipoObservacion> getTipo(@Path("id") short id);

    // ── OBSERVACIONES ─────────────────────────────────────────────────
    @GET("api/observaciones")
    Call<List<Observacion>> getObservaciones();

    @GET("api/observaciones")
    Call<List<Observacion>> getObservacionesPorTipo(@Query("tipo") Long idTipo);

    @GET("api/observaciones")
    Call<List<Observacion>> getObservacionesPorUsuario(@Query("usuario") Long idUsuario);

    @GET("api/observaciones")
    Call<List<Observacion>> getObservacionesPorEstado(@Query("estado_observacion") String estado);

    @GET("api/observaciones/{id}")
    Call<Observacion> getObservacion(@Path("id") Long id);

    @POST("api/observaciones")
    Call<Observacion> crearObservacion(@Body Observacion observacion);

    @PUT("api/observaciones/{id}")
    Call<Observacion> editarObservacion(@Path("id") Long id, @Body Observacion observacion);

    @DELETE("api/observaciones/{id}")
    Call<Void> borrarObservacion(@Path("id") Long id);

    // ── IMÁGENES ──────────────────────────────────────────────────────
    @GET("api/observaciones/{id}/imagenes")
    Call<List<Imagen>> getImagenes(@Path("id") Long idObservacion);

    @POST("api/observaciones/{id}/imagenes")
    Call<Imagen> subirImagen(@Path("id") Long idObservacion, @Body Imagen imagen);

    @DELETE("api/observaciones/{idObs}/imagenes/{idImg}")
    Call<Void> eliminarImagen(@Path("idObs") Long idObservacion, @Path("idImg") Long idImagen);

    // ── COMENTARIOS ───────────────────────────────────────────────────
    @GET("api/observaciones/{id}/comentarios")
    Call<List<Comentario>> getComentarios(@Path("id") Long idObservacion);

    @POST("api/observaciones/{id}/comentarios")
    Call<Comentario> crearComentario(@Path("id") Long idObservacion, @Body Comentario comentario);

    @DELETE("api/observaciones/{idObs}/comentarios/{idCom}")
    Call<Void> eliminarComentario(@Path("idObs") Long idObservacion, @Path("idCom") Long idComentario);

    // ── ME GUSTAS ─────────────────────────────────────────────────────
    @GET("api/observaciones/{id}/megustas/count")
    Call<Map<String, Long>> getMeGustasCount(@Path("id") Long idObservacion);

    @GET("api/observaciones/{id}/megustas/existe")
    Call<Map<String, Boolean>> checkMeGustaExiste(@Path("id") Long idObservacion, @Query("idUsuario") Long idUsuario);

    @POST("api/observaciones/{id}/megustas")
    Call<Void> darMeGusta(@Path("id") Long idObservacion, @Query("idUsuario") Long idUsuario);

    @DELETE("api/observaciones/{id}/megustas")
    Call<Void> quitarMeGusta(@Path("id") Long idObservacion, @Query("idUsuario") Long idUsuario);

    // ── USUARIOS ──────────────────────────────────────────────────────
    @POST("api/auth/register")
    Call<Usuario> registrar(@Body RegistroRequest request);

    @POST("api/auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> credenciales);

    @GET("api/usuarios/{id}")
    Call<Usuario> getPerfil(@Path("id") Long id);

    // ── ESPECIES ──────────────────────────────────────────────────────
    @GET("api/especies")
    Call<List<Especie>> getEspecies();

    @GET("api/especies/{id}")
    Call<Especie> getEspecie(@Path("id") Long id);

    @POST("api/especies")
    Call<Especie> crearEspecie(@Body Especie especie);

    @PUT("api/especies/{id}")
    Call<Especie> actualizarEspecie(@Path("id") Long id, @Body Especie especie);

    @DELETE("api/especies/{id}")
    Call<Void> eliminarEspecie(@Path("id") Long id);
}
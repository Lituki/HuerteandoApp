package com.huerteando.app.api;

import com.huerteando.app.clases.ActualizarPerfilRequest;
import com.huerteando.app.clases.Comentario;
import com.huerteando.app.clases.ComentarioRequest;
import com.huerteando.app.clases.ImagenResponse;
import com.huerteando.app.clases.LikeResponse;
import com.huerteando.app.clases.LoginRequest;
import com.huerteando.app.clases.LoginResponse;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.clases.ObservacionRequest;
import com.huerteando.app.clases.RegistroRequest;
import com.huerteando.app.clases.Usuario;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ==================== AUTENTICACIÓN ====================

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<Usuario> registrar(@Body RegistroRequest request);

    // ==================== OBSERVACIONES ====================

    @GET("api/observaciones")    Call<List<Observacion>> getObservaciones(
            @Query("tipo")     String tipo,
            @Query("estado")   String estado,
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

    @Multipart
    @POST("api/observaciones/{id}/imagenes")
    Call<ImagenResponse> subirImagenes(
            @Path("id") Long id,
            @Part List<MultipartBody.Part> imagenes
    );

    // ==================== LIKES ====================

    @POST("api/observaciones/{id}/like")
    Call<LikeResponse> darLike(@Path("id") Long id);

    @DELETE("api/observaciones/{id}/like")
    Call<LikeResponse> quitarLike(@Path("id") Long id);

    // ==================== COMENTARIOS ====================

    @GET("api/observaciones/{id}/comentarios")
    Call<List<Comentario>> getComentarios(@Path("id") Long idObservacion);

    @POST("api/observaciones/{id}/comentarios")
    Call<Comentario> addComentario(
            @Path("id") Long idObservacion,
            @Body ComentarioRequest request
    );

    @DELETE("api/comentarios/{id}")
    Call<Void> eliminarComentario(@Path("id") Long id);

    // ==================== USUARIOS ====================

    @GET("api/usuarios/me")
    Call<Usuario> getMiPerfil();

    @PUT("api/usuarios/me")
    Call<Usuario> actualizarPerfil(@Body ActualizarPerfilRequest request);

    @GET("api/usuarios/{id}")
    Call<Usuario> getUsuario(@Path("id") Long id);
}
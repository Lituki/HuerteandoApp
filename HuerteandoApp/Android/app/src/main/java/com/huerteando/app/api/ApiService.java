package com.huerteando.app.api;

import com.huerteando.app.clases.Comentario;
import com.huerteando.app.clases.LoginRequest;
import com.huerteando.app.clases.LoginResponse;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.clases.Usuario;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
import com.huerteando.app.clases.ActualizarPerfilRequest;
import com.huerteando.app.clases.ComentarioRequest;
import com.huerteando.app.clases.ImagenResponse;
import com.huerteando.app.clases.LikeResponse;
import com.huerteando.app.clases.ObservacionRequest;
import com.huerteando.app.clases.RegistroRequest;

public interface ApiService {

    // ==================== AUTENTICACIÓN ====================
    
    // Login — POST /api/auth/login
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Registro — POST /api/usuarios
    @POST("usuarios")
    Call<Usuario> registrar(@Body RegistroRequest request);

    // ==================== OBSERVACIONES ====================
    
    // Obtener todas las observaciones (con filtros opcionales)
    // GET /api/observaciones?tipo=PLANTA&estado=ABIERTA
    @GET("observaciones")
    Call<List<Observacion>> getObservaciones(
        @Query("tipo") String tipo,           // Filtrar por tipo (PLANTA, RINCON, DENUNCIA)
        @Query("estado") String estado,      // Filtrar por estado (ABIERTA, CERRADA)
        @Query("zona") String zona           // Filtrar por nombre de zona
    );

    // Obtener una observación por su ID
    // GET /api/observaciones/1
    @GET("observaciones/{id}")
    Call<Observacion> getObservacion(@Path("id") Long id);

    // Crear una nueva observación
    // POST /api/observaciones
    @POST("observaciones")
    Call<Observacion> crearObservacion(@Body ObservacionRequest request);

    // Actualizar una observación
    // PUT /api/observaciones/1
    @PUT("observaciones/{id}")
    Call<Observacion> actualizarObservacion(
        @Path("id") Long id,
        @Body ObservacionRequest request
    );

    // Eliminar una observación (solo ADMIN)
    // DELETE /api/observaciones/1
    @DELETE("observaciones/{id}")
    Call<Void> eliminarObservacion(@Path("id") Long id);

    // Subir imágenes de una observación (hasta 3)
    // POST /api/observaciones/1/imagenes
    @Multipart
    @POST("observaciones/{id}/imagenes")
    Call<ImagenResponse> subirImagenes(
        @Path("id") Long id,
        @Part List<MultipartBody.Part> imagenes
    );

    // ==================== likes (CORAZONES) ====================
    
    // Dar like a una observación
    // POST /api/observaciones/1/like
    @POST("observaciones/{id}/like")
    Call<LikeResponse> darLike(@Path("id") Long id);

    // Quitar like a una observación
    // DELETE /api/observaciones/1/like
    @DELETE("observaciones/{id}/like")
    Call<LikeResponse> quitarLike(@Path("id") Long id);

    // ==================== COMENTARIOS ====================
    
    // Obtener comentarios de una observación
    // GET /api/observaciones/1/comentarios
    @GET("observaciones/{id}/comentarios")
    Call<List<Comentario>> getComentarios(@Path("id") Long idObservacion);

    // Añadir un comentario
    // POST /api/observaciones/1/comentarios
    @POST("observaciones/{id}/comentarios")
    Call<Comentario> addComentario(
        @Path("id") Long idObservacion,
        @Body ComentarioRequest request
    );

    // Eliminar un comentario (solo ADMIN o autor)
    // DELETE /api/comentarios/1
    @DELETE("comentarios/{id}")
    Call<Void> eliminarComentario(@Path("id") Long id);

    // ==================== USUARIOS ====================
    
    // Obtener perfil del usuario actual
    // GET /api/usuarios/me
    @GET("usuarios/me")
    Call<Usuario> getMiPerfil();

    // Actualizar perfil
    // PUT /api/usuarios/me
    @PUT("usuarios/me")
    Call<Usuario> actualizarPerfil(@Body ActualizarPerfilRequest request);

    // Obtener usuario por ID (para ver perfiles de otros)
    // GET /api/usuarios/1
    @GET("usuarios/{id}")
    Call<Usuario> getUsuario(@Path("id") Long id);
}

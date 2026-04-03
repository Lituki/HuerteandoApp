package com.huerteando.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;

import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.Comentario;
import com.huerteando.app.clases.ComentarioRequest;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de Detalle - Muestra los detalles de una observación
 * 
 * ¿Qué hace esta clase?
 * 1. Carga los datos completos de una observación desde el servidor
 * 2. Muestra imágenes, descripción, ubicación
 * 3. Permite dar like
 * 4. Permite ver y añadir comentarios
 */
public class DetalleObservacionActivity extends AppCompatActivity {

    private long idObservacion;

    // Vistas
    private CollapsingToolbarLayout toolbarLayout;
    private ImageView ivImagen;
    private TextView tvTipo;
    private TextView tvDescripcion;
    private TextView tvZona;
    private TextView tvDireccion;
    private TextView tvFecha;
    private TextView tvAutor;
    private TextView tvLikes;
    private MaterialButton btnLike;

    // Comentarios
    private RecyclerView recyclerComentarios;
    private ComentarioAdapter adapter;
    private List<Comentario> comentarios = new ArrayList<>();
    private com.google.android.material.textfield.TextInputEditText editComentario;
    private MaterialButton btnEnviarComentario;

    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private Observacion observacionActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        sessionManager = new SessionManager(this);

        // Obtener ID de la observación
        idObservacion = getIntent().getLongExtra("idObservacion", -1);
        if (idObservacion == -1) {
            Toast.makeText(this, "Error: observación no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        toolbarLayout = findViewById(R.id.toolbarLayout);
        ivImagen = findViewById(R.id.ivImagen);
        tvTipo = findViewById(R.id.tvTipo);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvZona = findViewById(R.id.tvZona);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvFecha = findViewById(R.id.tvFecha);
        tvAutor = findViewById(R.id.tvAutor);
        tvLikes = findViewById(R.id.tvLikes);
        btnLike = findViewById(R.id.btnLike);
        recyclerComentarios = findViewById(R.id.recyclerComentarios);
        editComentario = findViewById(R.id.editComentario);
        btnEnviarComentario = findViewById(R.id.btnEnviarComentario);
        progressBar = findViewById(R.id.progressBar);

        // Configurar recycler de comentarios
        adapter = new ComentarioAdapter(comentarios);
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerComentarios.setAdapter(adapter);

        // Botón de like
        btnLike.setOnClickListener(v -> toggleLike());

        // Botón de enviar comentario
        btnEnviarComentario.setOnClickListener(v -> enviarComentario());

        // Cargar datos
        cargarObservacion();
        cargarComentarios();
    }

    private void cargarObservacion() {
        progressBar.setVisibility(View.VISIBLE);

        String token = sessionManager.getToken();
        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

        Call<Observacion> call = apiService.getObservacion(idObservacion);
        call.enqueue(new Callback<Observacion>() {
            @Override
            public void onResponse(Call<Observacion> call, Response<Observacion> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    observacionActual = response.body();
                    mostrarObservacion();
                } else {
                    Toast.makeText(DetalleObservacionActivity.this, "Error al cargar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Observacion> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetalleObservacionActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarObservacion() {
        toolbarLayout.setTitle(observacionActual.getTitulo());

        // Imagen principal
        if (observacionActual.getImagenesUrl() != null && !observacionActual.getImagenesUrl().isEmpty()) {
            Glide.with(this)
                    .load(observacionActual.getImagenesUrl().get(0))
                    .centerCrop()
                    .into(ivImagen);
        }

        // Tipo
        tvTipo.setText(observacionActual.getTipoObservacion());

        // Descripción
        tvDescripcion.setText(observacionActual.getDescripcion());

        // Ubicación
        tvZona.setText("Zona: " + (observacionActual.getNombreZona() != null ? observacionActual.getNombreZona() : "No especificada"));
        tvDireccion.setText(observacionActual.getDireccionTxt() != null ? observacionActual.getDireccionTxt() : "Sin dirección");

        // Fecha y autor
        tvFecha.setText("Fecha: " + observacionActual.getFechaObservacion());
        tvAutor.setText("Por: " + observacionActual.getAutorNick());

        // Likes
        actualizarBotonLike();
    }

    private void actualizarBotonLike() {
        if (observacionActual != null) {
            tvLikes.setText(String.valueOf(observacionActual.getNumLikes()));
            
            if (observacionActual.isLikePropio()) {
                btnLike.setIconResource(android.R.drawable.btn_star_big_on);
                btnLike.setText("Te gusta");
            } else {
                btnLike.setIconResource(android.R.drawable.btn_star_big_off);
                btnLike.setText("Me gusta");
            }
        }
    }

    private void toggleLike() {
        if (observacionActual == null) return;

        String token = sessionManager.getToken();
        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

        // Si ya tiene like → quitarlo; si no → dárselo
        Call<?> call;
        if (observacionActual.isLikePropio()) {
            call = apiService.quitarLike(idObservacion);
        } else {
            call = apiService.darLike(idObservacion);
        }

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    // Recargar para ver el estado actualizado
                    cargarObservacion();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(DetalleObservacionActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarComentarios() {
        String token = sessionManager.getToken();
        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

        Call<List<Comentario>> call = apiService.getComentarios(idObservacion);
        call.enqueue(new Callback<List<Comentario>>() {
            @Override
            public void onResponse(Call<List<Comentario>> call, Response<List<Comentario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    comentarios.clear();
                    comentarios.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Comentario>> call, Throwable t) {
                // Silencioso
            }
        });
    }

    private void enviarComentario() {
        String contenido = editComentario.getText() != null ? editComentario.getText().toString().trim() : "";
        if (contenido.isEmpty()) {
            Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.getToken();
        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

        ComentarioRequest request = new ComentarioRequest(contenido);
        Call<Comentario> call = apiService.addComentario(idObservacion, request);

        call.enqueue(new Callback<Comentario>() {
            @Override
            public void onResponse(Call<Comentario> call, Response<Comentario> response) {
                if (response.isSuccessful()) {
                    editComentario.setText("");
                    cargarComentarios();  // Recargar comentarios
                } else {
                    Toast.makeText(DetalleObservacionActivity.this, "Error al enviar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Comentario> call, Throwable t) {
                Toast.makeText(DetalleObservacionActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

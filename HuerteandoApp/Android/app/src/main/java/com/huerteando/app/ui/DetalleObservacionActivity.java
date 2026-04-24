package com.huerteando.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.huerteando.app.R;
import com.huerteando.app.adapter.ComentarioAdapter;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.Comentario;
import com.huerteando.app.clases.ComentarioRequest;
import com.huerteando.app.clases.LikeResponse;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de detalle de una observación.
 *
 * Layout: activity_detalle_observacion.xml
 *
 * RF cubiertos: RF-10 (ver por ID), RF-34/36 (comentarios),
 *               RF-38/40/41 (likes).
 *
 * Recibe el ID en el extra "idObservacion" (Long).
 */
public class DetalleObservacionActivity extends AppCompatActivity {

    // ─── IDs del layout activity_detalle_observacion.xml ──────────────────────
    private android.widget.ImageView ivDetalleImagen;
    private ProgressBar       progressDetalle;
    private TextView          tvDetalleTipo;
    private TextView          tvDetalleTitulo;
    private TextView          tvDetalleFecha;
    private TextView          tvDetalleDescripcion;
    private TextView          tvDetalleZona;
    private TextView          tvDetalleNombreTradicional;
    private TextView          tvDetalleEspecie;
    private TextView          tvDetalleNumLikes;
    private MaterialButton    btnLike;
    private RecyclerView      recyclerComentarios;
    private com.google.android.material.textfield.TextInputEditText editNuevoComentario;
    private MaterialButton    btnEnviarComentario;

    // ─── Datos ────────────────────────────────────────────────────────────────
    private long              idObservacion;
    private Observacion       observacionActual;
    private ComentarioAdapter adapterComentarios;
    private final List<Comentario>  comentarios = new ArrayList<>();
    private SessionManager session;
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_observacion);

        // CONFIGURACIÓN DE TOOLBAR (NUEVO)
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Botón atrás
            getSupportActionBar().setTitle(""); // El título lo maneja el CollapsingToolbar
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        session = new SessionManager(this);

        // Obtener ID de la observación
        idObservacion = getIntent().getLongExtra("idObservacion", -1L);
        if (idObservacion == -1L) {
            Toast.makeText(this, "Error: observación no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Enlazar vistas
        ivDetalleImagen            = findViewById(R.id.ivDetalleImagen);
        progressDetalle            = findViewById(R.id.progressDetalle);
        tvDetalleTipo              = findViewById(R.id.tvDetalleTipo);
        tvDetalleTitulo            = findViewById(R.id.tvDetalleTitulo);
        tvDetalleFecha             = findViewById(R.id.tvDetalleFecha);
        tvDetalleDescripcion       = findViewById(R.id.tvDetalleDescripcion);
        tvDetalleZona              = findViewById(R.id.tvDetalleZona);
        tvDetalleNombreTradicional = findViewById(R.id.tvDetalleNombreTradicional);
        tvDetalleEspecie           = findViewById(R.id.tvDetalleEspecie);
        tvDetalleNumLikes          = findViewById(R.id.tvDetalleNumLikes);
        btnLike                    = findViewById(R.id.btnLike);
        recyclerComentarios        = findViewById(R.id.recyclerComentarios);
        editNuevoComentario        = findViewById(R.id.editNuevoComentario);
        btnEnviarComentario        = findViewById(R.id.btnEnviarComentario);

        // RecyclerView de comentarios
        adapterComentarios = new ComentarioAdapter(comentarios);
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerComentarios.setAdapter(adapterComentarios);

        btnLike.setOnClickListener(v -> toggleLike());
        btnEnviarComentario.setOnClickListener(v -> enviarComentario());

        // Cargar datos
        cargarObservacion();
        cargarComentarios();
    }

    // ─── Carga de la observación ─────────────────────────────────────────────

    private void cargarObservacion() {
        progressDetalle.setVisibility(View.VISIBLE);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getObservacion(idObservacion).enqueue(new Callback<Observacion>() {
            @Override
            public void onResponse(Call<Observacion> call, Response<Observacion> response) {
                progressDetalle.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    observacionActual = response.body();
                    mostrarObservacion();
                } else {
                    Toast.makeText(DetalleObservacionActivity.this,
                            "Error al cargar la observación", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Observacion> call, Throwable t) {
                progressDetalle.setVisibility(View.GONE);
                Toast.makeText(DetalleObservacionActivity.this,
                        "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarObservacion() {
        Observacion o = observacionActual;

        // Tipo
        tvDetalleTipo.setText(o.getTipoObservacion());
        tvDetalleTipo.setTextColor(colorTipo(o.getTipoObservacion()));

        // Título: Lo ponemos en el texto y también en la Toolbar expansiva
        tvDetalleTitulo.setText(o.getTitulo());
        com.google.android.material.appbar.CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitle(o.getTitulo());
        }

        // Fecha
        tvDetalleFecha.setText("📅 " + (o.getFechaObservacion() != null ? o.getFechaObservacion() : ""));

        // Descripción
        tvDetalleDescripcion.setText(
                o.getDescripcion() != null && !o.getDescripcion().isEmpty()
                        ? o.getDescripcion() : "Sin descripción");

        // Zona (opcional)
        if (o.getNombreZona() != null && !o.getNombreZona().isEmpty()) {
            tvDetalleZona.setText("📍 " + o.getNombreZona());
            tvDetalleZona.setVisibility(View.VISIBLE);
        } else {
            tvDetalleZona.setVisibility(View.GONE);
        }

        // Nombre tradicional (opcional)
        if (o.getNombreTradicional() != null && !o.getNombreTradicional().isEmpty()) {
            tvDetalleNombreTradicional.setText("«" + o.getNombreTradicional() + "»");
            tvDetalleNombreTradicional.setVisibility(View.VISIBLE);
        } else {
            tvDetalleNombreTradicional.setVisibility(View.GONE);
        }

        // Especie (solo PLANTA, opcional)
        if ("PLANTA".equals(o.getTipoObservacion())
                && o.getEspecieNombre() != null
                && !o.getEspecieNombre().isEmpty()) {
            tvDetalleEspecie.setText("🌿 " + o.getEspecieNombre());
            tvDetalleEspecie.setVisibility(View.VISIBLE);
        } else {
            tvDetalleEspecie.setVisibility(View.GONE);
        }
        // Cargar imagen con Glide si existe
        if (o.getImagenesUrl() != null && !o.getImagenesUrl().isEmpty()) {
            ivDetalleImagen.setVisibility(View.VISIBLE);

            // Suponemos que mostramos la primera imagen de la lista
            String urlImagen = o.getImagenesUrl().get(0);

            // Si la URL es relativa, añade la BASE_URL de tu API
            // Si ya viene completa desde el servidor, déjala así:
            com.bumptech.glide.Glide.with(this)
                    .load(urlImagen)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivDetalleImagen);
        } else {
            ivDetalleImagen.setVisibility(View.GONE);
        }

        // Likes
        actualizarBotonLike();
    }

    // ─── Likes ───────────────────────────────────────────────────────────────

    private void actualizarBotonLike() {
        if (observacionActual == null) return;
        tvDetalleNumLikes.setText(String.valueOf(observacionActual.getNumLikes()));
        if (observacionActual.isLikePropio()) {
            btnLike.setText("❤️ Te gusta");
        } else {
            btnLike.setText("🤍 Me gusta");
        }
    }

    private void toggleLike() {
        if (observacionActual == null) return;

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<LikeResponse> call;

        if (observacionActual.isLikePropio()) {
            call = api.quitarLike(idObservacion);
        } else {
            call = api.darLike(idObservacion);
        }

        call.enqueue(new Callback<LikeResponse>() {
            @Override
            public void onResponse(Call<LikeResponse> c, Response<LikeResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    // Actualizar estado local sin recargar toda la observación
                    observacionActual.setNumLikes(r.body().getLikesTotales());
                    observacionActual.setLikePropio(r.body().isTieneLike());
                    actualizarBotonLike();
                }
            }

            @Override
            public void onFailure(Call<LikeResponse> c, Throwable t) {
                Toast.makeText(DetalleObservacionActivity.this,
                        "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Comentarios ─────────────────────────────────────────────────────────

    private void cargarComentarios() {
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getComentarios(idObservacion).enqueue(new Callback<List<Comentario>>() {
            @Override
            public void onResponse(Call<List<Comentario>> call, Response<List<Comentario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    comentarios.clear();
                    comentarios.addAll(response.body());
                    adapterComentarios.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Comentario>> call, Throwable t) { /* silencioso */ }
        });
    }

    private void enviarComentario() {
        String contenido = editNuevoComentario.getText() != null
                ? editNuevoComentario.getText().toString().trim() : "";

        if (contenido.isEmpty()) {
            Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.addComentario(idObservacion, new ComentarioRequest(contenido))
                .enqueue(new Callback<Comentario>() {
                    @Override
                    public void onResponse(Call<Comentario> call, Response<Comentario> response) {
                        if (response.isSuccessful()) {
                            editNuevoComentario.setText("");
                            cargarComentarios();
                        } else {
                            Toast.makeText(DetalleObservacionActivity.this,
                                    "Error al enviar comentario", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Comentario> call, Throwable t) {
                        Toast.makeText(DetalleObservacionActivity.this,
                                "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─── Utilidades ──────────────────────────────────────────────────────────

    private int colorTipo(String tipo) {
        if (tipo == null) return 0xFF888888;
        switch (tipo) {
            case "PLANTA":   return 0xFF4CAF50;
            case "RINCON":   return 0xFF2196F3;
            case "DENUNCIA": return 0xFFF44336;
            default:         return 0xFF888888;
        }
    }
}

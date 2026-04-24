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
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de detalle de una observación.
 */
public class DetalleObservacionActivity extends AppCompatActivity {

    private android.widget.ImageView ivDetalleImagen;
    private ProgressBar       progressDetalle;
    private TextView          tvDetalleTipo, tvDetalleTitulo, tvDetalleFecha;
    private TextView          tvDetalleDescripcion, tvDetalleZona, tvDetalleNombreTradicional;
    private TextView          tvDetalleEspecie, tvDetalleNumLikes;
    private MaterialButton    btnLike;
    private RecyclerView      recyclerComentarios;
    private com.google.android.material.textfield.TextInputEditText editNuevoComentario;
    private MaterialButton    btnEnviarComentario;

    private long              idObservacion;
    private Observacion       observacionActual;
    private ComentarioAdapter adapterComentarios;
    private final List<Comentario> comentarios = new ArrayList<>();
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_observacion);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        session = new SessionManager(this);

        idObservacion = getIntent().getLongExtra("idObservacion", -1L);
        if (idObservacion == -1L) {
            Toast.makeText(this, "Error: observación no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        enlazarVistas();

        adapterComentarios = new ComentarioAdapter(comentarios);
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerComentarios.setAdapter(adapterComentarios);

        btnLike.setOnClickListener(v -> toggleLike());
        btnEnviarComentario.setOnClickListener(v -> enviarComentario());

        cargarDatosCompletos();
    }

    private void enlazarVistas() {
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
    }

    private void cargarDatosCompletos() {
        cargarObservacion();
        cargarComentarios();
    }

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
                    // Una vez cargada la obs, pedimos el estado real del Like
                    actualizarEstadoLikeServidor();
                }
            }
            @Override public void onFailure(Call<Observacion> call, Throwable t) {
                progressDetalle.setVisibility(View.GONE);
            }
        });
    }

    private void actualizarEstadoLikeServidor() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Long idUsuario = session.getUserId();

        // 1. Conteo total
        api.getLikeCount(idObservacion).enqueue(new Callback<Map<String, Long>>() {
            @Override
            public void onResponse(Call<Map<String, Long>> call, Response<Map<String, Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long total = response.body().get("likes");
                    if (total != null && observacionActual != null) {
                        observacionActual.setNumLikes(total.intValue());
                        actualizarBotonLike();
                    }
                }
            }
            @Override public void onFailure(Call<Map<String, Long>> call, Throwable t) {}
        });

        // 2. ¿Tengo like yo?
        if (idUsuario != -1L) {
            api.checkLikeExiste(idObservacion, idUsuario).enqueue(new Callback<Map<String, Boolean>>() {
                @Override
                public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Boolean existe = response.body().get("yaLikeado");
                        if (existe != null && observacionActual != null) {
                            observacionActual.setLikePropio(existe);
                            actualizarBotonLike();
                        }
                    }
                }
                @Override public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {}
            });
        }
    }

    private void mostrarObservacion() {
        Observacion o = observacionActual;
        tvDetalleTipo.setText(o.getTipoObservacion());
        tvDetalleTitulo.setText(o.getTitulo());
        tvDetalleFecha.setText("📅 " + (o.getFechaObservacion() != null ? o.getFechaObservacion() : ""));
        tvDetalleDescripcion.setText(o.getDescripcion() != null && !o.getDescripcion().isEmpty() ? o.getDescripcion() : "Sin descripción");

        if (o.getNombreZona() != null && !o.getNombreZona().isEmpty()) {
            tvDetalleZona.setText("📍 " + o.getNombreZona());
            tvDetalleZona.setVisibility(View.VISIBLE);
        } else tvDetalleZona.setVisibility(View.GONE);

        if (o.getImagenesUrl() != null && !o.getImagenesUrl().isEmpty()) {
            ivDetalleImagen.setVisibility(View.VISIBLE);
            com.bumptech.glide.Glide.with(this).load(o.getImagenesUrl().get(0)).into(ivDetalleImagen);
        } else ivDetalleImagen.setVisibility(View.GONE);

        actualizarBotonLike();
    }

    private void actualizarBotonLike() {
        if (observacionActual == null) return;
        tvDetalleNumLikes.setText(String.valueOf(observacionActual.getNumLikes()));
        btnLike.setText(observacionActual.isLikePropio() ? "❤️ Te gusta" : "🤍 Me gusta");
    }

    private void toggleLike() {
        if (observacionActual == null) return;

        Long idUsuario = session.getUserId();
        if (idUsuario == -1L) {
            Toast.makeText(this, "Inicia sesión para dar like", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLike.setEnabled(false); // Bloqueamos para evitar spam
        ApiService api = ApiClient.getClient().create(ApiService.class);
        final boolean accionEsQuitar = observacionActual.isLikePropio();

        Call<Void> call = accionEsQuitar ? api.quitarLike(idObservacion, idUsuario) : api.darLike(idObservacion, idUsuario);
        
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnLike.setEnabled(true);
                if (response.isSuccessful() || response.code() == 409) {
                    // Si es 409 (Ya existía), simplemente refrescamos para sincronizar
                    actualizarEstadoLikeServidor();
                } else {
                    Toast.makeText(DetalleObservacionActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                btnLike.setEnabled(true);
                Toast.makeText(DetalleObservacionActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
            @Override public void onFailure(Call<List<Comentario>> call, Throwable t) {}
        });
    }

    private void enviarComentario() {
        String contenido = editNuevoComentario.getText() != null ? editNuevoComentario.getText().toString().trim() : "";
        if (contenido.isEmpty()) return;

        Long idUsuario = session.getUserId();
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.addComentario(idObservacion, new ComentarioRequest(contenido, idUsuario))
                .enqueue(new Callback<Comentario>() {
                    @Override
                    public void onResponse(Call<Comentario> call, Response<Comentario> response) {
                        if (response.isSuccessful()) {
                            editNuevoComentario.setText("");
                            cargarComentarios();
                        }
                    }
                    @Override public void onFailure(Call<Comentario> call, Throwable t) {}
                });
    }
}

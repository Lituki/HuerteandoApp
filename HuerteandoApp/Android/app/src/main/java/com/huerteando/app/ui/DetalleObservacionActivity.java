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
 */
public class DetalleObservacionActivity extends AppCompatActivity {

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

    private long              idObservacion;
    private Observacion       observacionActual;
    private ComentarioAdapter adapterComentarios;
    private final List<Comentario>  comentarios = new ArrayList<>();
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

        adapterComentarios = new ComentarioAdapter(comentarios);
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerComentarios.setAdapter(adapterComentarios);

        btnLike.setOnClickListener(v -> toggleLike());
        btnEnviarComentario.setOnClickListener(v -> enviarComentario());

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
                } else {
                    Toast.makeText(DetalleObservacionActivity.this,
                            "Error al cargar la observación", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Observacion> call, Throwable t) {
                progressDetalle.setVisibility(View.GONE);
                Toast.makeText(DetalleObservacionActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarObservacion() {
        Observacion o = observacionActual;
        tvDetalleTipo.setText(o.getTipoObservacion());
        tvDetalleTipo.setTextColor(colorTipo(o.getTipoObservacion()));
        tvDetalleTitulo.setText(o.getTitulo());
        com.google.android.material.appbar.CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        if (collapsingToolbar != null) collapsingToolbar.setTitle(o.getTitulo());
        tvDetalleFecha.setText("📅 " + (o.getFechaObservacion() != null ? o.getFechaObservacion() : ""));
        tvDetalleDescripcion.setText(o.getDescripcion() != null && !o.getDescripcion().isEmpty() ? o.getDescripcion() : "Sin descripción");

        if (o.getNombreZona() != null && !o.getNombreZona().isEmpty()) {
            tvDetalleZona.setText("📍 " + o.getNombreZona());
            tvDetalleZona.setVisibility(View.VISIBLE);
        } else tvDetalleZona.setVisibility(View.GONE);

        if (o.getNombreTradicional() != null && !o.getNombreTradicional().isEmpty()) {
            tvDetalleNombreTradicional.setText("«" + o.getNombreTradicional() + "»");
            tvDetalleNombreTradicional.setVisibility(View.VISIBLE);
        } else tvDetalleNombreTradicional.setVisibility(View.GONE);

        if ("PLANTA".equals(o.getTipoObservacion()) && o.getEspecieNombre() != null && !o.getEspecieNombre().isEmpty()) {
            tvDetalleEspecie.setText("🌿 " + o.getEspecieNombre());
            tvDetalleEspecie.setVisibility(View.VISIBLE);
        } else tvDetalleEspecie.setVisibility(View.GONE);

        if (o.getImagenesUrl() != null && !o.getImagenesUrl().isEmpty()) {
            ivDetalleImagen.setVisibility(View.VISIBLE);
            com.bumptech.glide.Glide.with(this).load(o.getImagenesUrl().get(0))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image).into(ivDetalleImagen);
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
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<LikeResponse> call = observacionActual.isLikePropio() ? api.quitarLike(idObservacion) : api.darLike(idObservacion);
        call.enqueue(new Callback<LikeResponse>() {
            @Override
            public void onResponse(Call<LikeResponse> c, Response<LikeResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    observacionActual.setNumLikes(r.body().getLikesTotales());
                    observacionActual.setLikePropio(r.body().isTieneLike());
                    actualizarBotonLike();
                } else {
                    Toast.makeText(DetalleObservacionActivity.this, "Error: " + r.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LikeResponse> c, Throwable t) {
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
            @Override
            public void onFailure(Call<List<Comentario>> call, Throwable t) {}
        });
    }

    private void enviarComentario() {
        String contenido = editNuevoComentario.getText() != null ? editNuevoComentario.getText().toString().trim() : "";
        if (contenido.isEmpty()) {
            Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show();
            return;
        }

        Long idUsuario = session.getUserId();
        if (idUsuario == -1L) {
            Toast.makeText(this, "Inicia sesión para comentar", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.addComentario(idObservacion, new ComentarioRequest(contenido, idUsuario))
                .enqueue(new Callback<Comentario>() {
                    @Override
                    public void onResponse(Call<Comentario> call, Response<Comentario> response) {
                        if (response.isSuccessful()) {
                            editNuevoComentario.setText("");
                            cargarComentarios();
                        } else {
                            Toast.makeText(DetalleObservacionActivity.this, "Error al enviar: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Comentario> call, Throwable t) {
                        Toast.makeText(DetalleObservacionActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int colorTipo(String tipo) {
        if (tipo == null) return 0xFF888888;
        switch (tipo) {
            case "PLANTA": return 0xFF4CAF50;
            case "RINCON": return 0xFF2196F3;
            case "DENUNCIA": return 0xFFF44336;
            default: return 0xFF888888;
        }
    }
}

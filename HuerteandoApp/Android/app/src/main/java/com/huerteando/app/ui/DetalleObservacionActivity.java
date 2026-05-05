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
import com.huerteando.app.clases.Imagen;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.clases.Usuario;
import com.huerteando.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de detalle de una observación.
 * Adaptada al nuevo manual.
 */
public class DetalleObservacionActivity extends AppCompatActivity {

    private android.widget.ImageView ivDetalleImagen;
    private ProgressBar progressDetalle;
    private TextView tvDetalleTipo, tvDetalleTitulo, tvDetalleFecha;
    private TextView tvDetalleDescripcion, tvDetalleZona, tvDetalleEspecie, tvDetalleNumMeGusta;
    private MaterialButton btnMeGusta;
    private RecyclerView recyclerComentarios;
    private com.google.android.material.textfield.TextInputEditText editNuevoComentario;
    private MaterialButton btnEnviarComentario;

    private long idObservacion;
    private Observacion observacionActual;
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
        setupRecyclerView();

        btnMeGusta.setOnClickListener(v -> toggleMeGusta());
        btnEnviarComentario.setOnClickListener(v -> enviarComentario());

        cargarDatosCompletos();
    }

    private void enlazarVistas() {
        ivDetalleImagen = findViewById(R.id.ivDetalleImagen);
        progressDetalle = findViewById(R.id.progressDetalle);
        tvDetalleTipo = findViewById(R.id.tvDetalleTipo);
        tvDetalleTitulo = findViewById(R.id.tvDetalleTitulo);
        tvDetalleFecha = findViewById(R.id.tvDetalleFecha);
        tvDetalleDescripcion = findViewById(R.id.tvDetalleDescripcion);
        tvDetalleZona = findViewById(R.id.tvDetalleZona);
        tvDetalleEspecie = findViewById(R.id.tvDetalleEspecie);
        tvDetalleNumMeGusta = findViewById(R.id.tvDetalleNumMegusta);
        btnMeGusta = findViewById(R.id.btnMeGusta);
        recyclerComentarios = findViewById(R.id.recyclerComentarios);
        editNuevoComentario = findViewById(R.id.editNuevoComentario);
        btnEnviarComentario = findViewById(R.id.btnEnviarComentario);
    }

    private void setupRecyclerView() {
        adapterComentarios = new ComentarioAdapter(comentarios, session.getUserId(), new ComentarioAdapter.OnComentarioActionListener() {
            @Override
            public void onDelete(Comentario comentario) {
                confirmarEliminarComentario(comentario);
            }
        });
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerComentarios.setAdapter(adapterComentarios);
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
                    actualizarEstadoMeGustaServidor();
                }
            }
            @Override public void onFailure(Call<Observacion> call, Throwable t) {
                progressDetalle.setVisibility(View.GONE);
                Toast.makeText(DetalleObservacionActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarObservacion() {
        Observacion o = observacionActual;
        if (o.getTipoObservacion() != null) {
            tvDetalleTipo.setText(o.getTipoObservacion().getNombre());
        }
        tvDetalleTitulo.setText(o.getTitulo());
        tvDetalleFecha.setText("📅 " + formatearFecha(o.getFechaObservacion()));
        tvDetalleDescripcion.setText(o.getDescripcion() != null ? o.getDescripcion() : "Sin descripción");
        tvDetalleZona.setText(o.getNombreZona() != null ? "📍 " + o.getNombreZona() : "Ubicación desconocida");
        
        if (o.getEspecie() != null) {
            tvDetalleEspecie.setText("🌿 " + o.getEspecie().getNombreComun());
            tvDetalleEspecie.setVisibility(View.VISIBLE);
        }

        // Imagen
        if (o.getImagenes() != null && !o.getImagenes().isEmpty()) {
            String url = o.getImagenes().get(0).getUrlArchivo();
            if (url != null) {
                if (!url.startsWith("http")) {
                    String base = ApiClient.BASE_URL;
                    if (base.endsWith("/") && url.startsWith("/")) url = base + url.substring(1);
                    else if (!base.endsWith("/") && !url.startsWith("/")) url = base + "/" + url;
                    else url = base + url;
                }
                
                com.bumptech.glide.Glide.with(this)
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivDetalleImagen);
            }
        }

        actualizarBotonMeGusta();
    }

    private String formatearFecha(String fechaIso) {
        if (fechaIso == null) return "";
        try {
            String limpia = fechaIso.split("\\.")[0].replace("T", " ");
            SimpleDateFormat sdfIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdfIso.parse(limpia);
            Locale localeES = new Locale("es", "ES");
            SimpleDateFormat sdfSalida = new SimpleDateFormat("dd 'de' MMMM, yyyy 'a las' HH:mm", localeES);
            return sdfSalida.format(date);
        } catch (Exception e) {
            return fechaIso;
        }
    }

    private void actualizarEstadoMeGustaServidor() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Long idUser = session.getUserId();

        api.getMeGustasCount(idObservacion).enqueue(new Callback<Map<String, Long>>() {
            @Override
            public void onResponse(Call<Map<String, Long>> call, Response<Map<String, Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long total = response.body().get("megustas");
                    if (total != null) {
                        observacionActual.setNumMeGustas(total.intValue());
                        actualizarBotonMeGusta();
                    }
                }
            }
            @Override public void onFailure(Call<Map<String, Long>> call, Throwable t) {}
        });

        if (idUser != -1L) {
            api.checkMeGustaExiste(idObservacion, idUser).enqueue(new Callback<Map<String, Boolean>>() {
                @Override
                public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Boolean existe = response.body().get("yaMeGusta");
                        if (existe != null) {
                            observacionActual.setMeGustaPropio(existe);
                            actualizarBotonMeGusta();
                        }
                    }
                }
                @Override public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {}
            });
        }
    }

    private void actualizarBotonMeGusta() {
        if (observacionActual == null) return;
        btnMeGusta.setText("Me gusta");
        if (observacionActual.isMeGustaPropio()) {
            btnMeGusta.setIconResource(R.drawable.ic_heart_full);
            btnMeGusta.setIconTint(android.content.res.ColorStateList.valueOf(android.graphics.Color.RED));
        } else {
            btnMeGusta.setIconResource(R.drawable.ic_heart_empty);
            btnMeGusta.setIconTint(null);
        }
        tvDetalleNumMeGusta.setText(String.valueOf(observacionActual.getNumMeGustas()));
    }

    private void toggleMeGusta() {
        if (observacionActual == null) return;
        Long idUser = session.getUserId();
        if (idUser == -1L) {
            Toast.makeText(this, "Inicia sesión para participar", Toast.LENGTH_SHORT).show();
            return;
        }

        btnMeGusta.setEnabled(false);
        ApiService api = ApiClient.getClient().create(ApiService.class);
        boolean yaDabaMeGusta = observacionActual.isMeGustaPropio();
        
        Call<Void> call = yaDabaMeGusta ? api.quitarMeGusta(idObservacion, idUser) : api.darMeGusta(idObservacion, idUser);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnMeGusta.setEnabled(true);
                if (response.isSuccessful() || response.code() == 409) {
                    boolean nuevoEstado = !yaDabaMeGusta;
                    observacionActual.setMeGustaPropio(nuevoEstado);
                    int numActual = observacionActual.getNumMeGustas();
                    observacionActual.setNumMeGustas(nuevoEstado ? numActual + 1 : Math.max(0, numActual - 1));
                    actualizarBotonMeGusta();
                    actualizarEstadoMeGustaServidor();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                btnMeGusta.setEnabled(true);
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

    private void confirmarEliminarComentario(Comentario comentario) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Comentario")
                .setMessage("¿Estás seguro de que deseas eliminar este comentario?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    ApiService api = ApiClient.getClient().create(ApiService.class);
                    api.eliminarComentario(idObservacion, comentario.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                cargarComentarios();
                                Toast.makeText(DetalleObservacionActivity.this, "Comentario eliminado", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void enviarComentario() {
        String texto = editNuevoComentario.getText() != null ? editNuevoComentario.getText().toString().trim() : "";
        if (texto.isEmpty()) return;

        Long idUser = session.getUserId();
        if (idUser == -1L) {
            Toast.makeText(this, "Inicia sesión para comentar", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Comentario c = new Comentario();
        c.setContenido(texto);
        Usuario u = new Usuario();
        u.setId(idUser);
        c.setUsuario(u);

        api.crearComentario(idObservacion, c).enqueue(new Callback<Comentario>() {
            @Override
            public void onResponse(Call<Comentario> call, Response<Comentario> response) {
                if (response.isSuccessful()) {
                    editNuevoComentario.setText("");
                    cargarComentarios();
                }
            }
            @Override public void onFailure(Call<Comentario> call, Throwable t) {
                Toast.makeText(DetalleObservacionActivity.this, "Error al comentar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
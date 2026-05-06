package com.huerteando.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.viewpager2.widget.ViewPager2;
import com.huerteando.app.adapter.ImageCarouselAdapter;
import com.huerteando.app.R;
import com.huerteando.app.adapter.ComentarioAdapter;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.Comentario;
import com.huerteando.app.clases.Imagen;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.clases.Usuario;
import com.huerteando.app.utils.ErrorUtils;
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

    private static final String TAG = "DetalleObservacionActivity";

    private android.widget.ImageView ivDetalleImagen;
    private ProgressBar progressDetalle;
    private TextView tvDetalleTipo, tvDetalleTitulo, tvDetalleFecha;
    private TextView tvDetalleDescripcion, tvDetalleZona, tvDetalleEspecie, tvDetalleNumMeGusta;
    private ViewPager2 viewPagerImagenes;
    private TabLayout tabDots;
    private MaterialButton btnMeGusta, btnBorrarImagen;
    private RecyclerView recyclerComentarios;
    private com.google.android.material.textfield.TextInputEditText editNuevoComentario;
    private MaterialButton btnEnviarComentario;

    private long idObservacion;
    private String imagenLocal;
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

        // Depuración de Intent para verificar extras
        Log.d(TAG, "Iniciando DetalleObservacionActivity...");
        if (getIntent() != null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                Log.d(TAG, "Extras encontrados en el Intent:");
                for (String key : extras.keySet()) {
                    Log.d(TAG, " - " + key + ": " + extras.get(key));
                }
            } else {
                Log.d(TAG, "El Intent no tiene extras (getExtras() es null)");
            }
        }

        idObservacion = getIntent().getLongExtra("idObservacion", -1L);
        imagenLocal = getIntent().getStringExtra("imagen"); // Recepción segura

        if (idObservacion == -1L) {
            Toast.makeText(this, "Error: observación no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        enlazarVistas();
        setupRecyclerView();

        btnMeGusta.setOnClickListener(v -> toggleMeGusta());
        btnEnviarComentario.setOnClickListener(v -> enviarComentario());
        if (btnBorrarImagen != null) {
            btnBorrarImagen.setOnClickListener(v -> confirmarEliminarImagen());
        }

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
        viewPagerImagenes = findViewById(R.id.viewPagerImagenes);
        tabDots = findViewById(R.id.tabDots);
        btnMeGusta = findViewById(R.id.btnMeGusta);
        btnBorrarImagen = findViewById(R.id.btnBorrarImagen);
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
        Log.d(TAG, "Cargando detalle de observación ID: " + idObservacion);
        progressDetalle.setVisibility(View.VISIBLE);
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getObservacion(idObservacion).enqueue(new Callback<Observacion>() {
            @Override
            public void onResponse(Call<Observacion> call, Response<Observacion> response) {
                progressDetalle.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Detalle cargado correctamente");
                    observacionActual = response.body();
                    mostrarObservacion();
                    actualizarEstadoMeGustaServidor();
                    invalidateOptionsMenu(); // Actualizar menú basado en el dueño
                } else {
                    Log.e(TAG, "Error al cargar observación: " + response.code());
                    ErrorUtils.mostrarToastError(DetalleObservacionActivity.this, response.code());
                }
            }
            @Override public void onFailure(Call<Observacion> call, Throwable t) {
                progressDetalle.setVisibility(View.GONE);
                Log.e(TAG, "Fallo de red al cargar observación", t);
                Toast.makeText(DetalleObservacionActivity.this, "Sin conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (observacionActual != null) {
            Long currentUserId = session.getUserId();
            String role = session.getRol();
            boolean isOwner = observacionActual.getUsuario() != null && observacionActual.getUsuario().getId().equals(currentUserId);
            boolean isAdmin = "ADMIN".equals(role);

            if (isOwner || isAdmin) {
                getMenuInflater().inflate(R.menu.menu_detalle, menu);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_editar) {
            irAEditar();
            return true;
        } else if (id == R.id.action_borrar) {
            confirmarEliminarObservacion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void irAEditar() {
        Intent intent = new Intent(this, CrearObservacionActivity.class);
        intent.putExtra("idObservacion", idObservacion);
        startActivity(intent);
    }

    private void confirmarEliminarObservacion() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Observación")
                .setMessage("¿Estás seguro de que deseas eliminar esta observación? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> borrarObservacion())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void borrarObservacion() {
        Log.d(TAG, "Borrando observación ID: " + idObservacion);
        progressDetalle.setVisibility(View.VISIBLE);
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.borrarObservacion(idObservacion).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressDetalle.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(DetalleObservacionActivity.this, "Observación eliminada", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Error al borrar observación: " + response.code());
                    ErrorUtils.mostrarToastError(DetalleObservacionActivity.this, response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDetalle.setVisibility(View.GONE);
                Log.e(TAG, "Fallo de red al borrar observación", t);
                Toast.makeText(DetalleObservacionActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarObservacion() {
        invalidateOptionsMenu();
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

        // Carrusel de Imágenes
        if (o.getImagenes() != null && !o.getImagenes().isEmpty()) {
            ImageCarouselAdapter carouselAdapter = new ImageCarouselAdapter(o.getImagenes());
            viewPagerImagenes.setAdapter(carouselAdapter);
            viewPagerImagenes.setVisibility(View.VISIBLE);
            
            if (o.getImagenes().size() > 1) {
                tabDots.setVisibility(View.VISIBLE);
                new TabLayoutMediator(tabDots, viewPagerImagenes, (tab, position) -> {}).attach();
            } else {
                tabDots.setVisibility(View.GONE);
            }

            // Mostrar botón borrar imagen si es dueño o admin (borra la imagen actual)
            if (btnBorrarImagen != null) {
                Long currentUserId = session.getUserId();
                String role = session.getRol();
                boolean isOwner = o.getUsuario() != null && o.getUsuario().getId().equals(currentUserId);
                boolean isAdmin = "ADMIN".equals(role);
                btnBorrarImagen.setVisibility((isOwner || isAdmin) ? View.VISIBLE : View.GONE);
            }
        } else {
            viewPagerImagenes.setVisibility(View.GONE);
            tabDots.setVisibility(View.GONE);
            ivDetalleImagen.setVisibility(View.VISIBLE);

            if (imagenLocal != null && !imagenLocal.isEmpty()) {
                Log.d(TAG, "Mostrando imagen local de previsualización: " + imagenLocal);
                Glide.with(this)
                        .load(Uri.parse(imagenLocal))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivDetalleImagen);
            } else {
                ivDetalleImagen.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            if (btnBorrarImagen != null) btnBorrarImagen.setVisibility(View.GONE);
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

    private void confirmarEliminarImagen() {
        if (observacionActual == null || observacionActual.getImagenes() == null || observacionActual.getImagenes().isEmpty()) return;
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Imagen")
                .setMessage("¿Estás seguro de que deseas eliminar la imagen de esta observación?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarImagen())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarImagen() {
        if (observacionActual == null || observacionActual.getImagenes() == null || observacionActual.getImagenes().isEmpty()) return;
        
        int currentPos = viewPagerImagenes.getCurrentItem();
        Long idImg = observacionActual.getImagenes().get(currentPos).getId();
        Log.d(TAG, "Eliminando imagen ID: " + idImg + " en posicion: " + currentPos);
        progressDetalle.setVisibility(View.VISIBLE);
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.eliminarImagen(idObservacion, idImg).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressDetalle.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(DetalleObservacionActivity.this, "Imagen eliminada", Toast.LENGTH_SHORT).show();
                    cargarObservacion(); // Recargar para actualizar vista
                } else {
                    Log.e(TAG, "Error al eliminar imagen: " + response.code());
                    ErrorUtils.mostrarToastError(DetalleObservacionActivity.this, response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDetalle.setVisibility(View.GONE);
                Log.e(TAG, "Fallo de red al eliminar imagen", t);
                Toast.makeText(DetalleObservacionActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleMeGusta() {
        if (observacionActual == null) return;
        Long idUser = session.getUserId();
        if (idUser == -1L) {
            Toast.makeText(this, "Inicia sesión para participar", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Cambiando estado de Me Gusta...");
        btnMeGusta.setEnabled(false);
        ApiService api = ApiClient.getClient().create(ApiService.class);
        boolean yaDabaMeGusta = observacionActual.isMeGustaPropio();
        
        Call<Void> call = yaDabaMeGusta ? api.quitarMeGusta(idObservacion, idUser) : api.darMeGusta(idObservacion, idUser);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnMeGusta.setEnabled(true);
                if (response.isSuccessful() || response.code() == 409) {
                    Log.d(TAG, "Me Gusta actualizado OK");
                    boolean nuevoEstado = !yaDabaMeGusta;
                    observacionActual.setMeGustaPropio(nuevoEstado);
                    int numActual = observacionActual.getNumMeGustas();
                    observacionActual.setNumMeGustas(nuevoEstado ? numActual + 1 : Math.max(0, numActual - 1));
                    actualizarBotonMeGusta();
                    actualizarEstadoMeGustaServidor();
                } else {
                    Log.e(TAG, "Error al cambiar Me Gusta: " + response.code());
                    ErrorUtils.mostrarToastError(DetalleObservacionActivity.this, response.code());
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                btnMeGusta.setEnabled(true);
                Log.e(TAG, "Fallo de red en Me Gusta", t);
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

        Log.d(TAG, "Enviando comentario...");
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
                    Log.d(TAG, "Comentario enviado con éxito");
                    editNuevoComentario.setText("");
                    cargarComentarios();
                } else {
                    Log.e(TAG, "Error al enviar comentario: " + response.code());
                    ErrorUtils.mostrarToastError(DetalleObservacionActivity.this, response.code());
                }
            }
            @Override public void onFailure(Call<Comentario> call, Throwable t) {
                Log.e(TAG, "Fallo de red al comentar", t);
                Toast.makeText(DetalleObservacionActivity.this, "Error al comentar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
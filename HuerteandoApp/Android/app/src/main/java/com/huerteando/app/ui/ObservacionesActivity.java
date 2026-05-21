package com.huerteando.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.huerteando.app.MyApp;
import com.huerteando.app.R;
import com.huerteando.app.adapter.ObservacionAdapter;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla principal: lista de observaciones.
 * Actualizada para cargar todas las observaciones por defecto segun Manual.
 */
public class ObservacionesActivity extends AppCompatActivity {

    private static final String TAG = "ObservacionesActivity";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvSinResultados;
    private Spinner spinnerTipo, spinnerOrden, spinnerUsuario;
    private FloatingActionButton fabNueva;

    private ObservacionAdapter adapter;
    private final List<Observacion> listaOriginal = new ArrayList<>();
    private final List<Observacion> listaAMostrar = new ArrayList<>();

    private Long idTipoSeleccionado = null;
    private Long idUsuarioSeleccionado = null;
    private String ordenSeleccionado = "fecha";
    private String textoBusqueda = "";
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observaciones);

        session = new SessionManager(this);
        setupToolbar();
        initViews();
        setupSpinners();

        fabNueva.setOnClickListener(v -> {
            if (!MyApp.getSession().haySesion()) {
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                startActivity(new Intent(this, CrearObservacionActivity.class));
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Huerteando");
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerObservaciones);
        progressBar = findViewById(R.id.progressBar);
        tvSinResultados = findViewById(R.id.tvSinResultados);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerOrden = findViewById(R.id.spinnerOrden);
        spinnerUsuario = findViewById(R.id.spinnerUsuario);
        fabNueva = findViewById(R.id.fabCrearObservacion);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ObservacionAdapter(listaAMostrar, obs -> {
            Intent intent = new Intent(this, DetalleObservacionActivity.class);
            intent.putExtra("idObservacion", obs.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(this,
                R.array.array_tipos, R.layout.spinner_item);
        adapterTipo.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                switch (pos) {
                    case 1: idTipoSeleccionado = 4L; break; // PLANTA
                    case 2: idTipoSeleccionado = 5L; break; // RINCON
                    case 3: idTipoSeleccionado = 6L; break; // INCIDENCIA
                    default: idTipoSeleccionado = null;
                }
                cargarObservaciones();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        ArrayAdapter<CharSequence> adapterUsuario = ArrayAdapter.createFromResource(this,
                R.array.array_filtro_usuario, R.layout.spinner_item);
        adapterUsuario.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerUsuario.setAdapter(adapterUsuario);
        spinnerUsuario.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    idUsuarioSeleccionado = session.getUserId();
                } else {
                    idUsuarioSeleccionado = null;
                }
                cargarObservaciones();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        final String[] valoresOrden = {"fecha", "me gusta", "comentarios"};
        ArrayAdapter<CharSequence> adapterOrden = ArrayAdapter.createFromResource(this,
                R.array.array_orden, R.layout.spinner_item);
        adapterOrden.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerOrden.setAdapter(adapterOrden);
        spinnerOrden.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                ordenSeleccionado = valoresOrden[pos];
                procesarYMostrarLista();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void cargarObservaciones() {
        Log.d(TAG, "Cargando observaciones desde la API...");
        progressBar.setVisibility(View.VISIBLE);
        ApiService api = ApiClient.getClient().create(ApiService.class);

        Log.d(TAG, "Cargando observaciones filtradas: Tipo=" + idTipoSeleccionado + ", Usuario=" + idUsuarioSeleccionado);
        Call<List<Observacion>> call = api.getObservaciones(idTipoSeleccionado, idUsuarioSeleccionado);

        call.enqueue(new Callback<List<Observacion>>() {
            @Override
            public void onResponse(Call<List<Observacion>> call, Response<List<Observacion>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Observaciones recibidas: " + response.body().size());
                    listaOriginal.clear();
                    listaOriginal.addAll(response.body());
                    procesarYMostrarLista();
                    // Cargamos los likes y comentarios reales de cada observacion
                    // para que no se queden todos a 0.
                    cargarContadores();
                } else {
                    Log.e(TAG, "Error del servidor al cargar observaciones: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<Observacion>> call, Throwable t) {
                Log.e(TAG, "Error crítico al conectar con la API", t);
                progressBar.setVisibility(View.GONE);
                tvSinResultados.setVisibility(View.VISIBLE);
            }
        });
    }

    private void procesarYMostrarLista() {
        List<Observacion> temp = new ArrayList<>(listaOriginal);

        // 1. Ordenar
        Collections.sort(temp, (o1, o2) -> {
            switch (ordenSeleccionado) {
                case "me gusta": return Integer.compare(o2.getNumMeGustas(), o1.getNumMeGustas());
                case "comentarios": return Integer.compare(o2.getNumComentarios(), o1.getNumComentarios());
                default:
                    String f1 = o1.getFechaObservacion() != null ? o1.getFechaObservacion() : "";
                    String f2 = o2.getFechaObservacion() != null ? o2.getFechaObservacion() : "";
                    return f2.compareTo(f1);
            }
        });

        // 2. Filtrar (por texto y refuerzo local de los otros filtros)
        listaAMostrar.clear();
        String query = textoBusqueda.toLowerCase().trim();
        for (Observacion o : temp) {
            boolean matchesSearch = query.isEmpty() || o.getTitulo().toLowerCase().contains(query);
            
            // Refuerzo local de filtros para asegurar coherencia si la API tarda o no soporta combinación
            boolean matchesTipo = (idTipoSeleccionado == null) || 
                    (o.getTipoObservacion() != null && o.getTipoObservacion().getId().longValue() == idTipoSeleccionado);
            
            boolean matchesUsuario = (idUsuarioSeleccionado == null) || 
                    (o.getUsuario() != null && o.getUsuario().getId().equals(idUsuarioSeleccionado));

            if (matchesSearch && matchesTipo && matchesUsuario) {
                listaAMostrar.add(o);
            }
        }

        adapter.notifyDataSetChanged();
        tvSinResultados.setVisibility(listaAMostrar.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void cargarContadores() {
        ApiService api = ApiClient.getClient().create(ApiService.class);

        for (Observacion observacion : listaOriginal) {
            if (observacion.getId() == null) {
                continue;
            }

            // Cargar Me Gustas
            api.getMeGustasCount(observacion.getId()).enqueue(new Callback<Map<String, Long>>() {
                @Override
                public void onResponse(Call<Map<String, Long>> call, Response<Map<String, Long>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Long total = response.body().get("megustas");
                        observacion.setNumMeGustas(total != null ? total.intValue() : 0);
                        procesarYMostrarLista();
                    }
                }
                @Override public void onFailure(Call<Map<String, Long>> call, Throwable t) {}
            });

            // Cargar Comentarios
            api.getComentariosCount(observacion.getId()).enqueue(new Callback<Map<String, Long>>() {
                @Override
                public void onResponse(Call<Map<String, Long>> call, Response<Map<String, Long>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Long total = response.body().get("comentarios");
                        observacion.setNumComentarios(total != null ? total.intValue() : 0);
                        procesarYMostrarLista();
                    }
                }
                @Override public void onFailure(Call<Map<String, Long>> call, Throwable t) {}
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_observaciones, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    textoBusqueda = query;
                    procesarYMostrarLista();
                    return true;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    textoBusqueda = newText;
                    procesarYMostrarLista();
                    return true;
                }
            });
        }

        // Ocultar opciones si el usuario no está logueado
        MenuItem actionPerfil = menu.findItem(R.id.action_perfil);
        MenuItem actionLogout = menu.findItem(R.id.action_logout);
        boolean estaLogueado = session.haySesion();

        if (actionPerfil != null) actionPerfil.setVisible(estaLogueado);
        if (actionLogout != null) actionLogout.setVisible(estaLogueado);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_perfil) {
            startActivity(new Intent(this, PerfilActivity.class));
            return true;
        } else if (id == R.id.action_especies) {
            startActivity(new Intent(this, EspeciesActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            session.cerrarSesion();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarObservaciones();
    }
}

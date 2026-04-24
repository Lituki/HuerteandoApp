package com.huerteando.app.ui;

import android.content.Intent;
import android.os.Bundle;
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
import com.huerteando.app.R;
import com.huerteando.app.adapter.ObservacionAdapter;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla principal: lista de observaciones con filtros y ordenación.
 */
public class ObservacionesActivity extends AppCompatActivity {

    // ─── Vistas ───────────────────────────────────────────────────────────────
    private RecyclerView         recyclerView;
    private ProgressBar          progressBar;
    private TextView             tvSinResultados;
    private Spinner              spinnerTipo;
    private Spinner              spinnerOrden;
    private FloatingActionButton fabNueva;

    // ─── Datos ────────────────────────────────────────────────────────────────
    private ObservacionAdapter adapter;
    private final List<Observacion>  listaOriginal = new ArrayList<>();
    private final List<Observacion>  listaAMostrar = new ArrayList<>();

    private String idTipoSeleccionado = null; // ID numérico para la API
    private String ordenSeleccionado  = "fecha";
    private String textoBusqueda      = "";

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observaciones);

        session = new SessionManager(this);

        // CONFIGURAR TOOLBAR (Añadido para que se vea la barra verde y el menú)
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Huerteando");
        }

        recyclerView    = findViewById(R.id.recyclerObservaciones);
        progressBar     = findViewById(R.id.progressBar);
        tvSinResultados = findViewById(R.id.tvSinResultados);
        spinnerTipo     = findViewById(R.id.spinnerTipo);
        spinnerOrden    = findViewById(R.id.spinnerOrden);
        fabNueva        = findViewById(R.id.fabCrearObservacion);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Usamos listaAMostrar para el adapter para que refleje filtros y orden
        adapter = new ObservacionAdapter(listaAMostrar, observacion -> {
            Intent intent = new Intent(this, DetalleObservacionActivity.class);
            intent.putExtra("idObservacion", observacion.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Spinner TIPO
        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(this,
                R.array.array_tipos, android.R.layout.simple_spinner_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                switch (pos) {
                    case 1: idTipoSeleccionado = "1"; break; // PLANTA
                    case 2: idTipoSeleccionado = "2"; break; // RINCON
                    case 3: idTipoSeleccionado = "3"; break; // INCIDENCIA
                    default: idTipoSeleccionado = null;
                }
                cargarObservaciones();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // Spinner ORDEN
        final String[] valoresOrden = {"fecha", "likes", "comentarios"};
        ArrayAdapter<CharSequence> adapterOrden = ArrayAdapter.createFromResource(this,
                R.array.array_orden, android.R.layout.simple_spinner_item);
        adapterOrden.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrden.setAdapter(adapterOrden);
        spinnerOrden.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                ordenSeleccionado = valoresOrden[pos];
                // Si ya tenemos datos, procesamos localmente para mayor fluidez
                if (!listaOriginal.isEmpty()) {
                    procesarYMostrarLista();
                } else {
                    cargarObservaciones();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        fabNueva.setOnClickListener(v ->
                startActivity(new Intent(this, CrearObservacionActivity.class)));

        cargarObservaciones();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_observaciones, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Buscar por nombre...");
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
        return true;
    }

    /**
     * Aplica ORDENACIÓN (descendente) y FILTRO de búsqueda sobre la lista original.
     */
    private void procesarYMostrarLista() {
        List<Observacion> temp = new ArrayList<>(listaOriginal);

        // 1. Ordenar de MAYOR a MENOR (Descendente)
        Collections.sort(temp, (o1, o2) -> {
            if ("likes".equals(ordenSeleccionado)) {
                return Integer.compare(o2.getNumLikes(), o1.getNumLikes());
            } else if ("comentarios".equals(ordenSeleccionado)) {
                return Integer.compare(o2.getNumComentarios(), o1.getNumComentarios());
            } else {
                // Por defecto fecha: más reciente arriba
                String f1 = o1.getFechaObservacion() != null ? o1.getFechaObservacion() : "";
                String f2 = o2.getFechaObservacion() != null ? o2.getFechaObservacion() : "";
                return f2.compareTo(f1);
            }
        });

        // 2. Filtrar por texto
        listaAMostrar.clear();
        String query = (textoBusqueda == null) ? "" : textoBusqueda.toLowerCase().trim();
        if (query.isEmpty()) {
            listaAMostrar.addAll(temp);
        } else {
            for (Observacion o : temp) {
                // Filtramos por título o descripción
                if (o.getTitulo().toLowerCase().contains(query) || 
                   (o.getDescripcion() != null && o.getDescripcion().toLowerCase().contains(query))) {
                    listaAMostrar.add(o);
                }
            }
        }

        // 3. Notificar cambios a la vista
        adapter.notifyDataSetChanged();
        tvSinResultados.setVisibility(listaAMostrar.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_perfil) {
            startActivity(new Intent(this, PerfilActivity.class));
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

    private void cargarObservaciones() {
        progressBar.setVisibility(View.VISIBLE);
        tvSinResultados.setVisibility(View.GONE);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        // Enviamos el ID numérico y el parámetro de orden al servidor
        api.getObservaciones(idTipoSeleccionado, null, ordenSeleccionado, null)
                .enqueue(new Callback<List<Observacion>>() {
                    @Override
                    public void onResponse(Call<List<Observacion>> call, Response<List<Observacion>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            listaOriginal.clear();
                            listaOriginal.addAll(response.body());
                            // Procesamos la lista final (filtro + ordenación garantizada)
                            procesarYMostrarLista();
                        } else {
                            mostrarError("Error del servidor: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Observacion>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        mostrarError("Error de conexión");
                    }
                });
    }

    private void mostrarError(String msg) {
        tvSinResultados.setText(msg);
        tvSinResultados.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarObservaciones();
    }
}

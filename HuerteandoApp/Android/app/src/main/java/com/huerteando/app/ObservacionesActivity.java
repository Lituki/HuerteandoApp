package com.huerteando.app;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla principal: lista de observaciones con filtros y ordenación.
 *
 * RF cubiertos: RF-09 (listar), RF-11 (filtrar), RF-12 (búsqueda texto),
 *               RF-13 (ordenar), RF-14 (combinar filtros).
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
    private final List<Observacion>  lista = new ArrayList<>();

    // ─── Filtros activos ──────────────────────────────────────────────────────
    private String tipoSeleccionado  = null;   // null = todos
    private String ordenSeleccionado = "fecha";
    private String textoBusqueda     = null;

    private SessionManager session;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observaciones);

        session = new SessionManager(this);

        // 1. Enlazar vistas
        recyclerView    = findViewById(R.id.recyclerObservaciones);
        progressBar     = findViewById(R.id.progressBar);
        tvSinResultados = findViewById(R.id.tvSinResultados);
        spinnerTipo     = findViewById(R.id.spinnerTipo);
        spinnerOrden    = findViewById(R.id.spinnerOrden);
        fabNueva        = findViewById(R.id.fabCrearObservacion);

        // 2. RecyclerView — constructor correcto (2 parámetros: lista + listener)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ObservacionAdapter(lista, observacion -> {
            Intent intent = new Intent(this, DetalleObservacionActivity.class);
            intent.putExtra("idObservacion", observacion.getId()); // clave unificada
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // 3. Spinner TIPO
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todos los tipos", "PLANTA", "RINCÓN", "DENUNCIA"});
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                tipoSeleccionado = (pos == 0) ? null : adapterTipo.getItem(pos);
                cargarObservaciones();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // 4. Spinner ORDEN
        final String[] valoresOrden = {"fecha", "likes", "comentarios"};
        ArrayAdapter<String> adapterOrden = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Más recientes", "Más likes", "Más comentarios"});
        adapterOrden.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrden.setAdapter(adapterOrden);
        spinnerOrden.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                ordenSeleccionado = valoresOrden[pos];
                cargarObservaciones();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // 5. FAB → crear nueva observación
        fabNueva.setOnClickListener(v ->
                startActivity(new Intent(this, CrearObservacionActivity.class)));

        // 6. Carga inicial
        cargarObservaciones();
    }

    // ─── Menú (búsqueda + cerrar sesión) ─────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_observaciones, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Buscar observaciones…");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                textoBusqueda = query.trim().isEmpty() ? null : query.trim();
                cargarObservaciones();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                textoBusqueda = newText.trim().isEmpty() ? null : newText.trim();
                cargarObservaciones();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            session.cerrarSesion();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── Carga de datos ───────────────────────────────────────────────────────

    private void cargarObservaciones() {
        progressBar.setVisibility(View.VISIBLE);
        tvSinResultados.setVisibility(View.GONE);

        ApiService api = ApiClient.getClient(session.getToken()).create(ApiService.class);

        // getObservaciones ahora tiene 4 parámetros: tipo, estado, orden, busqueda
        api.getObservaciones(tipoSeleccionado, null, ordenSeleccionado, textoBusqueda)
                .enqueue(new Callback<List<Observacion>>() {
                    @Override
                    public void onResponse(Call<List<Observacion>> call,
                                           Response<List<Observacion>> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            lista.clear();
                            lista.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            tvSinResultados.setVisibility(
                                    lista.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            mostrarError("Error al cargar observaciones");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Observacion>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        mostrarError("Error de conexión. Comprueba la red.");
                    }
                });
    }

    private void mostrarError(String msg) {
        tvSinResultados.setText(msg);
        tvSinResultados.setVisibility(View.VISIBLE);
    }

    // Recargar al volver de crear una observación
    @Override
    protected void onResume() {
        super.onResume();
        cargarObservaciones();
    }
}
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
    private final List<Observacion>  lista = new ArrayList<>();

    private String tipoSeleccionado  = null;
    private String ordenSeleccionado = "fecha";
    private String textoBusqueda     = null;

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
        adapter = new ObservacionAdapter(lista, observacion -> {
            Intent intent = new Intent(this, DetalleObservacionActivity.class);
            intent.putExtra("idObservacion", observacion.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(this,
                R.array.array_tipos, android.R.layout.simple_spinner_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                tipoSeleccionado = (pos == 0) ? null : adapterTipo.getItem(pos).toString();
                cargarObservaciones();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        final String[] valoresOrden = {"fecha", "likes", "comentarios"};
        ArrayAdapter<CharSequence> adapterOrden = ArrayAdapter.createFromResource(this,
                R.array.array_orden, android.R.layout.simple_spinner_item);
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
        }
        return true;
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
                            tvSinResultados.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
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

    @Override
    protected void onResume() {
        super.onResume();
        cargarObservaciones();
    }
}

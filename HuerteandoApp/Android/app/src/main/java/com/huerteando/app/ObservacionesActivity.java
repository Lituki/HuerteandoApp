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
 * Funcionalidades implementadas:
 *   - RF-09  Listar observaciones
 *   - RF-11  Filtrar por tipo (PLANTA, RINCON, DENUNCIA)
 *   - RF-12  Búsqueda por texto libre
 *   - RF-13  Ordenar por fecha, likes, comentarios
 *   - RF-14  Combinar filtros y ordenación
 *
 * Navegación:
 *   - FAB (+) → NuevaObservacionActivity
 *   - Click en tarjeta → DetalleObservacionActivity
 *   - Menú (⋮) → Cerrar sesión
 */
public class ObservacionesActivity extends AppCompatActivity {

    // ---- Vistas ----
    private RecyclerView          recyclerView;
    private ProgressBar           progressBar;
    private TextView              tvSinResultados;
    private Spinner               spinnerTipo;
    private Spinner               spinnerOrden;
    private FloatingActionButton  fabNueva;

    // ---- Adaptador y datos ----
    private ObservacionAdapter    adapter;
    private List<Observacion>     listaObservaciones = new ArrayList<>();

    // ---- Estado de filtros ----
    private String tipoSeleccionado  = null;  // null = todos los tipos
    private String ordenSeleccionado = "fecha"; // por defecto: más reciente
    private String textoBusqueda     = null;

    // ---- Sesión ----
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observaciones);

        session = new SessionManager(this);

        // 1. Enlazar vistas
        recyclerView     = findViewById(R.id.recyclerObservaciones);
        progressBar      = findViewById(R.id.progressBar);
        tvSinResultados  = findViewById(R.id.tvSinResultados);
        spinnerTipo      = findViewById(R.id.spinnerTipo);
        spinnerOrden     = findViewById(R.id.spinnerOrden);
        fabNueva         = findViewById(R.id.fabNuevaObservacion);

        // 2. Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ObservacionAdapter(listaObservaciones, session.getUserId(), observacion -> {
            // Al pulsar una tarjeta → abrir el detalle
            Intent intent = new Intent(this, DetalleObservacionActivity.class);
            intent.putExtra("id_observacion", observacion.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // 3. Configurar spinner de TIPO
        //    El primer ítem es "Todos" (valor null en la query)
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todos los tipos", "PLANTA", "RINCÓN", "DENUNCIA"});
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Posición 0 = "Todos" → null, resto = nombre del tipo
                tipoSeleccionado = position == 0 ? null : adapterTipo.getItem(position);
                cargarObservaciones();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 4. Configurar spinner de ORDEN
        ArrayAdapter<String> adapterOrden = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Más recientes", "Más likes", "Más comentarios"});
        adapterOrden.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrden.setAdapter(adapterOrden);
        spinnerOrden.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private final String[] valores = {"fecha", "likes", "comentarios"};
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ordenSeleccionado = valores[position];
                cargarObservaciones();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 5. FAB para nueva observación
        fabNueva.setOnClickListener(v ->
                startActivity(new Intent(this, NuevaObservacionActivity.class)));

        // 6. Carga inicial
        cargarObservaciones();
    }

    /**
     * Añade la lupa de búsqueda en el ActionBar.
     * Al escribir texto se actualiza textoBusqueda y se recarga.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_observaciones, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Buscar observaciones...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                textoBusqueda = query.trim().isEmpty() ? null : query.trim();
                cargarObservaciones();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // Buscar en tiempo real (se puede quitar si la API es lenta)
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
            // Cerrar sesión → limpiar SharedPreferences y volver al login
            session.cerrarSesion();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Llama a la API con los filtros actuales y actualiza el RecyclerView.
     * Se ejecuta cada vez que cambia un spinner o el texto de búsqueda.
     */
    private void cargarObservaciones() {
        progressBar.setVisibility(View.VISIBLE);
        tvSinResultados.setVisibility(View.GONE);

        ApiService api = ApiClient.getClient(session.getToken()).create(ApiService.class);

        api.getObservaciones(tipoSeleccionado, null, ordenSeleccionado, textoBusqueda)
                .enqueue(new Callback<List<Observacion>>() {

                    @Override
                    public void onResponse(Call<List<Observacion>> call,
                                           Response<List<Observacion>> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            listaObservaciones.clear();
                            listaObservaciones.addAll(response.body());
                            adapter.notifyDataSetChanged();

                            // Si la lista está vacía, mostrar mensaje
                            tvSinResultados.setVisibility(
                                    listaObservaciones.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Observacion>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        tvSinResultados.setText("Error de conexión. Comprueba la red.");
                        tvSinResultados.setVisibility(View.VISIBLE);
                    }
                });
    }

    /**
     * Al volver de NuevaObservacionActivity recargamos la lista
     * para que aparezca la observación recién creada.
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarObservaciones();
    }
}

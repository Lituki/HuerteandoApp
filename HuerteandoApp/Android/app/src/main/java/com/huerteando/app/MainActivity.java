package com.huerteando.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
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
 * Activity Principal - Muestra la lista de observaciones
 * 
 * ¿Qué hace esta clase?
 * 1. Muestra un listado de todas las observaciones de la Huerta de Murcia
 * 2. Permite filtrar por tipo (Planta, Rincón, Denuncia)
 * 3. Permite cerrar sesión
 * 4. Permite crear nuevas observaciones
 */
public class MainActivity extends AppCompatActivity implements ObservacionAdapter.OnObservacionClickListener {

    // Elementos del layout
    private RecyclerView recyclerView;           // Lista de observaciones
    private ProgressBar progressBar;             // Indicador de carga
    private TextView tvVacio;                     // Mensaje cuando no hay datos
    private TabLayout tabLayout;                  // Filtros (Todas, Plantas, Rincón, Denuncias)
    private FloatingActionButton fabCrear;       // Botón flotante para crear
    private boolean primeraCarga = true;        // Controla si es el primer inicio
    // Adaptador y datos
    private ObservacionAdapter adapter;
    private List<Observacion> listaObservaciones = new ArrayList<>();

    // Gestor de sesión
    private SessionManager sessionManager;

    // Filtro actual
    private String filtroTipo = null;  // null = todas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar sesión
        sessionManager = new SessionManager(this);

        // Verificar que el usuario esté logueado
        if (!sessionManager.haySesion()) {
            irALogin();
            return;
        }

        // Configurar toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Huerteando");

        // Conectar elementos
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvVacio = findViewById(R.id.tvVacio);
        tabLayout = findViewById(R.id.tabLayout);
        fabCrear = findViewById(R.id.fabCrear);

        // Configurar RecyclerView
        adapter = new ObservacionAdapter(listaObservaciones, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Configurar tabs (filtros)
        tabLayout.addTab(tabLayout.newTab().setText("Todas"));
        tabLayout.addTab(tabLayout.newTab().setText("Plantas"));
        tabLayout.addTab(tabLayout.newTab().setText("Rincón"));
        tabLayout.addTab(tabLayout.newTab().setText("Denuncia"));

        // Listener de tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Cambiar filtro según la pestaña
                switch (tab.getPosition()) {
                    case 0: filtroTipo = null; break;      // Todas
                    case 1: filtroTipo = "PLANTA"; break;  // Plantas
                    case 2: filtroTipo = "RINCON"; break; // Rincón
                    case 3: filtroTipo = "DENUNCIA"; break;// Denuncia
                }
                cargarObservaciones();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Botón flotante para crear observación
        fabCrear.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearObservacionActivity.class);
            startActivity(intent);
        });

        // Cargar datos
        cargarObservaciones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Si es la primera vez (ya lo cargamos en onCreate), nos lo saltamos.
        // Las veces siguientes (volver de DetalleObservacion, etc.) sí recargamos.
        if (primeraCarga) {
            primeraCarga = false;
            return;
        }

        cargarObservaciones();
    }

    /**
     * Carga las observaciones desde el servidor
     */
    private void cargarObservaciones() {
        // Mostrar loading
        progressBar.setVisibility(View.VISIBLE);
        tvVacio.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        // Obtener token
        String token = sessionManager.getToken();

        // Crear API service
        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

        // Llamar al endpoint
        Call<List<Observacion>> call = apiService.getObservaciones(filtroTipo, null, null);

        call.enqueue(new Callback<List<Observacion>>() {
            @Override
            public void onResponse(Call<List<Observacion>> call, Response<List<Observacion>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    listaObservaciones.clear();
                    if (response.body() != null) {
                        listaObservaciones.addAll(response.body());
                    }

                    adapter.notifyDataSetChanged();

                    // Mostrar mensaje si no hay datos
                    if (listaObservaciones.isEmpty()) {
                        tvVacio.setText("No hay observaciones aún.\n¡Sé el primero en compartir!");
                        tvVacio.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error al cargar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Observacion>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Métodos del menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_cerrar_sesion) {
            sessionManager.cerrarSesion();
            irALogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Click en una observación
    @Override
    public void onObservacionClick(Observacion observacion) {
        // Ir a la pantalla de detalle
        Intent intent = new Intent(MainActivity.this, DetalleObservacionActivity.class);
        intent.putExtra("idObservacion", observacion.getId());
        startActivity(intent);
    }

    // Ir a login
    private void irALogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}

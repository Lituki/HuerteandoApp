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
 * MainActivity — listado con tabs de filtro rápido.
 * Alternativa a ObservacionesActivity (con Spinners y búsqueda).
 *
 * NOTA: el Launcher arranca en LoginActivity → ObservacionesActivity.
 * Esta pantalla queda disponible como alternativa.
 */
public class MainActivity extends AppCompatActivity
        implements ObservacionAdapter.OnObservacionClickListener {

    private RecyclerView         recyclerView;
    private ProgressBar          progressBar;
    private TextView             tvVacio;
    private TabLayout            tabLayout;
    private FloatingActionButton fabCrear;

    private ObservacionAdapter   adapter;
    private List<Observacion>    listaObservaciones = new ArrayList<>();

    private SessionManager sessionManager;
    private String         filtroTipo = null; // null = todas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        if (!sessionManager.haySesion()) {
            irALogin();
            return;
        }

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Huerteando");

        recyclerView = findViewById(R.id.recyclerView);
        progressBar  = findViewById(R.id.progressBar);
        tvVacio      = findViewById(R.id.tvVacio);
        tabLayout    = findViewById(R.id.tabLayout);
        fabCrear     = findViewById(R.id.fabCrear);

        adapter = new ObservacionAdapter(listaObservaciones, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Tabs de filtro
        tabLayout.addTab(tabLayout.newTab().setText("Todas"));
        tabLayout.addTab(tabLayout.newTab().setText("Plantas"));
        tabLayout.addTab(tabLayout.newTab().setText("Rincón"));
        tabLayout.addTab(tabLayout.newTab().setText("Denuncia"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: filtroTipo = null;       break;
                    case 1: filtroTipo = "PLANTA";   break;
                    case 2: filtroTipo = "RINCON";   break;
                    case 3: filtroTipo = "DENUNCIA"; break;
                }
                cargarObservaciones();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        fabCrear.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CrearObservacionActivity.class)));

        cargarObservaciones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarObservaciones();
    }

    private void cargarObservaciones() {
        progressBar.setVisibility(View.VISIBLE);
        tvVacio.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        String token = sessionManager.getToken();
        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

        // CORRECCIÓN: 4 parámetros (tipo, estado, orden, busqueda)
        Call<List<Observacion>> call = apiService.getObservaciones(filtroTipo, null, null, null);

        call.enqueue(new Callback<List<Observacion>>() {
            @Override
            public void onResponse(Call<List<Observacion>> call, Response<List<Observacion>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    listaObservaciones.clear();
                    if (response.body() != null) listaObservaciones.addAll(response.body());
                    adapter.notifyDataSetChanged();
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

    @Override
    public void onObservacionClick(Observacion observacion) {
        Intent intent = new Intent(MainActivity.this, DetalleObservacionActivity.class);
        intent.putExtra("idObservacion", observacion.getId());
        startActivity(intent);
    }

    private void irALogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
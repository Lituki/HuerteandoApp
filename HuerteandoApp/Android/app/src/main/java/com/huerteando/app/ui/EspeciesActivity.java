package com.huerteando.app.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.huerteando.app.R;
import com.huerteando.app.adapter.EspecieAdapter;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.Especie;
import com.huerteando.app.utils.ErrorUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EspeciesActivity extends AppCompatActivity {

    private static final String TAG = "EspeciesActivity";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvSinEspecies;
    private EspecieAdapter adapter;
    private final List<Especie> listaEspecies = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_especies);

        apiService = ApiClient.getClient().create(ApiService.class);

        setupToolbar();
        initViews();
        cargarEspecies();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarEspecies);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerEspecies);
        progressBar = findViewById(R.id.progressEspecies);
        tvSinEspecies = findViewById(R.id.tvSinEspecies);
        FloatingActionButton fab = findViewById(R.id.fabNuevaEspecie);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EspecieAdapter(listaEspecies, new EspecieAdapter.OnEspecieActionListener() {
            @Override
            public void onEdit(Especie especie) {
                mostrarDialogoEspecie(especie);
            }

            @Override
            public void onDelete(Especie especie) {
                confirmarEliminacion(especie);
            }
        });
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> mostrarDialogoEspecie(null));
    }

    private void cargarEspecies() {
        Log.d(TAG, "Cargando catálogo de especies...");
        progressBar.setVisibility(View.VISIBLE);
        apiService.getEspecies().enqueue(new Callback<List<Especie>>() {
            @Override
            public void onResponse(Call<List<Especie>> call, Response<List<Especie>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Especies cargadas: " + response.body().size());
                    listaEspecies.clear();
                    listaEspecies.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    tvSinEspecies.setVisibility(listaEspecies.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Log.e(TAG, "Error al cargar especies: " + response.code());
                    ErrorUtils.mostrarToastError(EspeciesActivity.this, response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Especie>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Fallo de red al cargar especies", t);
                Toast.makeText(EspeciesActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoEspecie(Especie especie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_especie, null);
        
        EditText editComun = view.findViewById(R.id.editEspecieComun);
        EditText editCientifico = view.findViewById(R.id.editEspecieCientifico);
        EditText editFamilia = view.findViewById(R.id.editEspecieFamilia);

        if (especie != null) {
            editComun.setText(especie.getNombreComun());
            editCientifico.setText(especie.getNombreCientifico());
            editFamilia.setText(especie.getFamilia());
            builder.setTitle("Editar Especie");
        } else {
            builder.setTitle("Nueva Especie");
        }

        builder.setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String comun = editComun.getText().toString().trim();
                    String cientifico = editCientifico.getText().toString().trim();
                    String familia = editFamilia.getText().toString().trim();
                    
                    if (comun.isEmpty()) return;

                    Especie nueva = especie != null ? especie : new Especie();
                    nueva.setNombreComun(comun);
                    nueva.setNombreCientifico(cientifico);
                    nueva.setFamilia(familia);

                    if (especie == null) crearEspecie(nueva);
                    else actualizarEspecie(nueva);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void crearEspecie(Especie especie) {
        Log.d(TAG, "Creando nueva especie: " + especie.getNombreComun());
        apiService.crearEspecie(especie).enqueue(new Callback<Especie>() {
            @Override
            public void onResponse(Call<Especie> call, Response<Especie> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Especie creada con éxito");
                    cargarEspecies();
                } else {
                    Log.e(TAG, "Error al crear especie: " + response.code());
                    ErrorUtils.mostrarToastError(EspeciesActivity.this, response.code());
                }
            }
            @Override public void onFailure(Call<Especie> call, Throwable t) {
                Log.e(TAG, "Fallo de red al crear especie", t);
            }
        });
    }

    private void actualizarEspecie(Especie especie) {
        Log.d(TAG, "Actualizando especie ID: " + especie.getId());
        apiService.actualizarEspecie(especie.getId(), especie).enqueue(new Callback<Especie>() {
            @Override
            public void onResponse(Call<Especie> call, Response<Especie> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Especie actualizada OK");
                    cargarEspecies();
                } else {
                    Log.e(TAG, "Error al actualizar: " + response.code());
                    ErrorUtils.mostrarToastError(EspeciesActivity.this, response.code());
                }
            }
            @Override public void onFailure(Call<Especie> call, Throwable t) {
                Log.e(TAG, "Fallo de red al actualizar", t);
            }
        });
    }

    private void confirmarEliminacion(Especie especie) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Especie")
                .setMessage("¿Estás seguro de que deseas eliminar esta especie?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    Log.d(TAG, "Eliminando especie ID: " + especie.getId());
                    apiService.eliminarEspecie(especie.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Especie eliminada");
                                cargarEspecies();
                            } else {
                                Log.e(TAG, "Error al eliminar: " + response.code());
                                ErrorUtils.mostrarToastError(EspeciesActivity.this, response.code());
                            }
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {
                            Log.e(TAG, "Fallo de red al eliminar", t);
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}

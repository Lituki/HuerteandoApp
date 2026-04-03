package com.huerteando.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla para crear una nueva observación geolocalizad.
 *
 * Flujo:
 *   1. El usuario elige el tipo (PLANTA / RINCÓN / DENUNCIA).
 *   2. Escribe título y descripción (la ubicación se obtiene por GPS).
 *   3. Pulsa "Guardar" → POST /api/observaciones.
 *   4. Si OK → finish() y ObservacionesActivity recarga la lista en onResume().
 *
 * Requisitos cubiertos: RF-06, RF-07
 * Geolocalización: latitud/longitud obligatorias (RF-07)
 */
public class NuevaObservacionActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1001;

    private Spinner               spinnerTipoNueva;
    private TextInputEditText     editTitulo, editDescripcion, editNombreTradicional;
    private MaterialButton        btnObtenerUbicacion, btnGuardar;
    private TextView              tvUbicacionInfo, tvError;
    private ProgressBar           progressGuardar;

    private double latitud  = 0.0;
    private double longitud = 0.0;
    private boolean ubicacionObtenida = false;

    private FusedLocationProviderClient fusedLocationClient;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_observacion);

        session = new SessionManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Enlazar vistas
        spinnerTipoNueva         = findViewById(R.id.spinnerTipoNueva);
        editTitulo               = findViewById(R.id.editTituloNueva);
        editDescripcion          = findViewById(R.id.editDescripcionNueva);
        editNombreTradicional    = findViewById(R.id.editNombreTradicionalNueva);
        btnObtenerUbicacion      = findViewById(R.id.btnObtenerUbicacion);
        btnGuardar               = findViewById(R.id.btnGuardarObservacion);
        tvUbicacionInfo          = findViewById(R.id.tvUbicacionInfo);
        tvError                  = findViewById(R.id.tvErrorNueva);
        progressGuardar          = findViewById(R.id.progressGuardar);

        // Tipos disponibles (RF-18)
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"PLANTA", "RINCON", "DENUNCIA"});
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoNueva.setAdapter(tipoAdapter);

        // Obtener ubicación GPS
        btnObtenerUbicacion.setOnClickListener(v -> obtenerUbicacion());

        // Guardar observación
        btnGuardar.setOnClickListener(v -> guardarObservacion());
    }

    /**
     * Solicita el permiso de ubicación si no está concedido,
     * y obtiene la última ubicación conocida del GPS.
     */
    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Pedir el permiso al usuario (el resultado llega en onRequestPermissionsResult)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
            return;
        }

        btnObtenerUbicacion.setEnabled(false);
        tvUbicacionInfo.setText("Obteniendo ubicación...");

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            btnObtenerUbicacion.setEnabled(true);
            if (location != null) {
                latitud  = location.getLatitude();
                longitud = location.getLongitude();
                ubicacionObtenida = true;
                tvUbicacionInfo.setText(String.format("📍 %.5f, %.5f", latitud, longitud));
            } else {
                tvUbicacionInfo.setText("No se pudo obtener la ubicación. Muévete y vuelve a intentarlo.");
            }
        }).addOnFailureListener(e -> {
            btnObtenerUbicacion.setEnabled(true);
            tvUbicacionInfo.setText("Error al acceder al GPS.");
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion(); // reintentar ahora que el permiso está concedido
        } else {
            Toast.makeText(this, "Sin permiso de ubicación no se puede crear la observación.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Valida los campos y llama a la API para crear la observación.
     */
    private void guardarObservacion() {
        String titulo      = editTitulo.getText() != null ? editTitulo.getText().toString().trim() : "";
        String descripcion = editDescripcion.getText() != null ? editDescripcion.getText().toString().trim() : "";
        String nomTrad     = editNombreTradicional.getText() != null ? editNombreTradicional.getText().toString().trim() : "";
        String tipo        = spinnerTipoNueva.getSelectedItem().toString();

        // Validaciones
        if (titulo.isEmpty()) {
            mostrarError("El título es obligatorio.");
            return;
        }
        if (!ubicacionObtenida) {
            mostrarError("Debes obtener la ubicación antes de guardar.");
            return;
        }

        // Construir el objeto Observacion para enviarlo al servidor
        Observacion nueva = new Observacion();
        nueva.setTitulo(titulo);
        nueva.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        nueva.setTipoObservacion(tipo);
        nueva.setLatitud(latitud);
        nueva.setLongitud(longitud);
        nueva.setNombreTradicional(nomTrad.isEmpty() ? null : nomTrad);

        ocultarError();
        btnGuardar.setEnabled(false);
        progressGuardar.setVisibility(View.VISIBLE);

        ApiService api = ApiClient.getClient(session.getToken()).create(ApiService.class);
        api.crearObservacion(nueva).enqueue(new Callback<Observacion>() {
            @Override
            public void onResponse(Call<Observacion> call, Response<Observacion> response) {
                progressGuardar.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(NuevaObservacionActivity.this,
                            "Observación creada ✓", Toast.LENGTH_SHORT).show();
                    finish(); // volver a la lista (onResume recargará)
                } else {
                    mostrarError("Error al guardar (" + response.code() + ").");
                }
            }
            @Override
            public void onFailure(Call<Observacion> call, Throwable t) {
                progressGuardar.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);
                mostrarError("Sin conexión con el servidor.");
            }
        });
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private void ocultarError() {
        tvError.setVisibility(View.GONE);
    }
}

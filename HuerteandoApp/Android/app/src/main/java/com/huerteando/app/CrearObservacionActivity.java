package com.huerteando.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.ObservacionRequest;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.utils.SessionManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity para crear una nueva observación
 * 
 * ¿Qué hace esta clase?
 * 1. Permite al usuario crear una nueva observación (planta, rincón o denuncia)
 * 2. Obtiene la ubicación actual del usuario
 * 3. Permite seleccionar fecha y tipo
 * 4. Envía los datos al servidor
 */
public class CrearObservacionActivity extends AppCompatActivity {

    // Campos del formulario
    private TextInputEditText editTitulo;
    private TextInputEditText editDescripcion;
    private AutoCompleteTextView spinnerTipo;
    private TextInputEditText editEspecie;
    private TextInputEditText editZona;
    private TextInputEditText editDireccion;
    private TextInputEditText editFecha;
    private TextInputEditText editNombreTradicional;
    private List<android.net.Uri> imagenesSeleccionadas = new java.util.ArrayList<>();
    private android.widget.Button btnSeleccionarImagen;
    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> pickImageLauncher;
    private MaterialButton btnGuardar;
    private MaterialButton btnMiUbicacion;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitud = 0;
    private double longitud = 0;
    private String direccionActual = "";
    private boolean ubicacionObtenida = false;
    private SessionManager sessionManager;

    // Tipos de observación
    private final String[] tipos = {"PLANTA", "RINCON", "DENUNCIA"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_observacion);
        pickImageLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        android.content.Intent data = result.getData();

                        imagenesSeleccionadas.clear();

                        if (data.getClipData() != null) {

                            int count = data.getClipData().getItemCount();

                            for (int i = 0; i < count; i++) {
                                android.net.Uri uri =
                                        data.getClipData().getItemAt(i).getUri();
                                imagenesSeleccionadas.add(uri);
                            }

                        } else if (data.getData() != null) {
                            imagenesSeleccionadas.add(data.getData());
                        }

                        android.widget.Toast.makeText(
                                this,
                                imagenesSeleccionadas.size() + " imágenes seleccionadas",
                                android.widget.Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        sessionManager = new SessionManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Conectar vistas
        editTitulo = findViewById(R.id.editTitulo);
        editDescripcion = findViewById(R.id.editDescripcion);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        editEspecie = findViewById(R.id.editEspecie);
        editZona = findViewById(R.id.editZona);
        editDireccion = findViewById(R.id.editDireccion);
        editFecha = findViewById(R.id.editFecha);
        editNombreTradicional = findViewById(R.id.editNombreTradicional);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnMiUbicacion = findViewById(R.id.btnMiUbicacion);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        progressBar = findViewById(R.id.progressBar);

        // Configurar spinner de tipo
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, tipos);
        spinnerTipo.setAdapter(adapter);

        // Selector de fecha
        editFecha.setOnClickListener(v -> mostrarSelectorFecha());

        // Botón de mi ubicación
        btnMiUbicacion.setOnClickListener(v -> obtenerUbicacionActual());

        // Botón guardar
        btnGuardar.setOnClickListener(v -> guardarObservacion());
        btnGuardar.setEnabled(false); // Deshabilitar hasta que se obtenga la ubicación

        // Poner fecha actual por defecto
        String fechaActual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editFecha.setText(fechaActual);
    }

    private void mostrarSelectorFecha() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona la fecha")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            editFecha.setText(sdf.format(calendar.getTime()));
        });

        datePicker.show(getSupportFragmentManager(), "FECHA");
    }

    private void obtenerUbicacionActual() {
        // Verificar permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    progressBar.setVisibility(View.GONE);
                    if (location != null) {
                        latitud = location.getLatitude();
                        longitud = location.getLongitude();
                        ubicacionObtenida = true;
                        btnGuardar.setEnabled(true);
                        
                        // Obtener dirección legible
                        obtenerDireccionDesdeCoordenadas(latitud, longitud);
                    } else {
                        ubicacionObtenida = false;
                        btnGuardar.setEnabled(false);
                        Toast.makeText(this, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    ubicacionObtenida = false;
                    btnGuardar.setEnabled(false);
                    Toast.makeText(this, "Error al obtener ubicación", Toast.LENGTH_SHORT).show();
                });
    }

    private void obtenerDireccionDesdeCoordenadas(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                direccionActual = address.getAddressLine(0);
                editDireccion.setText(direccionActual);
                String zonaTexto = address.getSubLocality();
                if (zonaTexto == null) zonaTexto = address.getLocality();
                if (zonaTexto == null) zonaTexto = "Sin zona";
                editZona.setText(zonaTexto);;
            }
        } catch (IOException e) {
            Toast.makeText(this, "No se pudo obtener la dirección", Toast.LENGTH_SHORT).show();
        }
    }
    private void abrirGaleria() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true);

        pickImageLauncher.launch(
                android.content.Intent.createChooser(intent, "Selecciona imágenes")
        );
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual();
        } else {
            ubicacionObtenida = false;
            btnGuardar.setEnabled(false);
            Toast.makeText(this, "Sin GPS no se puede geolocalizar la observación.", Toast.LENGTH_LONG).show();
        }
    }

    private void guardarObservacion() {
        // Validar campos obligatorios
        String titulo = editTitulo.getText() != null ? editTitulo.getText().toString().trim() : "";
        String descripcion = editDescripcion.getText() != null ? editDescripcion.getText().toString().trim() : "";
        String tipo = spinnerTipo.getText() != null ? spinnerTipo.getText().toString().trim() : "";
        String zona = editZona.getText() != null ? editZona.getText().toString().trim() : "";
        String direccion = editDireccion.getText() != null ? editDireccion.getText().toString().trim() : "";
        String fecha = editFecha.getText() != null ? editFecha.getText().toString().trim() : "";

        if (titulo.isEmpty()) {
            editTitulo.setError("Campo obligatorio");
            return;
        }

        if (tipo.isEmpty()) {
            spinnerTipo.setError("Selecciona un tipo");
            return;
        }

        if (fecha.isEmpty()) {
            editFecha.setError("Selecciona una fecha");
            return;
        }
        if (descripcion.isEmpty()) {
            editDescripcion.setError("Campo obligatorio");
            return;
        }

        if (zona.isEmpty()) {
            editZona.setError("Campo obligatorio");
            return;
        }

        if (direccion.isEmpty()) {
            editDireccion.setError("Campo obligatorio");
            return;
        }
        // Validar que se haya seleccionado una ubicación
        if (!ubicacionObtenida) {
            Toast.makeText(this, "Debes seleccionar una ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener valores opcionales
        String especie = editEspecie.getText() != null ? editEspecie.getText().toString().trim() : "";
        String nombreTradicional = editNombreTradicional.getText() != null ? 
                editNombreTradicional.getText().toString().trim() : "";

        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        // Crear request
        ObservacionRequest request = new ObservacionRequest(
                titulo, descripcion, fecha, tipo, especie,
                latitud, longitud, direccion, zona, nombreTradicional
        );

        // Llamar al API
        String token = sessionManager.getToken();
        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

        Call<Observacion> call = apiService.crearObservacion(request);
        call.enqueue(new Callback<Observacion>() {
            @Override
            public void onResponse(Call<Observacion> call, Response<Observacion> response) {
                progressBar.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CrearObservacionActivity.this, "¡Observación creada!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(
                            CrearObservacionActivity.this,
                            "Error servidor: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Observacion> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);
                Toast.makeText(CrearObservacionActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

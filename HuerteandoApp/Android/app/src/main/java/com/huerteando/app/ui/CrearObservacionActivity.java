package com.huerteando.app.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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
import com.google.android.material.textfield.TextInputEditText;
import com.huerteando.app.R;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.Imagen;
import com.huerteando.app.clases.ObservacionRequest;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity para crear una nueva observación.
 * Sincronizada con el backend para envío de coordenadas como double y fechas ISO.
 */
public class CrearObservacionActivity extends AppCompatActivity {

    private TextInputEditText editTitulo, editDescripcion, editEspecie, editZona, editDireccion, editFecha, editNombreTradicional;
    private AutoCompleteTextView spinnerTipo;
    private final List<Uri> imagenesSeleccionadas = new ArrayList<>();
    private android.widget.Button btnSeleccionarImagen;
    private android.widget.TextView tvImagenesSeleccionadas;
    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> pickImageLauncher;
    private MaterialButton btnGuardar, btnMiUbicacion;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitud = 0, longitud = 0;
    private boolean ubicacionObtenida = false;
    private SessionManager sessionManager;

    // Tipos de observación
    private final String[] tipos = {"PLANTA", "RINCON", "DENUNCIA"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_observacion);
        
        sessionManager = new SessionManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupImagePicker();
        
        // Configurar spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tipos);
        spinnerTipo.setAdapter(adapter);

        // Fecha actual
        editFecha.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));
        editFecha.setEnabled(false);

        btnMiUbicacion.setOnClickListener(v -> obtenerUbicacionActual());
        btnGuardar.setOnClickListener(v -> guardarObservacion());
        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
    }

    private void initViews() {
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
        tvImagenesSeleccionadas = findViewById(R.id.tvImagenesSeleccionadas);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imagenesSeleccionadas.clear();
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) imagenesSeleccionadas.add(result.getData().getClipData().getItemAt(i).getUri());
                        } else if (result.getData().getData() != null) {
                            imagenesSeleccionadas.add(result.getData().getData());
                        }
                        tvImagenesSeleccionadas.setText(imagenesSeleccionadas.size() + " imágenes seleccionadas");
                    }
                }
        );
    }

    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            progressBar.setVisibility(View.GONE);
            if (location != null) {
                latitud = location.getLatitude();
                longitud = location.getLongitude();
                ubicacionObtenida = true;
                obtenerDireccion(latitud, longitud);
            }
        });
    }

    private void obtenerDireccion(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                editDireccion.setText(addresses.get(0).getAddressLine(0));
                editZona.setText(addresses.get(0).getLocality());
            }
        } catch (IOException ignored) {}
    }

    private void abrirGaleria() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickImageLauncher.launch(android.content.Intent.createChooser(intent, "Selecciona imágenes"));
    }

    private void guardarObservacion() {
        if (!validarFormulario()) return;

        String titulo = editTitulo.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();
        String tipoStr = spinnerTipo.getText().toString().trim();
        String especie = editEspecie.getText().toString().trim();
        String zona = editZona.getText().toString().trim();
        String direccion = editDireccion.getText().toString().trim();
        String nombreTradicional = editNombreTradicional.getText().toString().trim();
        
        String fechaISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

        int idTipo = "RINCON".equals(tipoStr) ? 2 : ("DENUNCIA".equals(tipoStr) ? 3 : 1);
        
        ObservacionRequest request = new ObservacionRequest(
                titulo, descripcion, fechaISO, 
                new ObservacionRequest.TipoRequest(idTipo), 
                especie, latitud, longitud, direccion, zona, nombreTradicional,
                new ObservacionRequest.UsuarioRequest(sessionManager.getUserId()),
                "ABIERTA"
        );

        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.crearObservacion(request).enqueue(new Callback<Observacion>() {
            @Override
            public void onResponse(Call<Observacion> call, Response<Observacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!imagenesSeleccionadas.isEmpty()) {
                        subirImagenes(response.body().getId());
                    } else {
                        finalizar();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnGuardar.setEnabled(true);
                    Toast.makeText(CrearObservacionActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Observacion> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);
                Toast.makeText(CrearObservacionActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validarFormulario() {
        if (editTitulo.getText().toString().isEmpty()) return false;
        if (!ubicacionObtenida) {
            Toast.makeText(this, "Obtén tu ubicación primero", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void subirImagenes(long idObs) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        final int[] subidas = {0};
        for (Uri uri : imagenesSeleccionadas) {
            String b64 = convertirUriABase64(uri);
            if (b64 != null) {
                apiService.subirImagen(idObs, new Imagen(b64, "Foto")).enqueue(new Callback<Imagen>() {
                    @Override public void onResponse(Call<Imagen> c, Response<Imagen> r) {
                        subidas[0]++;
                        if (subidas[0] == imagenesSeleccionadas.size()) finalizar();
                    }
                    @Override public void onFailure(Call<Imagen> c, Throwable t) {
                        subidas[0]++;
                        if (subidas[0] == imagenesSeleccionadas.size()) finalizar();
                    }
                });
            }
        }
    }

    private String convertirUriABase64(Uri uri) {
        try {
            Bitmap b = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) { return null; }
    }

    private void finalizar() {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "¡Guardado!", Toast.LENGTH_SHORT).show();
        finish();
    }
}

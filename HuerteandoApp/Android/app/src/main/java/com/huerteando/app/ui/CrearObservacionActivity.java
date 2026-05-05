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
import com.huerteando.app.clases.Especie;
import com.huerteando.app.clases.Imagen;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.clases.TipoObservacion;
import com.huerteando.app.clases.Usuario;
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
 * Adaptada al nuevo manual (uso directo de Observacion model).
 */
public class CrearObservacionActivity extends AppCompatActivity {

    private TextInputEditText editTitulo, editDescripcion, editZona, editDireccion, editFecha, editNombreTradicional;
    private AutoCompleteTextView spinnerTipo, spinnerEspecie;
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
    private List<Especie> especiesCatalogo = new ArrayList<>();

    // Nombres para el spinner (IDs internos 1, 2, 3)
    private final String[] nombresTipos = {"Planta", "Rincón de interés", "Incidencia ambiental"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_observacion);
        
        sessionManager = new SessionManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupToolbar();
        setupImagePicker();
        cargarEspecies();
        
        // Configurar spinner tipos
        ArrayAdapter<String> adapterTipos = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nombresTipos);
        spinnerTipo.setAdapter(adapterTipos);

        // Fecha actual (formato legible para UI)
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
        spinnerEspecie = findViewById(R.id.spinnerEspecie);
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

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.nueva_observacion);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void cargarEspecies() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getEspecies().enqueue(new Callback<List<Especie>>() {
            @Override
            public void onResponse(Call<List<Especie>> call, Response<List<Especie>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    especiesCatalogo = response.body();
                    List<String> nombres = new ArrayList<>();
                    for (Especie e : especiesCatalogo) {
                        nombres.add(e.getNombreComun() + " (" + e.getNombreCientifico() + ")");
                    }
                    ArrayAdapter<String> adapterEspecies = new ArrayAdapter<>(CrearObservacionActivity.this,
                            android.R.layout.simple_dropdown_item_1line, nombres);
                    spinnerEspecie.setAdapter(adapterEspecies);
                }
            }
            @Override public void onFailure(Call<List<Especie>> call, Throwable t) {}
        });
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

        // Construir objeto Observacion segun manual
        Observacion obs = new Observacion();
        obs.setTitulo(editTitulo.getText().toString().trim());
        obs.setDescripcion(editDescripcion.getText().toString().trim());
        obs.setNombreZona(editZona.getText().toString().trim());
        obs.setDireccionTxt(editDireccion.getText().toString().trim());
        obs.setNombreTradicional(editNombreTradicional.getText().toString().trim());
        obs.setLatitud(latitud);
        obs.setLongitud(longitud);
        obs.setEstadoObservacion("ABIERTA");
        
        // Formato ISO para el backend
        String fechaISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
        obs.setFechaObservacion(fechaISO);

        // Seteamos Tipo como objeto con ID
        String tipoStr = spinnerTipo.getText().toString().trim();
        TipoObservacion tipo = new TipoObservacion();
        if (tipoStr.equals(nombresTipos[1])) tipo.setId(2);
        else if (tipoStr.equals(nombresTipos[2])) tipo.setId(3);
        else tipo.setId(1);
        obs.setTipoObservacion(tipo);

        // Seteamos Usuario como objeto con ID
        Usuario user = new Usuario();
        user.setId(sessionManager.getUserId());
        obs.setUsuario(user);

        // Seteamos Especie si hay
        String especieTxt = spinnerEspecie.getText().toString().trim();
        if (!especieTxt.isEmpty()) {
            // Buscamos el ID en la lista local si lo necesitamos, 
            // pero el manual solo muestra el objeto Especie.
            // Para simplificar segun manual, si el backend permite crear/vincular por nombre, 
            // pero normalmente se enviaria el ID si existe.
            for (Especie e : especiesCatalogo) {
                if (especieTxt.contains(e.getNombreCientifico())) {
                    obs.setEspecie(e);
                    break;
                }
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.crearObservacion(obs).enqueue(new Callback<Observacion>() {
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
        if (spinnerTipo.getText().toString().isEmpty()) return false;
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
                Imagen img = new Imagen();
                img.setUrlArchivo(b64); // El manual dice urlArchivo obligatorio
                img.setTitulo("Foto");

                apiService.subirImagen(idObs, img).enqueue(new Callback<Imagen>() {
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
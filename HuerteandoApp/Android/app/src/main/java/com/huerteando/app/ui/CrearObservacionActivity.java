package com.huerteando.app.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.huerteando.app.R;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.Especie;
import com.huerteando.app.clases.Imagen;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.clases.TipoObservacion;
import com.huerteando.app.clases.Usuario;
import com.huerteando.app.utils.ErrorUtils;
import com.huerteando.app.utils.ImageUtils;
import com.huerteando.app.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity para crear una nueva observación.
 * Adaptada al nuevo manual (uso directo de Observacion model).
 */
public class CrearObservacionActivity extends AppCompatActivity {

    private static final String TAG = "CrearObservacion";

    private TextInputEditText editTitulo, editDescripcion, editZona, editDireccion, editFecha, editNombreTradicional;
    private TextInputLayout layoutTitulo, layoutTipo;
    private MaterialAutoCompleteTextView spinnerTipo, spinnerEspecie;
    private final List<Uri> imagenesSeleccionadas = new ArrayList<>();
    private android.widget.Button btnSeleccionarImagen;
    private MaterialButton btnEliminarImagenes;
    private android.widget.TextView tvImagenesSeleccionadas;
    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> pickImageLauncher;
    private MaterialButton btnGuardar, btnMiUbicacion;
    private android.widget.TextView tvError;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitud = 0, longitud = 0;
    private boolean ubicacionObtenida = false;
    private SessionManager sessionManager;
    private List<Especie> especiesCatalogo = new ArrayList<>();
    private List<TipoObservacion> tiposCatalogo = new ArrayList<>();
    private long idObservacionEdit = -1L;
    private Observacion observacionAEditar;

    // Nombres para el spinner (se cargarán de la API)
    private String[] nombresTipos = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_observacion);
        
        sessionManager = new SessionManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        idObservacionEdit = getIntent().getLongExtra("idObservacion", -1L);

        initViews();
        setupToolbar();
        setupImagePicker();
        cargarTipos();
        cargarEspecies();

        if (idObservacionEdit != -1L) {
            cargarDatosObservacion();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Editar Observación");
            }
            btnGuardar.setText("Actualizar");
        } else {
            // Fecha actual (formato legible para UI)
            editFecha.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));
        }
        editFecha.setEnabled(false);

        btnMiUbicacion.setOnClickListener(v -> obtenerUbicacionActual());
        btnGuardar.setOnClickListener(v -> guardarObservacion());
        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnEliminarImagenes.setOnClickListener(v -> eliminarImagenes());

        // Forzar despliegue de opciones al tocar el campo
        View.OnClickListener dropdownListener = v -> {
            MaterialAutoCompleteTextView s = (MaterialAutoCompleteTextView) v;
            if (s.getAdapter() != null) {
                s.showDropDown();
            } else {
                Toast.makeText(this, "Cargando datos, espera un momento...", Toast.LENGTH_SHORT).show();
            }
        };
        spinnerTipo.setOnClickListener(dropdownListener);
        spinnerEspecie.setOnClickListener(dropdownListener);

        // También al recibir foco
        spinnerTipo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) spinnerTipo.showDropDown();
        });
        spinnerEspecie.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) spinnerEspecie.showDropDown();
        });
    }

    private void initViews() {
        editTitulo = findViewById(R.id.editTitulo);
        editDescripcion = findViewById(R.id.editDescripcion);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerEspecie = findViewById(R.id.spinnerEspecie);
        layoutTitulo = findViewById(R.id.layoutTitulo);
        layoutTipo = findViewById(R.id.layoutTipo);
        editZona = findViewById(R.id.editZona);
        editDireccion = findViewById(R.id.editDireccion);
        editFecha = findViewById(R.id.editFecha);
        editNombreTradicional = findViewById(R.id.editNombreTradicional);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnMiUbicacion = findViewById(R.id.btnMiUbicacion);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnEliminarImagenes = findViewById(R.id.btnEliminarImagenes);
        tvImagenesSeleccionadas = findViewById(R.id.tvImagenesSeleccionadas);
        tvError = findViewById(R.id.tvErrorCrear);
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

    private void cargarDatosObservacion() {
        Log.d(TAG, "Cargando datos para editar observación ID: " + idObservacionEdit);
        progressBar.setVisibility(View.VISIBLE);
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getObservacion(idObservacionEdit).enqueue(new Callback<Observacion>() {
            @Override
            public void onResponse(Call<Observacion> call, Response<Observacion> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    observacionAEditar = response.body();
                    rellenarCampos();
                } else {
                    Log.e(TAG, "Error al cargar datos de edición: " + response.code());
                    Toast.makeText(CrearObservacionActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Observacion> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Fallo de red al cargar datos de edición", t);
                Toast.makeText(CrearObservacionActivity.this, "Sin conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rellenarCampos() {
        editTitulo.setText(observacionAEditar.getTitulo());
        editDescripcion.setText(observacionAEditar.getDescripcion());
        editZona.setText(observacionAEditar.getNombreZona());
        editDireccion.setText(observacionAEditar.getDireccionTxt());
        editNombreTradicional.setText(observacionAEditar.getNombreTradicional());
        latitud = observacionAEditar.getLatitud();
        longitud = observacionAEditar.getLongitud();
        ubicacionObtenida = true;

        if (observacionAEditar.getTipoObservacion() != null) {
            spinnerTipo.setText(observacionAEditar.getTipoObservacion().getNombre(), false);
        }

        if (observacionAEditar.getEspecie() != null) {
            String espText = observacionAEditar.getEspecie().getNombreComun() + " (" + observacionAEditar.getEspecie().getNombreCientifico() + ")";
            spinnerEspecie.setText(espText, false);
        }

        // Formatear fecha para mostrar
        try {
            String iso = observacionAEditar.getFechaObservacion();
            if (iso != null) {
                String limpia = iso.split("\\.")[0].replace("T", " ");
                SimpleDateFormat sdfIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = sdfIso.parse(limpia);
                editFecha.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date));
            }
        } catch (Exception e) {
            editFecha.setText(observacionAEditar.getFechaObservacion());
        }

        if (observacionAEditar.getImagenes() != null && !observacionAEditar.getImagenes().isEmpty()) {
            tvImagenesSeleccionadas.setText("La observación ya tiene imágenes. Puedes añadir más.");
        }
    }

    private void cargarTipos() {
        Log.d(TAG, "Cargando tipos de observación...");
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getTipos().enqueue(new Callback<List<TipoObservacion>>() {
            @Override
            public void onResponse(Call<List<TipoObservacion>> call, Response<List<TipoObservacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tiposCatalogo = response.body();
                    nombresTipos = new String[tiposCatalogo.size()];
                    for (int i = 0; i < tiposCatalogo.size(); i++) {
                        nombresTipos[i] = tiposCatalogo.get(i).getNombre();
                    }
                    ArrayAdapter<String> adapterTipos = new ArrayAdapter<>(CrearObservacionActivity.this,
                            android.R.layout.simple_dropdown_item_1line, nombresTipos);
                    spinnerTipo.setAdapter(adapterTipos);
                    spinnerTipo.setThreshold(0);
                    Log.d(TAG, "Tipos cargados: " + tiposCatalogo.size());
                } else {
                    Log.e(TAG, "Error al cargar tipos: " + response.code());
                }
            }
            @Override public void onFailure(Call<List<TipoObservacion>> call, Throwable t) {
                Log.e(TAG, "Fallo de red al cargar tipos", t);
            }
        });
    }

    private void cargarEspecies() {
        Log.d(TAG, "Cargando catálogo de especies...");
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getEspecies().enqueue(new Callback<List<Especie>>() {
            @Override
            public void onResponse(Call<List<Especie>> call, Response<List<Especie>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Especies cargadas: " + response.body().size());
                    especiesCatalogo = response.body();
                    List<String> nombres = new ArrayList<>();
                    for (Especie e : especiesCatalogo) {
                        nombres.add(e.getNombreComun() + " (" + e.getNombreCientifico() + ")");
                    }
                    ArrayAdapter<String> adapterEspecies = new ArrayAdapter<>(CrearObservacionActivity.this,
                            android.R.layout.simple_dropdown_item_1line, nombres);
                    spinnerEspecie.setAdapter(adapterEspecies);
                    spinnerEspecie.setThreshold(0);
                } else {
                    Log.e(TAG, "Error al cargar especies: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<Especie>> call, Throwable t) {
                Log.e(TAG, "Fallo de red al cargar especies", t);
            }
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
                        actualizarVistaImagenes();
                    }
                }
        );
    }

    private void actualizarVistaImagenes() {
        if (imagenesSeleccionadas.isEmpty()) {
            tvImagenesSeleccionadas.setText("Ninguna imagen seleccionada");
            btnEliminarImagenes.setVisibility(View.GONE);
        } else {
            tvImagenesSeleccionadas.setText(imagenesSeleccionadas.size() + " imágenes seleccionadas");
            btnEliminarImagenes.setVisibility(View.VISIBLE);
        }
    }

    private void eliminarImagenes() {
        imagenesSeleccionadas.clear();
        actualizarVistaImagenes();
        Log.d(TAG, "Imágenes deseleccionadas.");
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

        // Seteamos Tipo como objeto con ID real de la base de datos
        String tipoStr = spinnerTipo.getText().toString().trim();
        TipoObservacion tipo = null;
        for (TipoObservacion t : tiposCatalogo) {
            if (t.getNombre().equalsIgnoreCase(tipoStr)) {
                tipo = t;
                break;
            }
        }
        
        if (tipo == null) {
            mostrarError("Selecciona un tipo válido");
            return;
        }
        obs.setTipoObservacion(tipo);

        // Seteamos Usuario como objeto con ID
        Usuario user = new Usuario();
        user.setId(sessionManager.getUserId());
        obs.setUsuario(user);

        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Observacion> call;

        if (idObservacionEdit != -1L) {
            Log.d(TAG, "Editando observación existente ID: " + idObservacionEdit);
            obs.setId(idObservacionEdit);
            // Mantenemos la fecha original si estamos editando
            obs.setFechaObservacion(observacionAEditar.getFechaObservacion());
            call = apiService.editarObservacion(idObservacionEdit, obs);
        } else {
            Log.d(TAG, "Enviando nueva observación al servidor...");
            call = apiService.crearObservacion(obs);
        }

        call.enqueue(new Callback<Observacion>() {
            @Override
            public void onResponse(Call<Observacion> call, Response<Observacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long idFinal = response.body().getId();
                    Log.d(TAG, "¡Éxito! Observación guardada con ID: " + idFinal);
                    if (!imagenesSeleccionadas.isEmpty()) {
                        subirImagenes(idFinal);
                    } else {
                        finalizar(idFinal);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnGuardar.setEnabled(true);
                    Log.e(TAG, "Error del servidor al guardar observación: " + response.code());
                    ErrorUtils.mostrarToastError(CrearObservacionActivity.this, response.code());
                }
            }

            @Override
            public void onFailure(Call<Observacion> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);
                Log.e(TAG, "Error crítico de conexión", t);
                Toast.makeText(CrearObservacionActivity.this,
                        "Sin conexión: Comprueba tu internet y el servidor",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validarFormulario() {
        layoutTitulo.setError(null);
        layoutTipo.setError(null);

        boolean valido = true;

        if (editTitulo.getText().toString().isEmpty()) {
            layoutTitulo.setError("El título es obligatorio");
            valido = false;
        }
        if (spinnerTipo.getText().toString().isEmpty()) {
            layoutTipo.setError("Selecciona un tipo de observación");
            valido = false;
        }
        if (!ubicacionObtenida) {
            Toast.makeText(this, "Obtén tu ubicación primero", Toast.LENGTH_SHORT).show();
            valido = false;
        }
        return valido;
    }

    private void subirImagenes(long idObs) {
        Log.d(TAG, "Subiendo " + imagenesSeleccionadas.size() + " imágenes para observación " + idObs);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        final int[] subidas = {0};
        final int[] errores = {0};

        
        for (int i = 0; i < imagenesSeleccionadas.size(); i++) {
            Uri uri = imagenesSeleccionadas.get(i);
            try {
                // Usamos ImageUtils para comprimir y asegurar tamaño < 1MB
                File file = ImageUtils.compressImage(this, uri, "upload", i);
                if (file == null || !file.exists()) {
                    Log.e(TAG, "Error: El archivo comprimido no se pudo crear para la URI: " + uri);
                    subidas[0]++;
                    if (subidas[0] == imagenesSeleccionadas.size()) finalizar(idObs);
                    continue;
                }

                Log.d(TAG, "Imagen comprimida lista: " + file.getName() + " Tamaño: " + file.length() + " bytes");

                String mimeType = getContentResolver().getType(uri);
                if (mimeType == null) mimeType = "image/jpeg";
                
                // Sintaxis correcta para OkHttp 4.x
                RequestBody requestFile = RequestBody.create(file, MediaType.parse(mimeType));
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                RequestBody tituloPart = RequestBody.create("Foto de observación", MediaType.parse("text/plain"));

                apiService.subirImagen(idObs, body, tituloPart).enqueue(new Callback<Imagen>() {
                    @Override
                    public void onResponse(Call<Imagen> c, Response<Imagen> r) {
                        subidas[0]++;

                        if (r.isSuccessful()) {
                            Log.d(TAG, "Imagen subida OK");
                        } else {
                            errores[0]++;
                            Log.e(TAG, "Error al subir imagen. Código: " + r.code());
                        }

                        if (subidas[0] == imagenesSeleccionadas.size()) {
                            if (errores[0] > 0) {
                                Toast.makeText(CrearObservacionActivity.this,
                                        errores[0] + " imágenes fallaron",
                                        Toast.LENGTH_LONG).show();
                            }
                            finalizar(idObs);
                        }
                    }
                    @Override public void onFailure(Call<Imagen> c, Throwable t) {
                        subidas[0]++;
                        Log.e(TAG, "Fallo de red al subir imagen", t);
                        if (subidas[0] == imagenesSeleccionadas.size()) finalizar(idObs);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error procesando imagen", e);
                subidas[0]++;
                if (subidas[0] == imagenesSeleccionadas.size()) finalizar(idObs);
            }
        }
    }


    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
    }

    private void finalizar(long idObs) {
        progressBar.setVisibility(View.GONE);
        Log.d(TAG, "Proceso finalizado correctamente. Navegando a detalle ID: " + idObs);

        Intent intent = new Intent(this, DetalleObservacionActivity.class);
        intent.putExtra("idObservacion", idObs);

        // Si hay imágenes locales, pasamos la primera para previsualización inmediata
        if (!imagenesSeleccionadas.isEmpty()) {
            String uriStr = imagenesSeleccionadas.get(0).toString();
            intent.putExtra("imagen", uriStr);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.d(TAG, "Extras: idObservacion=" + idObs + ", imagen=" + uriStr);
        }

        startActivity(intent);

        String msg = idObservacionEdit != -1L ? "¡Observación actualizada! 🌱" : "¡Observación compartida con éxito! 🌱";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }
}

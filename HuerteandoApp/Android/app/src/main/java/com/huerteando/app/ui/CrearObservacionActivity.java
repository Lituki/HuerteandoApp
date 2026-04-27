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
import java.math.BigDecimal;
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
 * 
 * Refactorizada para subir imágenes en formato Base64 (JSON) y evitar errores de Multipart.
 * Las coordenadas se envían como BigDecimal y la fecha en formato ISO.
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
    private final List<Uri> imagenesSeleccionadas = new ArrayList<>();
    private android.widget.Button btnSeleccionarImagen;
    private android.widget.TextView tvImagenesSeleccionadas;
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
                                Uri uri = data.getClipData().getItemAt(i).getUri();
                                imagenesSeleccionadas.add(uri);
                            }
                        } else if (data.getData() != null) {
                            imagenesSeleccionadas.add(data.getData());
                        }

                        String mensaje = imagenesSeleccionadas.size() + " imágenes seleccionadas";
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();

                        if (tvImagenesSeleccionadas != null) {
                            tvImagenesSeleccionadas.setText(mensaje);
                            tvImagenesSeleccionadas.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        }
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
        tvImagenesSeleccionadas = findViewById(R.id.tvImagenesSeleccionadas);
        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        progressBar = findViewById(R.id.progressBar);

        // Configurar spinner de tipo
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, tipos);
        spinnerTipo.setAdapter(adapter);

        // Mostrar fecha y hora actual del teléfono
        String fechaVisible = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        editFecha.setText(fechaVisible);
        editFecha.setEnabled(false);
        editFecha.setFocusable(false);

        // Botón de mi ubicación
        btnMiUbicacion.setOnClickListener(v -> obtenerUbicacionActual());

        // Botón guardar
        btnGuardar.setOnClickListener(v -> guardarObservacion());
        btnGuardar.setEnabled(true);
    }

    private void obtenerUbicacionActual() {
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
                        obtenerDireccionDesdeCoordenadas(latitud, longitud);
                    } else {
                        ubicacionObtenida = false;
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
                editZona.setText(zonaTexto);
            }
        } catch (IOException e) {
            Toast.makeText(this, "No se pudo obtener la dirección", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirGaleria() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickImageLauncher.launch(android.content.Intent.createChooser(intent, "Selecciona imágenes"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual();
        } else {
            ubicacionObtenida = false;
            Toast.makeText(this, "Sin GPS no se puede geolocalizar la observación.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Sube las imágenes seleccionadas al servidor convirtiéndolas a Base64.
     * Se envían como objetos Imagen vía JSON.
     */
    private void subirImagenes(long idObservacion) {
        if (imagenesSeleccionadas.isEmpty()) {
            finalizarYSalir();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        final int total = imagenesSeleccionadas.size();
        final int[] contador = {0};

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        for (Uri uri : imagenesSeleccionadas) {
            String base64Imagen = convertirUriABase64(uri);

            if (base64Imagen != null) {
                Imagen imagenObj = new Imagen();
                // El Backend espera el Base64 en el campo urlArchivo
                imagenObj.setUrlArchivo(base64Imagen);
                imagenObj.setTitulo("Obs_" + idObservacion + "_" + System.currentTimeMillis());

                apiService.subirImagen(idObservacion, imagenObj).enqueue(new Callback<Imagen>() {
                    @Override
                    public void onResponse(Call<Imagen> call, Response<Imagen> response) {
                        contador[0]++;
                        if (contador[0] == total) {
                            finalizarYSalir();
                        }
                    }

                    @Override
                    public void onFailure(Call<Imagen> call, Throwable t) {
                        contador[0]++;
                        if (contador[0] == total) {
                            finalizarYSalir();
                        }
                    }
                });
            } else {
                contador[0]++;
                if (contador[0] == total) {
                    finalizarYSalir();
                }
            }
        }
    }

    /**
     * Procesa la imagen localmente: carga el Bitmap, lo redimensiona y lo comprime
     * para evitar que el String Base64 sea demasiado grande (Error 413).
     */
    private String convertirUriABase64(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            if (bitmap == null) return null;

            // Redimensionado para evitar Payload Too Large (max 1024px)
            int maxSize = 1024;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            if (width > maxSize || height > maxSize) {
                float ratio = (float) width / (float) height;
                if (ratio > 1) {
                    width = maxSize;
                    height = (int) (width / ratio);
                } else {
                    height = maxSize;
                    width = (int) (height * ratio);
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Compresión al 60% para balancear calidad y tamaño
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] imageBytes = baos.toByteArray();
            
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void finalizarYSalir() {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "¡Observación e imágenes guardadas!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void guardarObservacion() {
        String titulo = editTitulo.getText() != null ? editTitulo.getText().toString().trim() : "";
        String descripcion = editDescripcion.getText() != null ? editDescripcion.getText().toString().trim() : "";
        String tipo = spinnerTipo.getText() != null ? spinnerTipo.getText().toString().trim() : "";
        String zona = editZona.getText() != null ? editZona.getText().toString().trim() : "";
        String direccion = editDireccion.getText() != null ? editDireccion.getText().toString().trim() : "";

        if (titulo.isEmpty()) {
            editTitulo.setError("Campo obligatorio");
            return;
        }
        if (tipo.isEmpty()) {
            spinnerTipo.setError("Selecciona un tipo");
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
        if (!ubicacionObtenida) {
            Toast.makeText(this, "Debes seleccionar una ubicación", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("DENUNCIA".equals(tipo) && imagenesSeleccionadas.isEmpty()) {
            Toast.makeText(this, "Las denuncias ambientales requieren al menos una imagen.", Toast.LENGTH_LONG).show();
            return;
        }

        String especie = editEspecie.getText() != null ? editEspecie.getText().toString().trim() : "";
        String nombreTradicional = editNombreTradicional.getText() != null ? 
                editNombreTradicional.getText().toString().trim() : "";

        // Fecha y hora automáticas en formato ISO (requerido por backend LocalDateTime)
        String fechaActualISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

        // Obtenemos el ID del usuario logueado
        Long idUsuario = sessionManager.getUserId();
        ObservacionRequest.UsuarioRequest usuarioObjeto = new ObservacionRequest.UsuarioRequest(idUsuario);

        // Mapeo del tipo de observación
        int idTipo = 1; // PLANTA
        if ("RINCON".equals(tipo)) idTipo = 2;
        if ("DENUNCIA".equals(tipo)) idTipo = 3;
        ObservacionRequest.TipoRequest tipoObjeto = new ObservacionRequest.TipoRequest(idTipo);

        // Convertir coordenadas a BigDecimal para el backend
        BigDecimal latBD = new BigDecimal(String.valueOf(latitud));
        BigDecimal lonBD = new BigDecimal(String.valueOf(longitud));

        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        ObservacionRequest request = new ObservacionRequest(
                titulo, descripcion, fechaActualISO, tipoObjeto, especie,
                latBD, lonBD, direccion, zona, nombreTradicional,
                usuarioObjeto
        );

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Observacion> call = apiService.crearObservacion(request);
        call.enqueue(new Callback<Observacion>() {
            @Override
            public void onResponse(Call<Observacion> call, Response<Observacion> response) {
                progressBar.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    if (!imagenesSeleccionadas.isEmpty()) {
                        subirImagenes(response.body().getId());
                    } else {
                        Toast.makeText(CrearObservacionActivity.this, "¡Observación creada!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(CrearObservacionActivity.this, "Error: " + response.code(), Toast.LENGTH_LONG).show();
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

package com.huerteando.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.huerteando.app.R;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.Usuario;
import com.huerteando.app.utils.ImageUtils;
import com.huerteando.app.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de perfil del usuario.
 * Muestra los datos de la sesión actual y permite cerrar sesión.
 */
public class PerfilActivity extends AppCompatActivity {

    private static final String TAG = "PerfilActivity";

    private TextView tvNombre, tvApellidos, tvEmail, tvFecha, tvNick, tvRol;
    private ShapeableImageView ivAvatar;
    private SessionManager session;

    // Lanzador para cambiar avatar
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Log.d(TAG, "Nuevo avatar seleccionado: " + uri.toString());
                    // 1. Mostrar preview local inmediata con Glide
                    com.bumptech.glide.Glide.with(this).load(uri).into(ivAvatar);
                    
                    // 2. Subir al servidor
                    subirAvatarAlServidor(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Iniciando PerfilActivity...");
        setContentView(R.layout.activity_perfil);

        session = new SessionManager(this);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPerfil);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Enlazar vistas
        tvNombre = findViewById(R.id.tvPerfilNombre);
        tvApellidos = findViewById(R.id.tvPerfilApellidos);
        tvEmail = findViewById(R.id.tvPerfilEmail);
        tvFecha = findViewById(R.id.tvPerfilFecha);
        tvNick = findViewById(R.id.tvPerfilNick);
        tvRol = findViewById(R.id.tvPerfilRol);
        ivAvatar = findViewById(R.id.ivPerfilAvatar);
        MaterialButton btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        MaterialButton btnCambiarAvatar = findViewById(R.id.btnCambiarAvatar);
        MaterialButton btnEliminarAvatar = findViewById(R.id.btnEliminarAvatar);

        // Cargar datos de la sesión
        cargarDatosUsuario();
        sincronizarConServidor();

        // Lógica de avatar
        btnCambiarAvatar.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnEliminarAvatar.setOnClickListener(v -> {
            ivAvatar.setImageResource(R.drawable.ic_avatar_plant);
            Toast.makeText(this, "Avatar eliminado", Toast.LENGTH_SHORT).show();
        });

        // Lógica de cerrar sesión
        btnCerrarSesion.setOnClickListener(v -> {
            session.cerrarSesion();
            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
            // Limpiar el stack de actividades para que no pueda volver atrás
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void subirAvatarAlServidor(Uri uri) {
        try {
            // Usamos ImageUtils para comprimir el avatar < 1MB
            File file = ImageUtils.compressImage(this, uri, "avatar", 0);
            if (file == null || !file.exists()) {
                Log.e(TAG, "Error: No se pudo comprimir el avatar");
                Toast.makeText(this, "Error al procesar imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Avatar comprimido listo: " + file.getName() + " Tamaño: " + file.length() + " bytes");

            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "image/jpeg";

            RequestBody requestFile = RequestBody.create(file, MediaType.parse(mimeType));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            ApiService api = ApiClient.getClient().create(ApiService.class);
            api.subirAvatar(session.getUserId(), body).enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Usuario u = response.body();
                        Log.d(TAG, "Avatar subido con éxito: " + u.getAvatarUrl());
                        // 3. Actualizar sesión con la URL remota
                        session.guardarDatosCompletos(u.getId(), u.getNick(), u.getNombre(), u.getApellidos(), u.getEmail(), u.getFechaRegistro(), u.getRol(), u.getAvatarUrl());
                        // 4. Cargar desde URL remota para confirmar y evitar SecurityException
                        cargarDatosUsuario();
                        Toast.makeText(PerfilActivity.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Error al subir avatar: " + response.code());
                        Toast.makeText(PerfilActivity.this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Usuario> call, Throwable t) {
                    Log.e(TAG, "Fallo de red al subir avatar", t);
                    Toast.makeText(PerfilActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error al procesar avatar para subir", e);
        }
    }

    // El método copiarUriACache ya no es necesario

    private void sincronizarConServidor() {
        if (!session.haySesion()) return;
        
        Log.d(TAG, "Sincronizando perfil con el servidor...");
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getPerfil(session.getUserId()).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario u = response.body();
                    session.guardarDatosCompletos(u.getId(), u.getNick(), u.getNombre(), u.getApellidos(), u.getEmail(), u.getFechaRegistro(), u.getRol(), u.getAvatarUrl());
                    cargarDatosUsuario();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Log.e(TAG, "Error al sincronizar perfil", t);
            }
        });
    }

    private void cargarDatosUsuario() {
        if (session.haySesion()) {
            // Añadimos comprobaciones de seguridad para evitar el crash
            String nombre = session.getNombre() != null ? session.getNombre() : "Usuario";
            String apellidos = session.getApellidos() != null ? session.getApellidos() : "";
            String email = session.getEmail() != null ? session.getEmail() : "";
            String fecha = session.getFechaRegistro() != null ? session.getFechaRegistro() : "";
            String nick = session.getNick() != null ? session.getNick() : "sin_nick";
            String rol = session.getRol() != null ? session.getRol() : "USUARIO";

            tvNombre.setText(nombre);
            tvApellidos.setText(apellidos);
            tvEmail.setText(email);
            
            // Formatear fecha si es ISO
            if (fecha.contains("T")) {
                tvFecha.setText(fecha.split("T")[0]);
            } else {
                tvFecha.setText(fecha);
            }

            tvNick.setText(String.format(Locale.getDefault(), "@%s", nick));
            tvRol.setText(rol);

            // Cargar avatar usando Glide para evitar problemas de permisos de URI
            String avatarUrl = session.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("default_avatar")) {
                // Si la URL es relativa, le añadimos la BASE_URL
                if (!avatarUrl.startsWith("http")) {
                    String base = com.huerteando.app.api.ApiClient.BASE_URL;
                    if (base.endsWith("/") && avatarUrl.startsWith("/")) {
                        avatarUrl = base + avatarUrl.substring(1);
                    } else if (!base.endsWith("/") && !avatarUrl.startsWith("/")) {
                        avatarUrl = base + "/" + avatarUrl;
                    } else {
                        avatarUrl = base + avatarUrl;
                    }
                }

                com.bumptech.glide.Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_avatar_plant)
                        .error(R.drawable.ic_avatar_plant)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_avatar_plant);
            }
        }
    }
}

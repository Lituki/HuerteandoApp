package com.huerteando.app.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.huerteando.app.R;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.RegistroRequest;
import com.huerteando.app.clases.Usuario;
import com.huerteando.app.utils.ErrorUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de Registro - Pantalla para crear una cuenta nueva
 *
 * ¿Qué hace esta clase?
 * 1. Permite al usuario registrarse con: nick, contraseña, nombre, apellidos, email
 * 2. Envía los datos al servidor (API REST)
 * 3. Si el registro es exitoso, vuelve al login
 */
public class RegistroActivity extends AppCompatActivity {

    private static final String TAG = "RegistroActivity";

    // Elementos del layout
    private TextInputEditText editNick;
    private TextInputEditText editPassword;
    private TextInputEditText editConfirmarPassword;
    private TextInputEditText editNombre;
    private TextInputEditText editApellidos;
    private TextInputEditText editEmail;
    private MaterialButton btnRegistro;
    private TextView tvError;
    private TextView tvIrALogin;

    // Avatar
    private ShapeableImageView imgAvatar;
    private MaterialButton btnSeleccionarAvatar;
    private String avatarUriString = "default_avatar"; // Valor por defecto

    // Lanzador para abrir la galería
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Si el usuario selecciona una imagen, la mostramos y guardamos su URI
                    imgAvatar.setImageURI(uri);
                    avatarUriString = uri.toString();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        // Conectar las variables con los elementos del layout
        editNick = findViewById(R.id.editNick);
        editPassword = findViewById(R.id.editPassword);
        editConfirmarPassword = findViewById(R.id.editConfirmarPassword);
        editNombre = findViewById(R.id.editNombre);
        editApellidos = findViewById(R.id.editApellidos);
        editEmail = findViewById(R.id.editEmail);
        btnRegistro = findViewById(R.id.btnRegistro);
        tvError = findViewById(R.id.tvError);
        tvIrALogin = findViewById(R.id.tvIrALogin);

        // Inicializar elementos del avatar
        imgAvatar = findViewById(R.id.imgAvatar);
        btnSeleccionarAvatar = findViewById(R.id.btnSeleccionarAvatar);

        // Configurar botón para seleccionar avatar
        btnSeleccionarAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir la galería para seleccionar solo imágenes
                galleryLauncher.launch("image/*");
            }
        });

        // Volver al login al pulsar el texto
        tvIrALogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Cierra esta pantalla y vuelve a la anterior (LoginActivity)
            }
        });

        // Botón de registro
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarRegistro(); // Llamamos a la función de registro
            }
        });
    }
    private void realizarRegistro() {
        // Obtener valores
        String nick = editNick.getText() != null ? editNick.getText().toString().trim() : "";
        String password = editPassword.getText() != null ? editPassword.getText().toString().trim() : "";
        String confirmarPassword = editConfirmarPassword.getText() != null ? editConfirmarPassword.getText().toString().trim() : "";
        String nombre = editNombre.getText() != null ? editNombre.getText().toString().trim() : "";
        String apellidos = editApellidos.getText() != null ? editApellidos.getText().toString().trim() : "";
        String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";

        Log.d(TAG, "Iniciando proceso de registro para: " + nick + " (" + email + ")");

        // Validaciones
        if (nick.isEmpty() || password.isEmpty() || nombre.isEmpty() || email.isEmpty()) {
            Log.w(TAG, "Validación fallida: Campos obligatorios vacíos");
            mostrarError("Por favor, completa los campos obligatorios");
            return;
        }

        // Validar que el formato del correo electrónico sea correcto
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.w(TAG, "Validación fallida: Formato de email incorrecto");
            mostrarError("Por favor, introduce un correo electrónico válido (ejemplo@correo.com)");
            return;
        }

        if (!password.equals(confirmarPassword)) {
            Log.w(TAG, "Validación fallida: Contraseñas no coinciden");
            mostrarError("Las contraseñas no coinciden");
            return;
        }

        // Seguridad de la contraseña: Mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 carácter especial
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$";
        if (!password.matches(passwordPattern)) {
            Log.w(TAG, "Validación fallida: Contraseña débil");
            mostrarError("La contraseña debe tener al menos 8 letras, 1 mayúscula, 1 minúscula y 1 símbolo");
            return;
        }

        // Ocultar error y desactivar botón
        tvError.setVisibility(View.GONE);
        btnRegistro.setEnabled(false);
        btnRegistro.setText("Registrando...");

        // Llamar al API
        Log.d(TAG, "Enviando datos al servidor...");
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        RegistroRequest request = new RegistroRequest(nick, password, nombre, apellidos, email, avatarUriString);

        Call<Usuario> call = apiService.registrar(request);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                btnRegistro.setEnabled(true);
                btnRegistro.setText("Registrarse");

                if (response.isSuccessful()) {
                    Log.d(TAG, "¡Registro completado con éxito! Usuario creado.");
                    Toast.makeText(RegistroActivity.this, "¡Bienvenido/a! Ya puedes iniciar sesión 🌿", Toast.LENGTH_LONG).show();
                    // Volver al login
                    finish();
                } else {
                    int code = response.code();
                    Log.e(TAG, "Error en el servidor al registrar: " + code);
                    mostrarError(ErrorUtils.getMensajeError(code));
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                btnRegistro.setEnabled(true);
                btnRegistro.setText("Registrarse");
                Log.e(TAG, "Fallo crítico de red en el registro", t);
                mostrarError("No se ha podido conectar con el servidor. Revisa tu internet.");
            }
        });
    }

    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
    }
}
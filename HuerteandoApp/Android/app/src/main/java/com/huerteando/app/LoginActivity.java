package com.huerteando.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.LoginRequest;
import com.huerteando.app.clases.LoginResponse;
import com.huerteando.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de Login - Pantalla de inicio de sesión
 * 
 * ¿Qué hace esta clase?
 * 1. Permite al usuario introducir su nick y contraseña
 * 2. Envía estos datos al servidor (API REST)
 * 3. Si el login es correcto, guarda la sesión y va a la pantalla principal
 * 4. Si hay error, muestra un mensaje
 */
public class LoginActivity extends AppCompatActivity {

    // Elementos del layout (vistas)
    private TextInputEditText editNick;      // Campo para el usuario
    private TextInputEditText editPassword;  // Campo para la contraseña
    private MaterialButton btnLogin;         // Botón de entrar
    private TextView tvError;                // Mensaje de error
    private TextView tvIrARegistro;          // Texto para ir a registro

    // Gestor de sesión (guarda el token en el dispositivo)
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Inicializar el gestor de sesión
        sessionManager = new SessionManager(this);

        // Verificar si ya hay sesión activa
        // Si ya está logueado, ir directamente a la pantalla principal
        if (sessionManager.haySesion()) {
            irAMain();
            return;
        }

        // Conectar las variables con los elementos del layout
        editNick = findViewById(R.id.editNick);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);
        tvIrARegistro = findViewById(R.id.tvIrARegistro);

        // Ajustar los márgenes para que no se superponga con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Programar el botón de login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarLogin();
            }
        });

        // Programar el texto de ir a registro
        tvIrARegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ir a la pantalla de registro
                Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Método para hacer el login
     * 1. Obtiene los datos de los campos de texto
     * 2. Valida que no estén vacíos
     * 3. Llama al API para verificar credenciales
     * 4. Guarda la sesión si es exitoso
     */
    private void realizarLogin() {
        // Obtener texto de los campos
        String nick = editNick.getText() != null ? editNick.getText().toString().trim() : "";
        String password = editPassword.getText() != null ? editPassword.getText().toString().trim() : "";

        // Validar que los campos no estén vacíos
        if (nick.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, completa todos los campos");
            return;
        }

        // Ocultar mensaje de error
        tvError.setVisibility(View.GONE);
        
        // Desactivar el botón mientras carga (para evitar doble click)
        btnLogin.setEnabled(false);
        btnLogin.setText("Cargando...");

        // Crear la llamada al API
        // Primero obtenemos el cliente sin token (para login)
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        
        // Crear objeto con las credenciales
        LoginRequest request = new LoginRequest(nick, password);
        
        // Hacer la llamada al servidor
        Call<LoginResponse> call = apiService.login(request);
        
        // Definir qué hacer cuando llegue la respuesta
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Reactivar el botón
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");

                if (response.isSuccessful()) {
                    // Login correcto - el servidor nos dio un token
                    LoginResponse loginResponse = response.body();
                    
                    if (loginResponse != null) {
                        // Guardar la sesión del usuario
                        sessionManager.guardarSesion(loginResponse);
                        
                        // Ir a la pantalla principal
                        irAMain();
                    } else {
                        mostrarError("Error al procesar la respuesta del servidor");
                    }
                } else {
                    // Login incorrecto (credenciales wrong)
                    if (response.code() == 401) {
                        mostrarError("Usuario o contraseña incorrectos");
                    } else if (response.code() == 404) {
                        mostrarError("Usuario no encontrado");
                    } else {
                        mostrarError("Error del servidor: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Reactivar el botón
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");

                // Error de conexión (sin internet, servidor caído, etc.)
                mostrarError("Error de conexión. ¿Estás conectado a internet?");
            }
        });
    }

    /**
     * Muestra un mensaje de error en la pantalla
     */
    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
    }

    /**
     * Navega a la pantalla principal de la app
     */
    private void irAMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // Limpiar el historial para que no pueda volver al login con el botón atrás
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}

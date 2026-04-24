package com.huerteando.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.huerteando.app.R;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.clases.RegistroRequest;
import com.huerteando.app.clases.Usuario;

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

        // Validaciones
        if (nick.isEmpty() || password.isEmpty() || nombre.isEmpty() || email.isEmpty()) {
            mostrarError("Por favor, completa los campos obligatorios");
            return;
        }

        if (!password.equals(confirmarPassword)) {
            mostrarError("Las contraseñas no coinciden");
            return;
        }

        if (password.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        // Ocultar error y desactivar botón
        tvError.setVisibility(View.GONE);
        btnRegistro.setEnabled(false);
        btnRegistro.setText("Registrando...");

        // Llamar al API
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        RegistroRequest request = new RegistroRequest(nick, password, nombre, apellidos, email);

        Call<Usuario> call = apiService.registrar(request);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                btnRegistro.setEnabled(true);
                btnRegistro.setText("Registrarse");

                if (response.isSuccessful()) {
                    Toast.makeText(RegistroActivity.this, "¡Registro exitoso! Ya puedes iniciar sesión", Toast.LENGTH_LONG).show();
                    // Volver al login
                    finish();
                } else {
                    if (response.code() == 409) {
                        mostrarError("El nick o email ya están en uso");
                    } else {
                        mostrarError("Error al registrar: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                btnRegistro.setEnabled(true);
                btnRegistro.setText("Registrarse");
                mostrarError("Error de conexión");
            }
        });
    }

    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
    }
}

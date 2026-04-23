package com.huerteando.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
 * Pantalla de inicio de sesión.
 *
 * Flujo:
 *   1. Si ya hay sesión activa → va directamente a ObservacionesActivity.
 *   2. El usuario introduce nick + contraseña → POST /api/auth/login.
 *   3. Si OK → guarda token en SessionManager → va a ObservacionesActivity.
 *   4. Si error → muestra mensaje debajo del botón.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editNick;
    private TextInputEditText editPassword;
    private MaterialButton    btnLogin;
    private TextView          tvError;
    private TextView          tvIrARegistro;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Si ya está logueado, saltar directamente a la app
        if (sessionManager.haySesion()) {
            irAObservaciones();
            return;
        }

        editNick      = findViewById(R.id.editNick);
        editPassword  = findViewById(R.id.editPassword);
        btnLogin      = findViewById(R.id.btnLogin);
        tvError       = findViewById(R.id.tvError);
        tvIrARegistro = findViewById(R.id.tvIrARegistro);

        // Padding para no solapar con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginLayout), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        btnLogin.setOnClickListener(v -> realizarLogin());

        tvIrARegistro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class)));
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void realizarLogin() {
        String nick     = texto(editNick);
        String password = texto(editPassword);

        if (nick.isEmpty()) {
            mostrarError("Por favor, introduce el nick de usuario");
            return;
        }
        if (password.isEmpty()) {
            mostrarError("Por favor, introduce la contraseña");
            return;
        }


        tvError.setVisibility(View.GONE);
        btnLogin.setEnabled(false);
        btnLogin.setText("Cargando…");

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.login(new LoginRequest(nick, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");

                if (response.isSuccessful() && response.body() != null) {
                    sessionManager.guardarSesion(response.body());
                    irAObservaciones();
                } else {
                    int code = response.code();
                    if (code == 401 || code == 403) {
                        mostrarError("Usuario o contraseña incorrectos");
                    } else if (code == 404) {
                        mostrarError("Usuario no encontrado");
                    } else {
                        mostrarError("Error del servidor (" + code + ")");
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");
                mostrarError("Error de conexión. ¿Tienes internet?");
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void irAObservaciones() {
        Intent intent = new Intent(LoginActivity.this, ObservacionesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    /** Devuelve el texto del campo recortado, o "" si es null. */
    private String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }
}
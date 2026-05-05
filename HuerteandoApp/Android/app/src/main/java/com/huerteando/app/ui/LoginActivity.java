package com.huerteando.app.ui;

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
import com.huerteando.app.R;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.api.ApiService;
import com.huerteando.app.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de inicio de sesión.
 * Actualizada para usar Map como respuesta segun el Manual.
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
        
        Map<String, String> credenciales = new HashMap<>();
        credenciales.put("nick", nick);
        credenciales.put("password", password);

        api.login(credenciales).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> datos = response.body();
                    
                    // GSON convierte numeros a Double en Map<String, Object>
                    Long idUsuario = ((Double) datos.get("id")).longValue();
                    String nickUsuario = (String) datos.get("nick");
                    String nombre = (String) datos.get("nombre");
                    
                    // Guardamos en sesion
                    sessionManager.guardarDatosSimples(idUsuario, nickUsuario, nombre);
                    
                    irAObservaciones();
                } else {
                    int code = response.code();
                    if (code == 401 || code == 403) {
                        mostrarError("Usuario o contraseña incorrectos");
                    } else {
                        mostrarError("Error del servidor (" + code + ")");
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");
                mostrarError("Error de conexión. ¿Tienes internet?");
            }
        });
    }

    private void irAObservaciones() {
        Intent intent = new Intent(LoginActivity.this, ObservacionesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }
}
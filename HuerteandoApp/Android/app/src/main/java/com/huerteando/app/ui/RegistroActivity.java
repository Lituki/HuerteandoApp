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
import com.huerteando.app.api.SupabaseClient;
import com.huerteando.app.api.SupabaseService;
import com.huerteando.app.clases.RegistroRequest;
import com.huerteando.app.clases.SupabaseSignUpRequest;
import com.huerteando.app.clases.SupabaseSignUpResponse;
import com.huerteando.app.clases.Usuario;
import com.huerteando.app.utils.ErrorUtils;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegistroActivity extends AppCompatActivity {

    private TextInputEditText editNick;
    private TextInputEditText editPassword;
    private TextInputEditText editConfirmarPassword;
    private TextInputEditText editNombre;
    private TextInputEditText editApellidos;
    private TextInputEditText editEmail;
    private MaterialButton btnRegistro;
    private TextView tvError;
    private TextView tvIrALogin;
    private final String avatarUriString = "default_avatar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        editNick = findViewById(R.id.editNick);
        editPassword = findViewById(R.id.editPassword);
        editConfirmarPassword = findViewById(R.id.editConfirmarPassword);
        editNombre = findViewById(R.id.editNombre);
        editApellidos = findViewById(R.id.editApellidos);
        editEmail = findViewById(R.id.editEmail);
        btnRegistro = findViewById(R.id.btnRegistro);
        tvError = findViewById(R.id.tvError);
        tvIrALogin = findViewById(R.id.tvIrALogin);

        tvIrALogin.setOnClickListener(v -> finish());

        btnRegistro.setOnClickListener(v -> realizarRegistro());
    }

    private void realizarRegistro() {
        String nick              = getText(editNick);
        String password          = getText(editPassword);
        String confirmarPassword = getText(editConfirmarPassword);
        String nombre            = getText(editNombre);
        String apellidos         = getText(editApellidos);
        String email             = getText(editEmail);

        if (nick.isEmpty() || password.isEmpty() || nombre.isEmpty() || email.isEmpty()) {
            mostrarError("Por favor, completa los campos obligatorios");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarError("Por favor, introduce un correo electrónico válido (ejemplo@correo.com)");
            return;
        }

        if (!password.equals(confirmarPassword)) {
            mostrarError("Las contraseñas no coinciden");
            return;
        }

        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$")) {
            mostrarError("La contraseña debe tener al menos 8 letras, 1 mayúscula, 1 minúscula y 1 símbolo");
            return;
        }

        tvError.setVisibility(View.GONE);
        setBtnCargando(true);

        comprobarDisponibilidad(nick, password, nombre, apellidos, email);
    }

    private void comprobarDisponibilidad(String nick, String password,
                                         String nombre, String apellidos, String email) {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.comprobarDisponibilidad(nick, email).enqueue(new Callback<Map<String, Boolean>>() {
            @Override
            public void onResponse(Call<Map<String, Boolean>> call,
                                   Response<Map<String, Boolean>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    setBtnCargando(false);
                    mostrarError("No se pudo comprobar la disponibilidad del usuario");
                    return;
                }

                boolean nickDisponible  = Boolean.TRUE.equals(response.body().get("nickDisponible"));
                boolean emailDisponible = Boolean.TRUE.equals(response.body().get("emailDisponible"));

                if (!nickDisponible) {
                    setBtnCargando(false);
                    mostrarError("El nombre de usuario ya está en uso");
                    return;
                }

                if (!emailDisponible) {
                    setBtnCargando(false);
                    mostrarError("El correo ya existe en Huerteando");
                    return;
                }

                registrarEnSupabase(nick, password, nombre, apellidos, email);
            }

            @Override
            public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
                setBtnCargando(false);
                mostrarError("No se ha podido conectar con el servidor. Revisa tu internet.");
            }
        });
    }

    private void registrarEnSupabase(String nick, String password,
                                     String nombre, String apellidos, String email) {
        SupabaseService supabase = SupabaseClient.getClient().create(SupabaseService.class);
        SupabaseSignUpRequest request = new SupabaseSignUpRequest(email, password);

        supabase.signUp(request).enqueue(new Callback<SupabaseSignUpResponse>() {
            @Override
            public void onResponse(Call<SupabaseSignUpResponse> call,
                                   Response<SupabaseSignUpResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    setBtnCargando(false);
                    mostrarError("No se pudo crear la cuenta. Inténtalo de nuevo.");
                    return;
                }
                crearPerfilEnBackend(nick, nombre, apellidos, email, avatarUriString);
            }

            @Override
            public void onFailure(Call<SupabaseSignUpResponse> call, Throwable t) {
                setBtnCargando(false);
                mostrarError("No se ha podido conectar con el servidor. Revisa tu internet.");
            }
        });
    }

    private void crearPerfilEnBackend(String nick, String nombre,
                                      String apellidos, String email, String avatarUrl) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        RegistroRequest request = new RegistroRequest(nick, "SUPABASE_MANAGED", nombre, apellidos, email, avatarUrl);

        apiService.registrar(request).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                setBtnCargando(false);

                if (response.isSuccessful()) {
                    Toast.makeText(RegistroActivity.this,
                            "¡Bienvenido/a! Ya puedes iniciar sesión 🌿",
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    mostrarError(ErrorUtils.getMensajeError(response.code()));
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                setBtnCargando(false);
                mostrarError("No se ha podido conectar con el servidor. Revisa tu internet.");
            }
        });
    }

    private String getText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void setBtnCargando(boolean cargando) {
        btnRegistro.setEnabled(!cargando);
        btnRegistro.setText(cargando ? "Registrando..." : "Registrarse");
    }

    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
    }
}
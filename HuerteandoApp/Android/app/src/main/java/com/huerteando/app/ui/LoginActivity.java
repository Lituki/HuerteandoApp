package com.huerteando.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.huerteando.app.utils.ErrorUtils;
import com.huerteando.app.utils.SessionManager;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;

/**
 * Login con flujo:
 * 1. Supabase Auth
 * 2. Backend JWT
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private MaterialButton    btnLogin;
    private TextView          tvError;
    private TextView          tvIrARegistro;
    private SessionManager sessionManager;
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        httpClient = new OkHttpClient();

        if (sessionManager.haySesion()) {
            irAObservaciones();
            return;
        }

        editEmail     = findViewById(R.id.editEmail);
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
        String email = texto(editEmail);
        String password = texto(editPassword);

        Log.d(TAG, "Intentando iniciar sesión para el usuario: " + email);

        if (email.isEmpty()) {
            mostrarError("Introduce el email");
            return;
        }
        if (password.isEmpty()) {
            mostrarError("Introduce la contraseña");
            return;
        }

        tvError.setVisibility(View.GONE);
        btnLogin.setEnabled(false);
        btnLogin.setText("Accediendo...");

        loginSupabase(email, password);
    }
    /**
     * 1. LOGIN EN SUPABASE (OkHttp)
     */
    private void loginSupabase(String email, String password) {

        String url = getString(R.string.supabase_url_login);

        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("email", email);
            bodyJson.put("password", password);
        } catch (Exception e) {
            mostrarError("Error interno");
            return;
        }

        RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", getString(R.string.supabase_api_key))
                .addHeader("Content-Type", "application/json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");
                    mostrarError("Error de conexión con Supabase");
                });
                Log.e(TAG, "Error Supabase login", e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Entrar");
                        mostrarError("Login incorrecto");
                    });
                    return;
                }

                try {
                    String json = response.body().string();
                    JSONObject obj = new JSONObject(json);

                    String jwt = obj.getString("access_token");

                    Log.d(TAG, "Login Supabase OK (JWT recibido)");

                    // ⚠️ NO LOGS del token
                    sessionManager.guardarToken(jwt);

                    runOnUiThread(() -> llamarBackend());
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Entrar");
                        mostrarError("Error procesando respuesta");
                    });
                    Log.e(TAG, "Parse error Supabase", e);
                }
            }
        });
    }

    /**
     * 2. LLAMADA AL BACKEND CON JWT
     */
    private void llamarBackend() {

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.loginJwt().enqueue(new retrofit2.Callback<Map<String, Object>>() {

            @Override
            public void onResponse(retrofit2.Call<Map<String, Object>> call,
                                   retrofit2.Response<Map<String, Object>> response) {

                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");

                if (response.isSuccessful() && response.body() != null) {

                    Map<String, Object> datos = response.body();

                    Long idUsuario = ((Double) datos.get("id")).longValue();
                    String nickUsuario = (String) datos.get("nick");
                    String nombre = (String) datos.get("nombre");
                    String apellidos = (String) datos.get("apellidos");
                    String email = (String) datos.get("email");
                    String fecha = (String) datos.get("fechaRegistro");
                    String rol = (String) datos.get("rol");
                    String avatarUrl = (String) datos.get("avatarUrl");

                    sessionManager.guardarDatosCompletos(
                            idUsuario,
                            nickUsuario,
                            nombre,
                            apellidos,
                            email,
                            fecha,
                            rol,
                            avatarUrl
                    );

                    irAObservaciones();

                } else {
                    mostrarError("Error de perfil");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Map<String, Object>> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");
                mostrarError("Sin conexión con el backend");
            }
        });
    }

    private void irAObservaciones() {
        Intent intent = new Intent(this, ObservacionesActivity.class);
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
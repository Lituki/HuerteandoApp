package com.huerteando.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.huerteando.app.R;
import com.huerteando.app.utils.SessionManager;

import java.util.Locale;

/**
 * Pantalla de perfil del usuario.
 * Muestra los datos de la sesión actual y permite cerrar sesión.
 */
public class PerfilActivity extends AppCompatActivity {

    private TextView tvNombre, tvNick, tvRol;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        tvNick = findViewById(R.id.tvPerfilNick);
        tvRol = findViewById(R.id.tvPerfilRol);
        MaterialButton btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // Cargar datos de la sesión
        cargarDatosUsuario();

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

    private void cargarDatosUsuario() {
        if (session.haySesion()) {
            tvNombre.setText(session.getNombre());
            // Usamos String.format para evitar advertencias de concatenación
            tvNick.setText(String.format(Locale.getDefault(), "@%s", session.getNick()));
            tvRol.setText(session.getRol());
        }
    }
}

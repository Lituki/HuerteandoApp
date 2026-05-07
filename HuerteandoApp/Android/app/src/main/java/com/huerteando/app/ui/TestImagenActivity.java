package com.huerteando.app.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.huerteando.app.R;

public class TestImagenActivity extends AppCompatActivity {

    private static final String TAG = "TestImagen";

    private ImageView ivTestPreview;
    private MaterialButton btnTestSeleccionar;
    private MaterialButton btnTestSubir;
    private TextView tvTestStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_imagen);

        Log.d(TAG, "TestImagenActivity iniciada");

        enlazarVistas();
        configurarListeners();
    }

    private void enlazarVistas() {
        ivTestPreview = findViewById(R.id.ivTestPreview);
        btnTestSeleccionar = findViewById(R.id.btnTestSeleccionar);
        btnTestSubir = findViewById(R.id.btnTestSubir);
        tvTestStatus = findViewById(R.id.tvTestStatus);
    }

    private void configurarListeners() {
        btnTestSeleccionar.setOnClickListener(v -> {
            Log.d(TAG, "Click en Seleccionar Imagen (Fase 1 completada)");
            actualizarEstado("Botón seleccionar pulsado");
        });

        btnTestSubir.setOnClickListener(v -> {
            Log.d(TAG, "Click en Subir Imagen (Esperando Fase 4)");
            actualizarEstado("Botón subir pulsado");
        });
    }

    private void actualizarEstado(String mensaje) {
        if (tvTestStatus != null) {
            tvTestStatus.setText("Estado: " + mensaje);
        }
    }
}

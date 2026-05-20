package com.huerteando.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.huerteando.app.R;

public class SplashActivity extends AppCompatActivity {

    private static final long TIEMPO_SPLASH = 1500; // 1.5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Esperamos un poco para mostrar el logo y despues abrimos la pantalla principal.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, ObservacionesActivity.class);
            startActivity(intent);
            finish();
        }, TIEMPO_SPLASH);
    }
}
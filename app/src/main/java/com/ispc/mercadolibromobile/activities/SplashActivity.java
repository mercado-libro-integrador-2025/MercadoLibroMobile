package com.ispc.mercadolibromobile.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.utils.SessionUtils;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 4000; // Duración total del splash en milisegundos
    private static final int IMMERSIVE_ANIM_START_DELAY = 3500; // Inicio de la animación inmersiva

    private MediaPlayer mediaPlayer;
    private ImageView splashLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d(TAG, "Iniciando SplashActivity con animación y sonido");

        splashLogo = findViewById(R.id.splash_logo);

        // Configurar y empezar sonido
        mediaPlayer = MediaPlayer.create(this, R.raw.harpsound);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Reproducción de sonido completada.");
                // Opcional: liberar aquí si no se necesita más o en onDestroy
            });
        } else {
            Log.e(TAG, "Error al crear MediaPlayer para el sonido del splash.");
        }

        // Animación inicial (aparición y crecimiento)
        Animation initialAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_initial_animation);
        splashLogo.startAnimation(initialAnimation);

        // Handler para la animación inmersiva y la transición
        new Handler().postDelayed(() -> {
            Log.d(TAG, "Iniciando animación de zoom inmersivo.");
            Animation immersiveAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.splash_zoom_immersive);
            splashLogo.startAnimation(immersiveAnimation);
        }, IMMERSIVE_ANIM_START_DELAY);


        // Handler para la transición después de SPLASH_DURATION
        new Handler().postDelayed(this::navigateToNextActivity, SPLASH_DURATION);
    }

    private void navigateToNextActivity() {
        // Detener y liberar MediaPlayer si aún está activo
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "MediaPlayer liberado.");
        }

        String authToken = SessionUtils.getAuthToken(this);
        Intent intent;

        if (authToken != null) {
            Log.d(TAG, "Token de autenticación encontrado. Redirigiendo a MainActivity.");
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            Log.d(TAG, "No se encontró token de autenticación. Redirigiendo a LoginActivity.");
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Asegurarse de que el MediaPlayer se libera si la actividad se destruye prematuramente
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "MediaPlayer liberado en onDestroy.");
        }
    }
}

package com.ispc.mercadolibromobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.ispc.mercadolibromobile.utils.SessionUtils;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Iniciando SplashActivity");

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
}

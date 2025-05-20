package com.example.mercadolibromobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mercadolibromobile.R;

public class Politicas extends AppCompatActivity {
    private boolean isCalidadExpanded = false;
    private boolean isGarantiaExpanded = false;
    private boolean isSeguridadExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_politicas);

        // Calidad del Producto
        TextView titleCalidad = findViewById(R.id.titleCalidad);
        TextView contentCalidad = findViewById(R.id.contentCalidad);
        titleCalidad.setOnClickListener(v -> {
            isCalidadExpanded = !isCalidadExpanded;
            contentCalidad.setVisibility(isCalidadExpanded ? View.VISIBLE : View.GONE);
        });

        // Garantía de Satisfacción
        TextView titleGarantia = findViewById(R.id.titleGarantia);
        TextView contentGarantia = findViewById(R.id.contentGarantia);
        titleGarantia.setOnClickListener(v -> {
            isGarantiaExpanded = !isGarantiaExpanded;
            contentGarantia.setVisibility(isGarantiaExpanded ? View.VISIBLE : View.GONE);
        });

        // Seguridad en las Transacciones
        TextView titleSeguridad = findViewById(R.id.titleSeguridad);
        TextView contentSeguridad = findViewById(R.id.contentSeguridad);
        titleSeguridad.setOnClickListener(v -> {
            isSeguridadExpanded = !isSeguridadExpanded;
            contentSeguridad.setVisibility(isSeguridadExpanded ? View.VISIBLE : View.GONE);
        });


        // Privacidad de los Datos
        TextView titlePrivacidad = findViewById(R.id.titlePrivacidad);
        TextView contentPrivacidad = findViewById(R.id.contentPrivacidad);
        titlePrivacidad.setOnClickListener(v -> {
            boolean isPrivacidadExpanded = contentPrivacidad.getVisibility() == View.VISIBLE;
            contentPrivacidad.setVisibility(isPrivacidadExpanded ? View.GONE : View.VISIBLE);
        });

// Envío Seguro y Confiable
        TextView titleEnvio = findViewById(R.id.titleEnvio);
        TextView contentEnvio = findViewById(R.id.contentEnvio);
        titleEnvio.setOnClickListener(v -> {
            boolean isEnvioExpanded = contentEnvio.getVisibility() == View.VISIBLE;
            contentEnvio.setVisibility(isEnvioExpanded ? View.GONE : View.VISIBLE);
        });

// Atención al Cliente
        TextView titleAtencion = findViewById(R.id.titleAtencion);
        TextView contentAtencion = findViewById(R.id.contentAtencion);
        titleAtencion.setOnClickListener(v -> {
            boolean isAtencionExpanded = contentAtencion.getVisibility() == View.VISIBLE;
            contentAtencion.setVisibility(isAtencionExpanded ? View.GONE : View.VISIBLE);
        });

    }
}

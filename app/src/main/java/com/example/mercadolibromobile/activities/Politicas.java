package com.example.mercadolibromobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mercadolibromobile.R;

public class Politicas extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_politicas);

        setupExpandableText(findViewById(R.id.titleCalidad), findViewById(R.id.contentCalidad));
        setupExpandableText(findViewById(R.id.titleGarantia), findViewById(R.id.contentGarantia));
        setupExpandableText(findViewById(R.id.titleSeguridad), findViewById(R.id.contentSeguridad));
        setupExpandableText(findViewById(R.id.titlePrivacidad), findViewById(R.id.contentPrivacidad));
        setupExpandableText(findViewById(R.id.titleEnvio), findViewById(R.id.contentEnvio));
        setupExpandableText(findViewById(R.id.titleAtencion), findViewById(R.id.contentAtencion));
    }

    private void setupExpandableText(TextView title, TextView content) {
        title.setOnClickListener(v -> {
            boolean isExpanded = content.getVisibility() == View.VISIBLE;
            content.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        });
    }
}

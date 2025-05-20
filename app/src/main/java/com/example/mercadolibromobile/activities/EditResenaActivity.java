package com.example.mercadolibromobile.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.api.ApiService;
import com.example.mercadolibromobile.models.Resena;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditResenaActivity extends AppCompatActivity {

    private EditText comentarioEditText;
    private Button saveButton;
    private Button volverButton; // Añadido para el botón Volver
    private Resena resena; // La reseña que se va a editar
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_resena);

        comentarioEditText = findViewById(R.id.comentarioEditText);
        saveButton = findViewById(R.id.saveButton);
        volverButton = findViewById(R.id.volverButton); // Inicializar el botón Volver

        // Obtener la reseña del Intent
        resena = (Resena) getIntent().getSerializableExtra("resena");

        if (resena != null) {
            Log.d("EditResenaActivity", "Reseña cargada: " + resena.getComentario());
            comentarioEditText.setText(resena.getComentario());
        } else {
            Log.e("EditResenaActivity", "No se pudo cargar la reseña del Intent");
            Toast.makeText(this, "Error al cargar la reseña. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar Retrofit y apiService
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-mercado-libro-mobile.onrender.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        apiService = retrofit.create(ApiService.class);

        // Configurar el botón para guardar los cambios
        saveButton.setOnClickListener(v -> {
            String nuevoComentario = comentarioEditText.getText().toString().trim();
            if (!nuevoComentario.isEmpty()) {
                Log.d("EditResenaActivity", "Nuevo comentario ingresado: " + nuevoComentario);
                resena.setComentario(nuevoComentario); // Actualizar el comentario en el objeto Resena
                actualizarResena(resena); // Llamar al método para actualizar en el backend
            } else {
                Toast.makeText(this, "El comentario no puede estar vacío", Toast.LENGTH_SHORT).show();
                Log.e("EditResenaActivity", "Intento de guardar un comentario vacío.");
            }
        });

        // Configurar el botón Volver
        volverButton.setOnClickListener(v -> {
            finish(); // Cierra la actividad y vuelve a la anterior
        });
    }

    private void actualizarResena(Resena resenaActualizada) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("access_token", null);

        if (token != null) {
            String resenaId = String.valueOf(resenaActualizada.getId());
            Log.d("EditResenaActivity", "Actualizando reseña con ID: " + resenaId);
            Log.d("EditResenaActivity", "Datos de reseña a enviar: " + resenaActualizada.toString());

            Call<Resena> call = apiService.updateResena("Bearer " + token, resenaId, resenaActualizada);
            call.enqueue(new Callback<Resena>() {
                @Override
                public void onResponse(Call<Resena> call, Response<Resena> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("EditResenaActivity", "Reseña actualizada: " + response.body().getComentario());
                        Toast.makeText(EditResenaActivity.this, "Reseña actualizada exitosamente.", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("resenaActualizada", response.body());
                        setResult(RESULT_OK, resultIntent); // Indicar que se hizo la edición
                        finish(); // Cerrar la actividad
                    } else {
                        Log.e("EditResenaActivity", "Error en respuesta de actualización: " + response.code() + " - " + response.message());
                        if (response.errorBody() != null) {
                            try {
                                Log.e("EditResenaActivity", "Cuerpo de error: " + response.errorBody().string());
                            } catch (IOException e) {
                                Log.e("EditResenaActivity", "Error al leer el cuerpo de error", e);
                            }
                        }
                        Toast.makeText(EditResenaActivity.this, "Error al actualizar la reseña.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Resena> call, Throwable t) {
                    Log.e("EditResenaActivity", "Error en actualizarResena", t);
                    Toast.makeText(EditResenaActivity.this, "Error al actualizar la reseña. Por favor, intenta nuevamente.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("EditResenaActivity", "No se encontró el token. Inicia sesión nuevamente.");
            Toast.makeText(this, "No se encontró el token. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            // Redirigir al inicio de sesión o finalizar la actividad si el token no está presente
        }
    }
}

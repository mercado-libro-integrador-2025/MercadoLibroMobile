package com.example.mercadolibromobile.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.adapters.ResenaAdapter;
import com.example.mercadolibromobile.api.ApiService;
import com.example.mercadolibromobile.models.Resena;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MisResenasActivity extends AppCompatActivity implements ResenaAdapter.OnResenaInteractionListener {

    private RecyclerView recyclerView;
    private ResenaAdapter adapter;
    private ApiService apiService;
    private static final int REQUEST_CODE_ADD_REVIEW = 1;
    private static final int REQUEST_CODE_EDIT_REVIEW = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_resenas);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar el adaptador con una lista vacía y el listener
        adapter = new ResenaAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Inicializar Retrofit y apiService antes de llamar a getResenas()
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-mercado-libro-mobile.onrender.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        apiService = retrofit.create(ApiService.class);

        // Realizar la llamada a la API
        getResenas();

        // Configurar el botón para abrir la actividad de agregar reseñas
        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MisResenasActivity.this, AddResenasActivity.class);
            Log.d("MisResenasActivity", "Abriendo AddResenasActivity para agregar una nueva reseña.");
            startActivityForResult(intent, REQUEST_CODE_ADD_REVIEW);
        });

        // Configurar el botón "Volver"
        Button volverButton = findViewById(R.id.volverButton);
        volverButton.setOnClickListener(v -> {
            Log.d("MisResenasActivity", "Volviendo a la actividad anterior.");
            finish(); // Cierra esta actividad y vuelve a la anterior
        });
    }

    private void getResenas() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("access_token", null);
        String emailUsuario = sharedPreferences.getString("user_email", null);

        if (token != null && emailUsuario != null) {
            Log.d("MisResenasActivity", "Token: " + token);
            Log.d("MisResenasActivity", "Email del usuario: " + emailUsuario);
            Call<List<Resena>> call = apiService.getResenas("Bearer " + token);
            call.enqueue(new Callback<List<Resena>>() {
                @Override
                public void onResponse(Call<List<Resena>> call, Response<List<Resena>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Resena> allResenas = response.body();
                        List<Resena> userResenas = new ArrayList<>();

                        Log.d("MisResenasActivity", "Total reseñas recuperadas: " + allResenas.size());

                        for (Resena resena : allResenas) {
                            Log.d("MisResenasActivity", "Reseña: " + resena.getComentario() + ", Usuario: " + resena.getEmailUsuario());
                            if (resena.getEmailUsuario() != null && resena.getEmailUsuario().equals(emailUsuario)) {
                                userResenas.add(resena);
                            }
                        }

                        adapter.updateResenas(userResenas);
                        if (userResenas.isEmpty()) {
                            Toast.makeText(MisResenasActivity.this, "No se encontraron reseñas para el usuario.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("MisResenasActivity", "Error en la respuesta de la API: " + response.message());
                        Toast.makeText(MisResenasActivity.this, "No se encontraron reseñas.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Resena>> call, Throwable t) {
                    Log.e("MisResenasActivity", "Error al obtener reseñas", t);
                    Toast.makeText(MisResenasActivity.this, "Error al obtener reseñas.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("MisResenasActivity", "No se encontró el token o el correo del usuario. Inicia sesión nuevamente.");
            Toast.makeText(this, "No se encontró el token o el correo del usuario. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResenaEdit(Resena resena) {
        Intent intent = new Intent(MisResenasActivity.this, EditResenaActivity.class);
        intent.putExtra("resena", resena); // Asegúrate de que 'resena' es serializable
        Log.d("MisResenasActivity", "Editando reseña con ID: " + resena.getId());
        startActivityForResult(intent, REQUEST_CODE_EDIT_REVIEW);
    }

    @Override
    public void onResenaDelete(Resena resena) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("access_token", null);

        if (token != null) {
            String resenaId = String.valueOf(resena.getId());
            Log.d("MisResenasActivity", "Eliminando reseña con ID: " + resenaId);
            Call<Void> call = apiService.deleteResena("Bearer " + token, resenaId);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MisResenasActivity.this, "Reseña eliminada correctamente.", Toast.LENGTH_SHORT).show();
                        getResenas();
                    } else {
                        Log.e("MisResenasActivity", "Error al eliminar la reseña: " + response.message());
                        Toast.makeText(MisResenasActivity.this, "Error al eliminar la reseña.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("MisResenasActivity", "Error al eliminar reseña", t);
                    Toast.makeText(MisResenasActivity.this, "Error al eliminar la reseña.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("MisResenasActivity", "No se encontró el token. Inicia sesión nuevamente.");
            Toast.makeText(this, "No se encontró el token. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_CODE_ADD_REVIEW || requestCode == REQUEST_CODE_EDIT_REVIEW) && resultCode == RESULT_OK) {
            Log.d("MisResenasActivity", "Reseña agregada o editada correctamente. Actualizando reseñas.");
            getResenas();
        }
    }
}

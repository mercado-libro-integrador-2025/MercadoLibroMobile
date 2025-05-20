package com.example.mercadolibromobile.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.api.PedidoApi;
import com.example.mercadolibromobile.api.RetrofitClient;
import com.example.mercadolibromobile.models.Resena;
import com.example.mercadolibromobile.models.Book;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddResenasActivity extends AppCompatActivity {

    private Spinner spinnerOpciones;
    private EditText editTextResena;
    private Button buttonAgregar;
    private String selectedBookTitle;
    private PedidoApi apiService;
    private List<Book> libros; // Lista para almacenar los libros

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresena);

        // Inicializar el API
        apiService = RetrofitClient.getInstance("https://backend-mercado-libro-mobile.onrender.com/").create(PedidoApi.class);

        spinnerOpciones = findViewById(R.id.spinnerOpciones);
        editTextResena = findViewById(R.id.editTextResena);
        buttonAgregar = findViewById(R.id.buttonAgregar);

        // Llenar el spinner con títulos de libros
        obtenerListaDeLibros(); // Llamar al método para obtener libros

        // Manejar selección de spinner
        spinnerOpciones.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBookTitle = libros.get(position).getTitulo(); // Obtener el título del libro seleccionado
                Log.d("SelectedBook", "Libro seleccionado: " + selectedBookTitle); // Log para verificar
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBookTitle = null;
            }
        });

        buttonAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarResena();
            }
        });

        Button volverButton = findViewById(R.id.volverButton);
        volverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void agregarResena() {
        String comentario = editTextResena.getText().toString().trim();

        // Obtener email y token desde SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("access_token", null);
        String emailUsuario = sharedPreferences.getString("user_email", null);

        String fechaCreacion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (TextUtils.isEmpty(comentario) || emailUsuario == null || token == null || selectedBookTitle == null) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buscar el libro seleccionado en la lista de libros
        Book libroSeleccionado = null;
        for (Book libro : libros) {
            if (libro.getTitulo().equals(selectedBookTitle)) {
                libroSeleccionado = libro;
                break;
            }
        }

        if (libroSeleccionado == null) {
            Toast.makeText(this, "Libro no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear una nueva reseña con el ID del libro
        int libroId = libroSeleccionado.getIdLibro(); // Obtener el ID del libro
        Resena nuevaResena = new Resena(libroId, emailUsuario, comentario, fechaCreacion);

        // Realizar la llamada a la API para agregar la reseña
        agregarResenaAPIServer(nuevaResena, token);
    }

    private void agregarResenaAPIServer(Resena resena, String token) {
        Call<Void> call = apiService.addResena("Bearer " + token, resena);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Si la reseña se ha agregado con éxito
                    editTextResena.setText("");
                    Toast.makeText(AddResenasActivity.this, "Reseña agregada con éxito", Toast.LENGTH_SHORT).show();

                    // Establecer el resultado como exitoso y finalizar la actividad
                    setResult(RESULT_OK);
                    finish(); // Finaliza la actividad y regresa a MisResenasActivity
                } else {
                    Toast.makeText(AddResenasActivity.this, "Error al agregar reseña", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddResenasActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void obtenerListaDeLibros() {
        // Llamar a la API para obtener la lista de libros
        apiService.getBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    libros = response.body(); // Guardar la lista de libros

                    // Crear un adapter para el spinner
                    List<String> bookTitles = new ArrayList<>();
                    for (Book book : libros) {
                        bookTitles.add(book.getTitulo()); // Agregar títulos de libros
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddResenasActivity.this,
                            android.R.layout.simple_spinner_item, bookTitles);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerOpciones.setAdapter(adapter);
                } else {
                    Toast.makeText(AddResenasActivity.this, "Error al obtener libros", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Toast.makeText(AddResenasActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

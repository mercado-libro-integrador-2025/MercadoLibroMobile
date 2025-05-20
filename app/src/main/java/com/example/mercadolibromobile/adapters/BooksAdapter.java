package com.example.mercadolibromobile.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.api.CarritoApi;
import com.example.mercadolibromobile.api.RetrofitClient;
import com.example.mercadolibromobile.fragments.SinopsisFragment;
import com.example.mercadolibromobile.models.Book;
import com.example.mercadolibromobile.models.ItemCarrito;
import com.example.mercadolibromobile.utils.AuthUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

    private final List<Book> books;
    private final List<Book> booksListFull;
    private final FragmentActivity activity;
    private static final String BASE_URL = "https://backend-mercado-libro-mobile.onrender.com/api/";
    private static final String TAG = "BooksAdapter";

    public BooksAdapter(List<Book> books, FragmentActivity activity) {
        this.books = books;
        this.activity = activity;
        this.booksListFull = new ArrayList<>(books); // copia de la lista original
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);

        holder.tvBookTitle.setText(book.getTitulo());
        holder.tvBookPrice.setText("Precio: $" + book.getPrecio());
        holder.tvBookStock.setText("En stock: " + book.getStock());

        // Muestra el nombre del autor y la categoría
        holder.tvAuthor.setText("Autor: " + book.getAutor().getNombreAutor());
        holder.tvCategory.setText("Categoría: " + book.getCategoria().getNombreCategoria());

        String portadaUrl = book.getPortada();

        if (portadaUrl.startsWith("image/upload/")) {
            portadaUrl = portadaUrl.replace("image/upload/https://", "https://");
        }

        Log.d("URL Debug", "URL de la imagen: " + portadaUrl);

        Glide.with(holder.itemView.getContext())
                .load(portadaUrl)
                .timeout(10000)
                .into(holder.ivBookCover);


        // Botón para ver la sinopsis
        holder.btnSinopsis.setOnClickListener(v -> {
            SinopsisFragment fragment = SinopsisFragment.newInstance(
                    book.getTitulo(),
                    book.getDescripcion(),
                    book.getPortada()
            );
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Botón para comprar (Agregar al carrito)
        holder.btnComprar.setOnClickListener(v -> {
            String token = getAccessToken();
            Log.d(TAG, "Botón de compra clickeado");
            int userId = AuthUtils.obtenerUsuarioIdDesdeToken(token);

            if (userId != -1) {
                double precio = book.getPrecio();
                ItemCarrito itemCarrito = new ItemCarrito(book.getIdLibro(), userId, 1, precio);
                agregarAlCarrito(itemCarrito);
            } else {
                Toast.makeText(v.getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return books.size();
    }
    public void filter(String text) {
        books.clear();

        if (text.isEmpty()) {
            books.addAll(booksListFull);
        } else {
            text = text.toLowerCase();
            for (Book book : booksListFull) {
                if (book.getTitulo().toLowerCase().contains(text)) {
                    books.add(book);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookTitle, tvBookPrice, tvBookStock, tvAuthor, tvCategory;
        Button btnSinopsis, btnComprar;


        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookPrice = itemView.findViewById(R.id.tvBookPrice);
            tvBookStock = itemView.findViewById(R.id.tvBookStock);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnSinopsis = itemView.findViewById(R.id.btnSinopsis);
            btnComprar = itemView.findViewById(R.id.btnComprar);
        }
    }

    private void agregarAlCarrito(ItemCarrito itemCarrito) {
        String token = getAccessToken();  // Obtiene el token desde SharedPreferences

        if (token == null) {
            Toast.makeText(activity, "Token no encontrado. Por favor, inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Token no encontrado. No se puede agregar al carrito.");
            return;
        }

        CarritoApi carritoApi = RetrofitClient.getInstance(BASE_URL).create(CarritoApi.class);
        Call<ItemCarrito> call = carritoApi.agregarAlCarrito("Bearer " + token, itemCarrito);

        call.enqueue(new Callback<ItemCarrito>() {
            @Override
            public void onResponse(Call<ItemCarrito> call, Response<ItemCarrito> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Libro agregado al carrito exitosamente.");
                    Toast.makeText(activity, "Libro agregado al carrito", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Error al agregar al carrito. Código de respuesta: " + response.code() + " - " + response.message());
                    Toast.makeText(activity, "Error al agregar al carrito. Código: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ItemCarrito> call, Throwable t) {
                Log.e(TAG, "Fallo la conexión: " + t.getMessage());
                Toast.makeText(activity, "Fallo la conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getAccessToken() {
        SharedPreferences prefs = activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("access_token", null);
    }
}

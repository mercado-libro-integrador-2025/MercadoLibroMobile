package com.ispc.mercadolibromobile.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.fragments.ReviewListFragment;
import com.ispc.mercadolibromobile.fragments.SinopsisFragment;
import com.ispc.mercadolibromobile.models.Book;
import com.ispc.mercadolibromobile.models.ItemCarrito;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

    private final List<Book> books;
    private final List<Book> booksListFull;
    private final FragmentActivity activity;
    private static final String TAG = "BooksAdapter";

    public BooksAdapter(List<Book> books, FragmentActivity activity) {
        this.books = books;
        this.activity = activity;
        this.booksListFull = new ArrayList<>(books);
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

        holder.tvAuthor.setText("Autor: " + book.getAutor().getNombreAutor());
        holder.tvCategory.setText("Categoría: " + book.getCategoria().getNombreCategoria());

        String portadaUrl = book.getPortada();

        if (portadaUrl.startsWith("image/upload/https://")) {
            portadaUrl = portadaUrl.replace("image/upload/", "");
        }

        Log.d("URL Debug", "URL de la imagen: " + portadaUrl);

        Glide.with(holder.itemView.getContext())
                .load(portadaUrl)
                .timeout(10000)
                .into(holder.ivBookCover);

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

        holder.btnComprar.setOnClickListener(v -> {
            String token = SessionUtils.getAuthToken(activity);
            int userId = SessionUtils.getUserId(activity);

            Log.d(TAG, "Botón de compra clickeado. Token: " + token + ", UserId: " + userId);

            if (token != null && userId != -1) {
                double precio = book.getPrecio();
                ItemCarrito itemCarrito = new ItemCarrito(book.getIdLibro(), userId, 1, precio);
                agregarAlCarrito(itemCarrito);
            } else {
                Toast.makeText(v.getContext(), "Usuario no autenticado. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnViewReviews.setOnClickListener(v -> {
            if (activity != null) {
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, ReviewListFragment.newInstance(book.getIdLibro(), book.getTitulo()))
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(v.getContext(), activity.getString(R.string.error_show_reviews_fragment), Toast.LENGTH_SHORT).show();
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
        TextView tvBookTitle, tvBookPrice, tvAuthor, tvCategory;
        Button btnSinopsis, btnComprar, btnViewReviews;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookPrice = itemView.findViewById(R.id.tvBookPrice);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnSinopsis = itemView.findViewById(R.id.btnSinopsis);
            btnComprar = itemView.findViewById(R.id.btnComprar);
            btnViewReviews = itemView.findViewById(R.id.btnViewReviews);
        }
    }

    private void agregarAlCarrito(ItemCarrito itemCarrito) {
        String token = SessionUtils.getAuthToken(activity);

        if (token == null) {
            Toast.makeText(activity, "Token no encontrado. Por favor, inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Token no encontrado. No se puede agregar al carrito.");
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(activity);
        Call<ItemCarrito> call = apiService.agregarAlCarrito("Bearer " + token, itemCarrito);

        call.enqueue(new Callback<ItemCarrito>() {
            @Override
            public void onResponse(@NonNull Call<ItemCarrito> call, @NonNull Response<ItemCarrito> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Libro agregado al carrito exitosamente.");
                    Toast.makeText(activity, "Libro agregado al carrito", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Error al agregar al carrito. Código de respuesta: " + response.code() + " - " + response.message());
                    Toast.makeText(activity, "Error al agregar al carrito. Código: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ItemCarrito> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo la conexión: " + t.getMessage());
                Toast.makeText(activity, "Fallo la conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

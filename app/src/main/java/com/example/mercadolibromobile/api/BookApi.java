package com.example.mercadolibromobile.api;

import com.example.mercadolibromobile.models.Book;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface BookApi {
    @GET("libros/")
    Call<List<Book>> getBooks();
}
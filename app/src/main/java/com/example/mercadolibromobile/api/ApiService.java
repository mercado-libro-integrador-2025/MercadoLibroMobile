package com.example.mercadolibromobile.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import com.example.mercadolibromobile.models.AuthModels;
import com.example.mercadolibromobile.models.Book;
import com.example.mercadolibromobile.models.Pedido;
import com.example.mercadolibromobile.models.Resena;
import com.example.mercadolibromobile.models.User;

public interface ApiService {

    @GET("usuarios/")
    Call<List<User>> getUsers();

    @POST("/api/auth/login/")
    Call<User> loginUser(@Body AuthModels.LoginRequest loginRequest);

    @GET("/api/resenas/")
    Call<List<Resena>> getResenas(@Header("Authorization") String token);

    @POST("/api/resenas/")
    Call<Void> addResena(@Header("Authorization") String token, @Body Resena resena);

    @GET("/api/libros/") // Obtiene la lista de libros
    Call<List<Book>> getBooks(); // Cambia esto según tu modelo de libro

    @DELETE("/api/resenas/{id}/")
    Call<Void> deleteResena(@Header("Authorization") String token, @Path("id") String id);

    @PUT("/api/resenas/{id}/") // Asegúrate de incluir la barra al final de la URL
    Call<Resena> updateResena(
            @Header("Authorization") String token,
            @Path("id") String resenaId,
            @Body Resena resena
    );

}

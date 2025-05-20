package com.example.mercadolibromobile.api;

import com.example.mercadolibromobile.models.AuthModels;
import com.example.mercadolibromobile.models.Book;
import com.example.mercadolibromobile.models.Pedido;
import com.example.mercadolibromobile.models.Resena;
import com.example.mercadolibromobile.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PedidoApi {
    @GET("pedidos/")
    Call<List<Pedido>> getPedidos(@Header("Authorization") String authToken);



    // Obtener un pedido específico por ID
    @GET("pedidos/{id}")
    Call<Pedido> getPedidoPorId(@Header("Authorization") String authToken, @Path("id") int id);

    // Crear un nuevo pedido
    @POST("pedidos/")
    Call<Pedido> crearPedido(@Header("Authorization") String authToken, @Body Pedido nuevoPedido);

//no deberian estar aca sino en ApiService
    @POST("/api/resenas/")
    Call<Void> addResena(@Header("Authorization") String token, @Body Resena resena);
    @GET("usuarios/")
    Call<List<User>> getUsers();

    @POST("/api/auth/login/")
    Call<User> loginUser(@Body AuthModels.LoginRequest loginRequest);

    @GET("/api/resenas/")
    Call<List<Resena>> getResenas(@Header("Authorization") String token);

    @GET("/api/libros/") // Agrega esta línea para obtener la lista de libros
    Call<List<Book>> getBooks(); // Cambia esto según tu modelo de libro
// no deberian estar aca sino en ApiService
}

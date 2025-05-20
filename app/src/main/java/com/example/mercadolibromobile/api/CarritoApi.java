package com.example.mercadolibromobile.api;

import com.example.mercadolibromobile.models.ItemCarrito;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.Path;

public interface CarritoApi {
    @POST("carrito/")
    Call<ItemCarrito> agregarAlCarrito(
            @Header("Authorization") String token,
            @Body ItemCarrito itemCarrito
    );

    @GET("carrito/")
    Call<List<ItemCarrito>> obtenerCarrito(@Header("Authorization") String token);

    @DELETE("carrito/{id}/")
    Call<Void> eliminarDelCarrito(@Header("Authorization") String token, @Path("id") int id);

}

package com.example.mercadolibromobile.api;

import com.example.mercadolibromobile.models.User;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface deleteApi {

    // Método GET para obtener los datos del usuario autenticado
    @GET("usuarios/me/")
    Call<User> getAuthenticatedUser(
            @Header("Authorization") String authToken
    );

    // Método DELETE para eliminar al usuario por ID
    @DELETE("usuarios/{id}/")
    Call<Void> deleteUser(
            @Path("id") int id,
            @Header("Authorization") String authToken
    );
}

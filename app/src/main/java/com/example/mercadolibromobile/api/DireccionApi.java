package com.example.mercadolibromobile.api;

import com.example.mercadolibromobile.models.Direccion;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface DireccionApi {
    @GET("direcciones/")
    Call<List<Direccion>> getDirecciones(@Header("Authorization") String token);

    @POST("direcciones/")
    Call<Direccion> createDireccion(@Header("Authorization") String token, @Body Direccion direccion);
}

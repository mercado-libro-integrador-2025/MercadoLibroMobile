package com.example.mercadolibromobile.api;

import com.example.mercadolibromobile.models.Pago;
import com.example.mercadolibromobile.models.Pago;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.GET;

public interface PagoApi {

    // Método para realizar un pago
    @POST("metodopagos/")
    Call<Pago> realizarPago(@Header("Authorization") String token, @Body Pago pago);

    // Método para obtener los métodos de pago disponibles
    @GET("metodopagos/")
    Call<List<Pago>> getMostrarPago(@Header("Authorization") String token);
}

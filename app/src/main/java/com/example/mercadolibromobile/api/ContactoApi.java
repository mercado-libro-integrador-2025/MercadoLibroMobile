package com.example.mercadolibromobile.api;

import com.example.mercadolibromobile.models.Contacto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ContactoApi {
    @POST("contacto/")
    Call<Void> enviarConsulta(@Body Contacto contacto);
}

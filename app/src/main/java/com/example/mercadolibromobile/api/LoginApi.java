package com.example.mercadolibromobile.api;

import com.example.mercadolibromobile.models.AuthModels;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface LoginApi {
    @FormUrlEncoded
    @POST("auth/login/")
    Call<AuthModels.LoginResponse> login(@Field("email") String email, @Field("password") String password);

    @POST("auth/signup/")
    Call<AuthModels.SignupResponse> register(@Body AuthModels.SignupRequest signupRequest);
}
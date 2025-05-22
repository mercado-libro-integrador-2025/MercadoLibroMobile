package com.example.mercadolibromobile.api;

import com.example.mercadolibromobile.models.AuthModels;
import com.example.mercadolibromobile.models.Book;
import com.example.mercadolibromobile.models.Pedido;
import com.example.mercadolibromobile.models.User;
import com.example.mercadolibromobile.models.ItemCarrito;
import com.example.mercadolibromobile.models.Direccion;
import com.example.mercadolibromobile.models.Pago;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // =================== Usuarios ===================
    @GET("usuarios/")
    Call<List<User>> getUsers();

    @GET("usuarios/me/")
    Call<User> getAuthenticatedUser(@Header("Authorization") String token);

    @DELETE("usuarios/{id}/")
    Call<Void> deleteUser(@Path("id") int id, @Header("Authorization") String token);

    // =================== Auth ===================
    @FormUrlEncoded
    @POST("auth/login/")
    Call<AuthModels.LoginResponse> login(@Field("email") String email, @Field("password") String password);

    @POST("auth/signup/")
    Call<AuthModels.SignupResponse> register(@Body AuthModels.SignupRequest signupRequest);

    // =================== Libros ===================
    @GET("libros/")
    Call<List<Book>> getBooks();

    // =================== Reseñas ===================
    /*@GET("resenas/")
    Call<List<Resena>> getResenas(@Header("Authorization") String token);

    @POST("resenas/")
    Call<Void> addResena(@Header("Authorization") String token, @Body Resena resena);

    @DELETE("resenas/{id}/")
    Call<Void> deleteResena(@Header("Authorization") String token, @Path("id") String id);

    @PUT("resenas/{id}/")
    Call<Resena> updateResena(@Header("Authorization") String token, @Path("id") String resenaId, @Body Resena resena);
*/
    // =================== Pedidos ===================
    @GET("pedidos/")
    Call<List<Pedido>> getPedidos(@Header("Authorization") String token);

    @GET("pedidos/{id}/")
    Call<Pedido> getPedidoPorId(@Header("Authorization") String token, @Path("id") int id);

    @POST("pedidos/")
    Call<Pedido> crearPedido(@Header("Authorization") String token, @Body Pedido nuevoPedido);

    // =================== Carrito ===================
    @POST("carrito/")
    Call<ItemCarrito> agregarAlCarrito(@Header("Authorization") String token, @Body ItemCarrito itemCarrito);

    // =================== Direcciones ===================
    @POST("direcciones/")
    Call<Direccion> createDireccion(@Header("Authorization") String token, @Body Direccion nuevaDireccion);

    // =================== Pagos ===================
    @POST("pagos/")
    Call<Pago> realizarPago(@Header("Authorization") String token, @Body Pago pago);
}

package com.ispc.mercadolibromobile.api;

import com.ispc.mercadolibromobile.models.AuthModels;
import com.ispc.mercadolibromobile.models.Book;
import com.ispc.mercadolibromobile.models.Pedido;
//import com.ispc.mercadolibromobile.models.Resena;
import com.ispc.mercadolibromobile.models.User;
import com.ispc.mercadolibromobile.models.ItemCarrito;
import com.ispc.mercadolibromobile.models.Direccion;
import com.ispc.mercadolibromobile.models.Pago;
import com.ispc.mercadolibromobile.models.Contacto;

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
    /* @GET("resenas/")
    Call<List<Resena>> getResenas(@Header("Authorization") String token);

    @POST("resenas/")
    Call<Void> addResena(@Header("Authorization") String token, @Body Resena resena);

    @DELETE("resenas/{id}/")
    Call<Void> deleteResena(@Header("Authorization") String token, @Path("id") String id); // Asegúrate de que el ID de reseña es String

    @PUT("resenas/{id}/")
    Call<Resena> updateResena(@Header("Authorization") String token, @Path("id") String resenaId, @Body Resena resena);*/

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

    @GET("carrito/")
    Call<List<ItemCarrito>> obtenerCarrito(@Header("Authorization") String token);

    @DELETE("carrito/{id}/")
    Call<Void> eliminarDelCarrito(@Header("Authorization") String token, @Path("id") int id);

    // =================== Direcciones ===================
    @GET("direcciones/")
    Call<List<Direccion>> getDirecciones(@Header("Authorization") String token);

    @POST("direcciones/")
    Call<Direccion> createDireccion(@Header("Authorization") String token, @Body Direccion direccion);

    // =================== Pagos ===================
    @POST("pagos/")
    Call<Pago> realizarPago(@Header("Authorization") String token, @Body Pago pago);

    @GET("metodopagos/")
    Call<List<Pago>> getMostrarPago(@Header("Authorization") String token);

    // =================== Contacto ===================
    @POST("contacto/")
    Call<Void> enviarConsulta(@Body Contacto contacto);
}

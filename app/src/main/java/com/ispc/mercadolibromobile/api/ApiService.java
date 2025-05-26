package com.ispc.mercadolibromobile.api;

import com.ispc.mercadolibromobile.models.AuthModels;
import com.ispc.mercadolibromobile.models.Book;
import com.ispc.mercadolibromobile.models.Pedido;
import com.ispc.mercadolibromobile.models.Review;
import com.ispc.mercadolibromobile.models.User;
import com.ispc.mercadolibromobile.models.ItemCarrito;
import com.ispc.mercadolibromobile.models.Direccion;
import com.ispc.mercadolibromobile.models.Pago;
import com.ispc.mercadolibromobile.models.Contacto;
import com.ispc.mercadolibromobile.models.UserInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // =================== Usuarios ===================
    @GET("usuarios/")
    Call<List<UserInfo>> getUsers();

    @GET("usuarios/me/")
    Call<User> getAuthenticatedUser(@Header("Authorization") String token);

    @DELETE("usuarios/{id}/")
    Call<Void> deleteUser(@Path("id") int id, @Header("Authorization") String token);

    @PUT("usuarios/{id}/") //
    Call<UserInfo> updateUser(@Path("id") int userId, @Header("Authorization") String authToken, @Body UserInfo userInfo);

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
    @GET("resenas/")
    Call<List<Review>> getAllReviews(@Header("Authorization") String token); // Renombrado para claridad

    @GET("resenas/libro/{idLibro}/")
    Call<List<Review>> getReviewsForBook(@Path("idLibro") int idLibro);

    @GET("resenas/usuario/{idUsuario}/")
    Call<List<Review>> getReviewsByUser(@Header("Authorization") String token, @Path("idUsuario") int idUsuario);

    @POST("resenas/")
    Call<Review> createReview(@Header("Authorization") String token, @Body Review review); // CORRECCIÓN: Retorna Review

    @DELETE("resenas/{id}/")
    Call<Void> deleteReview(@Header("Authorization") String token, @Path("id") int id); // CORRECCIÓN: id como int

    @PUT("resenas/{id}/")
    Call<Review> updateReview(@Header("Authorization") String token, @Path("id") int reviewId, @Body Review review); // CORRECCIÓN: id como int, retorna Review

    @GET("resenas/{id}/")
    Call<Review> getReviewById(@Header("Authorization") String token, @Path("id") int reviewId); // Nuevo: para editar

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

    @PUT("direcciones/{id}/") // Nuevo: para actualizar una dirección existente
    Call<Direccion> updateDireccion(@Path("id") int id, @Header("Authorization") String token, @Body Direccion direccion);

    @DELETE("direcciones/{id}/") // Nuevo: para eliminar una dirección
    Call<Void> deleteDireccion(@Path("id") int id, @Header("Authorization") String token);

    // =================== Pagos ===================
    @POST("pagos/")
    Call<Pago> realizarPago(@Header("Authorization") String token, @Body Pago pago);

    @GET("metodopagos/")
    Call<List<Pago>> getMostrarPago(@Header("Authorization") String token);

    // =================== Contacto ===================
    @POST("contacto/")
    Call<Void> enviarConsulta(@Body Contacto contacto);

}

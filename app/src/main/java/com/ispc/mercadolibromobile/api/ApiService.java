package com.ispc.mercadolibromobile.api;

import com.ispc.mercadolibromobile.dtos.MercadoPagoPreferenceRequest;
import com.ispc.mercadolibromobile.dtos.MercadoPagoPreferenceResponse;
import com.ispc.mercadolibromobile.models.AuthModels;
import com.ispc.mercadolibromobile.models.Book;
import com.ispc.mercadolibromobile.dtos.ItemCarritoUpdateDto;
import com.ispc.mercadolibromobile.models.Pedido;
import com.ispc.mercadolibromobile.models.Review;
import com.ispc.mercadolibromobile.models.User;
import com.ispc.mercadolibromobile.models.ItemCarrito;
import com.ispc.mercadolibromobile.models.Direccion;
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
import retrofit2.http.PATCH;
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
    Call<List<Review>> getAllReviews(@Header("Authorization") String token);
    @GET("resenas/libro/{idLibro}/")
    Call<List<Review>> getReviewsForBook(@Path("idLibro") int idLibro);

    @GET("resenas/mis-resenas/")
    Call<List<Review>> getMyReviews(@Header("Authorization") String token);
    @POST("resenas/")
    Call<Review> createReview(@Header("Authorization") String token, @Body Review review);

    @DELETE("resenas/{id}/")
    Call<Void> deleteReview(@Header("Authorization") String token, @Path("id") int id);

    @PUT("resenas/{id}/")
    Call<Review> updateReview(@Header("Authorization") String token, @Path("id") int reviewId, @Body Review review);
    @GET("resenas/{id}/")
    Call<Review> getReviewById(@Header("Authorization") String token, @Path("id") int reviewId);

    @GET("libros/{id}/")
    Call<Book> getBookById(@Path("id")int bookId);

    // =================== Carrito ===================
    @GET("carrito/")
    Call<List<ItemCarrito>> obtenerCarrito(@Header("Authorization") String authToken);

    @DELETE("carrito/{id}/")
    Call<Void> eliminarDelCarrito(@Header("Authorization") String authToken, @Path("id") int itemId);
    @PATCH("carrito/{id}/")
    Call<ItemCarrito> actualizarItemCarrito(@Header("Authorization") String authToken, @Path("id") int itemId, @Body ItemCarritoUpdateDto itemCarritoUpdatedTo);

    @POST("carrito/")
    Call<ItemCarrito> agregarAlCarrito(@Header("Authorization") String authToken, @Body ItemCarrito itemCarrito);

    // =================== Direcciones ===================
    @GET("direcciones/")
    Call<List<Direccion>> getDirecciones(@Header("Authorization") String token);

    @POST("direcciones/")
    Call<Direccion> createDireccion(@Header("Authorization") String token, @Body Direccion direccion);

    @PUT("direcciones/{id}/")
    Call<Direccion> updateDireccion(@Path("id") int id, @Header("Authorization") String token, @Body Direccion direccion);

    @DELETE("direcciones/{id}/")
    Call<Void> deleteDireccion(@Path("id") int id, @Header("Authorization") String token);


    // =================== Pagos y Pedidos ===================
    @POST("checkout/crear-preferencia/")
    Call<MercadoPagoPreferenceResponse> crearPreferenciaMercadoPago(@Header("Authorization") String authToken, @Body MercadoPagoPreferenceRequest request);
    @GET("pedidos/")
    Call<List<Pedido>> getPedidos(@Header("Authorization") String token);
    @GET("pedidos/{id}/")
    Call<Pedido> getPedidoPorId(@Header("Authorization") String token, @Path("id") int id);

    // =================== Contacto ===================
    @POST("contacto/")
    Call<Void> enviarConsulta(@Body Contacto contacto);
    @GET("contacto/")
    Call<List<Contacto>> obtenerConsultas();
}
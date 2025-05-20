package com.example.mercadolibromobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.adapters.CarritoAdapter;
import com.example.mercadolibromobile.api.CarritoApi;
import com.example.mercadolibromobile.models.ItemCarrito;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CarritoFragment extends Fragment implements CarritoAdapter.CarritoListener {

    private RecyclerView recyclerViewCarrito;
    private TextView precioTotal;
    private Button btnFinalizarCompra;
    private List<ItemCarrito> itemsCarrito;
    private final String API_URL = "https://backend-mercado-libro-mobile.onrender.com/api/carrito/";
    private CarritoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carrito, container, false);

        recyclerViewCarrito = view.findViewById(R.id.recyclerViewCarrito);
        precioTotal = view.findViewById(R.id.precioTotal);
        btnFinalizarCompra = view.findViewById(R.id.btnFinalizarCompra);

        itemsCarrito = new ArrayList<>();
        adapter = new CarritoAdapter(itemsCarrito, getContext(), this);

        recyclerViewCarrito.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCarrito.setAdapter(adapter);

        obtenerDatosCarrito(adapter);

        btnFinalizarCompra.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new DireccionFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    private void obtenerDatosCarrito(CarritoAdapter adapter) {
        OkHttpClient client = new OkHttpClient();
        String token = getAccessToken();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Type listType = new TypeToken<List<ItemCarrito>>() {}.getType();
                    List<ItemCarrito> nuevosItems = new Gson().fromJson(responseData, listType);

                    requireActivity().runOnUiThread(() -> {
                        itemsCarrito.clear();
                        itemsCarrito.addAll(nuevosItems);
                        adapter.notifyDataSetChanged();
                        actualizarPrecioTotal();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        // Handle error (optional)
                    });
                }
            }
        });
    }

    private String getAccessToken() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("access_token", null);
    }

    private void actualizarPrecioTotal() {
        double total = 0.0;
        for (ItemCarrito item : itemsCarrito) {
            total += item.getTotal();
        }
        precioTotal.setText("Total: $" + total);
    }

    @Override
    public void aumentarCantidad(ItemCarrito item) {
        item.aumentarCantidad();
        adapter.notifyDataSetChanged();
        actualizarPrecioTotal();
    }

    @Override
    public void disminuirCantidad(ItemCarrito item) {
        if (item.getCantidad() > 1) {
            item.disminuirCantidad();
            adapter.notifyDataSetChanged();
            actualizarPrecioTotal();
        }
    }

    @Override
    public void eliminarItem(ItemCarrito item) {
        String token = getAccessToken();

        CarritoApi carritoApi = new Retrofit.Builder()
                .baseUrl("https://backend-mercado-libro-mobile.onrender.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CarritoApi.class);

        retrofit2.Call<Void> call = carritoApi.eliminarDelCarrito("Bearer " + token, item.getId());
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    itemsCarrito.remove(item);
                    adapter.notifyDataSetChanged();
                    actualizarPrecioTotal();
                } else {
                    System.out.println("Error al eliminar el item del carrito, código: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Void> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

}

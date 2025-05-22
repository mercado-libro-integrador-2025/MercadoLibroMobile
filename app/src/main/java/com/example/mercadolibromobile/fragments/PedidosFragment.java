package com.example.mercadolibromobile.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.adapters.PedidoAdapter;
import com.example.mercadolibromobile.api.ApiService; // Usar ApiService
import com.example.mercadolibromobile.api.RetrofitClient; // Usar RetrofitClient
import com.example.mercadolibromobile.models.Pedido;
import com.example.mercadolibromobile.utils.SessionUtils; // Importar SessionUtils

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedidosFragment extends Fragment {
    private RecyclerView recyclerView;
    private ApiService apiService;
    private String authToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("PedidosFragment", "PedidosFragment cargado correctamente");
        View rootView = inflater.inflate(R.layout.fragment_pedidos, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewPedidos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        authToken = SessionUtils.getAuthToken(getContext());

        apiService = RetrofitClient.getApiService(getContext());

        if (authToken != null) {
            cargarPedidos("Bearer " + authToken);
        } else {
            Toast.makeText(getContext(), "Error de autenticación. Por favor, inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            Log.e("PedidosFragment", "Token de autenticación no encontrado.");
        }

        return rootView;
    }
    private void cargarPedidos(String authToken) {
        Call<List<Pedido>> call = apiService.getPedidos(authToken);
        call.enqueue(new Callback<List<Pedido>>() {
            @Override
            public void onResponse(Call<List<Pedido>> call, Response<List<Pedido>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Pedido> pedidos = response.body();
                    Log.d("PedidosFragment", "Cantidad de pedidos: " + pedidos.size());
                    PedidoAdapter pedidoAdapter = new PedidoAdapter(pedidos);
                    recyclerView.setAdapter(pedidoAdapter);
                } else {
                    Log.d("PedidosFragment", "Respuesta no exitosa o sin datos: " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Error al cargar los pedidos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Pedido>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("PedidosFragment", "Fallo la conexión al cargar pedidos: ", t);
            }
        });
    }
}

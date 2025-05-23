package com.example.mercadolibromobile.fragments;

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
import com.example.mercadolibromobile.api.ApiService;
import com.example.mercadolibromobile.api.RetrofitClient;
import com.example.mercadolibromobile.models.Pedido;
import com.example.mercadolibromobile.utils.SessionUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedidosFragment extends Fragment {
    private RecyclerView recyclerView;
    private ApiService apiService;
    private String authToken;

    private static final String TAG = "PedidosFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "PedidosFragment cargado correctamente");
        View rootView = inflater.inflate(R.layout.fragment_pedidos, container, false);

        // Referencia al RecyclerView
        recyclerView = rootView.findViewById(R.id.recyclerViewPedidos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Obtener el token de autenticación desde SessionUtils
        authToken = SessionUtils.getAuthToken(requireContext());

        // Inicializar ApiService
        apiService = RetrofitClient.getApiService(requireContext());

        if (authToken != null) {
            cargarPedidos("Bearer " + authToken);
        } else {
            // Mostrar mensaje de error si no hay token
            Toast.makeText(requireContext(), getString(R.string.error_auth_required), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Token de autenticación no encontrado.");
        }

        return rootView;
    }
    private void cargarPedidos(@NonNull String authToken) {
        // Realiza la llamada a la API para obtener los pedidos
        Call<List<Pedido>> call = apiService.getPedidos(authToken);
        call.enqueue(new Callback<List<Pedido>>() {
            @Override
            public void onResponse(@NonNull Call<List<Pedido>> call, @NonNull Response<List<Pedido>> response) {
                // Asegurarse de que el fragmento está adjunto antes de actualizar la UI
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        List<Pedido> pedidos = response.body();
                        Log.d(TAG, "Cantidad de pedidos: " + pedidos.size());
                        PedidoAdapter pedidoAdapter = new PedidoAdapter(pedidos);
                        recyclerView.setAdapter(pedidoAdapter); // Configura el adaptador con los pedidos
                    } else {
                        Log.d(TAG, "Respuesta no exitosa o sin datos: " + response.code() + " - " + response.message());
                        Toast.makeText(requireContext(), getString(R.string.error_loading_orders, response.message()), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Pedido>> call, @NonNull Throwable t) {
                // Asegurarse de que el fragmento está adjunto antes de mostrar Toast
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_orders, t.getMessage()), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Fallo la conexión al cargar pedidos: " + t.getMessage(), t);
                }
            }
        });
    }
}

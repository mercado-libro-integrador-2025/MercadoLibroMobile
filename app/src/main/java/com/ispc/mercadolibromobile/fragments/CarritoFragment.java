package com.ispc.mercadolibromobile.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.adapters.CarritoAdapter;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.models.ItemCarrito;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarritoFragment extends Fragment implements CarritoAdapter.CarritoListener {

    private RecyclerView recyclerViewCarrito;
    private TextView precioTotal;
    private Button btnFinalizarCompra;
    private List<ItemCarrito> itemsCarrito;
    private CarritoAdapter adapter;
    private ApiService apiService;

    private static final String TAG = "CarritoFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carrito, container, false);

        // Inicializar vistas
        recyclerViewCarrito = view.findViewById(R.id.recyclerViewCarrito);
        precioTotal = view.findViewById(R.id.precioTotal);
        btnFinalizarCompra = view.findViewById(R.id.btnFinalizarCompra);

        // Inicializar la lista de ítems del carrito y el adaptador
        itemsCarrito = new ArrayList<>();
        adapter = new CarritoAdapter(itemsCarrito, getContext(), this);

        // Configurar el RecyclerView
        recyclerViewCarrito.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCarrito.setAdapter(adapter);

        // Obtener la instancia de ApiService
        apiService = RetrofitClient.getApiService(getContext());

        // Cargar los datos del carrito desde la API
        obtenerDatosCarrito();

        // Configurar el botón para finalizar la compra
        btnFinalizarCompra.setOnClickListener(v -> {
            // Navegar al fragmento de Dirección
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new DireccionFragment());
            transaction.addToBackStack(null); // Permite volver al fragmento anterior
            transaction.commit();
        });

        return view;
    }
    private void obtenerDatosCarrito() {
        String token = SessionUtils.getAuthToken(getContext());

        if (token == null) {
            Toast.makeText(getContext(), "No hay sesión activa. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Token de autenticación no encontrado.");
            return;
        }

        // Realizar la llamada a la API usando ApiService
        Call<List<ItemCarrito>> call = apiService.obtenerCarrito("Bearer " + token);
        call.enqueue(new Callback<List<ItemCarrito>>() {
            @Override
            public void onResponse(@NonNull Call<List<ItemCarrito>> call, @NonNull Response<List<ItemCarrito>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ItemCarrito> nuevosItems = response.body();
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            itemsCarrito.clear();
                            itemsCarrito.addAll(nuevosItems);
                            adapter.notifyDataSetChanged();
                            actualizarPrecioTotal();
                        });
                    }
                } else {
                    Log.e(TAG, "Error al obtener carrito. Código: " + response.code() + ", Mensaje: " + response.message());
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al cargar el carrito: " + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ItemCarrito>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo en la conexión al obtener carrito: " + t.getMessage(), t);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error de conexión al cargar el carrito.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void actualizarPrecioTotal() {
        double total = 0.0;
        for (ItemCarrito item : itemsCarrito) {
            total += item.getTotal();
        }
        precioTotal.setText(getString(R.string.total_price_format, String.format("%.2f", total)));
    }

    // =================== Implementación de CarritoAdapter.CarritoListener ===================

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
        } else {
            Toast.makeText(getContext(), "La cantidad mínima es 1.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void eliminarItem(ItemCarrito item) {
        String token = SessionUtils.getAuthToken(getContext());
        if (token == null) {
            Toast.makeText(getContext(), "No hay sesión activa. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Token de autenticación no encontrado para eliminar item.");
            return;
        }

        Call<Void> call = apiService.eliminarDelCarrito("Bearer " + token, item.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    itemsCarrito.remove(item);
                    adapter.notifyDataSetChanged();
                    actualizarPrecioTotal();
                    Toast.makeText(getContext(), "Producto eliminado del carrito.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Item eliminado del carrito exitosamente.");
                } else {
                    Log.e(TAG, "Error al eliminar el item del carrito. Código: " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Error al eliminar del carrito: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo la conexión al eliminar item: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error de conexión al eliminar del carrito.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

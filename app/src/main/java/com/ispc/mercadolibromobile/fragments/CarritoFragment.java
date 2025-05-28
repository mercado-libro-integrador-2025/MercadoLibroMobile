package com.ispc.mercadolibromobile.fragments;

import android.content.Context;
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
import com.ispc.mercadolibromobile.models.ItemCarritoUpdatedTo;
import com.ispc.mercadolibromobile.models.Direccion;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarritoFragment extends Fragment implements CarritoAdapter.CarritoListener {

    private RecyclerView recyclerViewCarrito;
    private TextView tvTotalFinal;
    private Button btnFinalizarCompra;
    private List<ItemCarrito> itemsCarrito;
    private CarritoAdapter adapter;
    private ApiService apiService;
    private CartUpdateListener cartUpdateListener;

    private TextView tvDireccionCompleta;
    private Button btnCambiarDireccion;
    private Direccion direccionSeleccionada;

    public static final String KEY_SELECTED_ADDRESS = "selected_address_from_list";
    private static final String TAG = "CarritoFragment";

    public interface CartUpdateListener {
        void onCartUpdated();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CartUpdateListener) {
            cartUpdateListener = (CartUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement CartUpdateListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cartUpdateListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carrito, container, false);

        recyclerViewCarrito = view.findViewById(R.id.recyclerViewCarrito);
        tvTotalFinal = view.findViewById(R.id.tvTotalFinal);
        btnFinalizarCompra = view.findViewById(R.id.btnFinalizarCompra);

        tvDireccionCompleta = view.findViewById(R.id.tvDireccionCompleta);
        btnCambiarDireccion = view.findViewById(R.id.btnCambiarDireccion);


        itemsCarrito = new ArrayList<>();
        adapter = new CarritoAdapter(itemsCarrito, getContext(), this);

        recyclerViewCarrito.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCarrito.setAdapter(adapter);

        apiService = RetrofitClient.getApiService(getContext());

        obtenerDatosCarrito();

        getParentFragmentManager().setFragmentResultListener("requestKeyAddressSelection", this, (requestKey, bundle) -> {
            Direccion selectedAddress = (Direccion) bundle.getSerializable(KEY_SELECTED_ADDRESS);
            if (selectedAddress != null) {
                direccionSeleccionada = selectedAddress;
                tvDireccionCompleta.setText(direccionSeleccionada.getFullAddress());
                Log.d(TAG, "Dirección seleccionada recibida de DireccionListFragment: " + direccionSeleccionada.getFullAddress());
            }
        });


        cargarDireccionInicial();

        btnCambiarDireccion.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean(DireccionListFragment.IS_SELECTION_MODE, true);
            DireccionListFragment direccionSelectionFragment = new DireccionListFragment();
            direccionSelectionFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, direccionSelectionFragment)
                    .addToBackStack(null)
                    .commit();
        });


        btnFinalizarCompra.setOnClickListener(v -> {
            if (itemsCarrito.isEmpty()) {
                Toast.makeText(getContext(), "Tu carrito está vacío. Agrega productos para finalizar la compra.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (direccionSeleccionada == null) {
                Toast.makeText(getContext(), "Por favor, selecciona una dirección de envío antes de continuar.", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle args = new Bundle();
            args.putSerializable("selected_direccion", direccionSeleccionada);
            PagoFragment pagoFragment = new PagoFragment();
            pagoFragment.setArguments(args);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, pagoFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
    private void cargarDireccionInicial() {
        String token = SessionUtils.getAuthToken(getContext());
        if (token == null) {
            tvDireccionCompleta.setText("Por favor, inicia sesión para gestionar direcciones.");
            btnCambiarDireccion.setText("Iniciar Sesión");
            return;
        }

        apiService.getDirecciones("Bearer " + token).enqueue(new Callback<List<Direccion>>() {
            @Override
            public void onResponse(@NonNull Call<List<Direccion>> call, @NonNull Response<List<Direccion>> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        direccionSeleccionada = response.body().get(0);
                        tvDireccionCompleta.setText(direccionSeleccionada.getFullAddress());
                        btnCambiarDireccion.setText("Cambiar dirección");
                        Log.d(TAG, "Dirección inicial cargada: " + direccionSeleccionada.getFullAddress());
                    } else {
                        tvDireccionCompleta.setText("No hay dirección de envío seleccionada. Por favor, agrega una.");
                        btnCambiarDireccion.setText("Agregar dirección");
                        direccionSeleccionada = null; // Asegurarse de que no haya ninguna dirección seleccionada
                        Log.d(TAG, "No se encontraron direcciones para el usuario.");
                    }
                    checkFinalizarCompraButtonState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Direccion>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "Error al cargar direcciones iniciales: " + t.getMessage());
                    tvDireccionCompleta.setText("Error al cargar direcciones. Intenta de nuevo.");
                    btnCambiarDireccion.setText("Reintentar");
                    checkFinalizarCompraButtonState();
                }
            }
        });
    }

    private void checkFinalizarCompraButtonState() {
        btnFinalizarCompra.setEnabled(!itemsCarrito.isEmpty() && direccionSeleccionada != null);
    }


    private void obtenerDatosCarrito() {
        String token = SessionUtils.getAuthToken(getContext());
        if (token == null) {
            Toast.makeText(getContext(), "No hay sesión activa. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Token de autenticación no encontrado para obtener carrito.");
            return;
        }
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
                            if (cartUpdateListener != null) {
                                cartUpdateListener.onCartUpdated();
                            }
                            checkFinalizarCompraButtonState();
                        });
                    }
                } else {
                    checkFinalizarCompraButtonState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ItemCarrito>> call, @NonNull Throwable t) {
                checkFinalizarCompraButtonState();
            }
        });
    }


    private void actualizarPrecioTotal() {
        double total = 0.0;
        for (ItemCarrito item : itemsCarrito) {
            total += item.getTotal();
        }
        tvTotalFinal.setText(getString(R.string.total_price_format, String.format("%.2f", total)));

        checkFinalizarCompraButtonState();
    }

    // =================== Implementación de CarritoAdapter.CarritoListener ===================
    // Estos métodos son llamados desde el adaptador cuando el usuario interactúa con los botones +/-

    @Override
    public void aumentarCantidad(ItemCarrito item) {
        int nuevaCantidad = item.getCantidad() + 1;
        actualizarCantidadEnServidor(item.getId(), nuevaCantidad);
    }

    @Override
    public void disminuirCantidad(ItemCarrito item) {
        if (item.getCantidad() > 1) {
            int nuevaCantidad = item.getCantidad() - 1;
            actualizarCantidadEnServidor(item.getId(), nuevaCantidad);
        } else {
            Toast.makeText(getContext(), "La cantidad mínima es 1.", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarCantidadEnServidor(int itemId, int nuevaCantidad) {
        String token = SessionUtils.getAuthToken(getContext());
        if (token == null) {
            Toast.makeText(getContext(), "No hay sesión activa. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Token de autenticación no encontrado para actualizar item.");
            return;
        }

        ItemCarritoUpdatedTo itemUpdateDto = new ItemCarritoUpdatedTo(nuevaCantidad);

        Call<ItemCarrito> call = apiService.actualizarItemCarrito("Bearer " + token, itemId, itemUpdateDto);
        call.enqueue(new Callback<ItemCarrito>() {
            @Override
            public void onResponse(@NonNull Call<ItemCarrito> call, @NonNull Response<ItemCarrito> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Cantidad actualizada.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Cantidad actualizada en el carrito exitosamente.");
                    obtenerDatosCarrito();
                    if (cartUpdateListener != null) {
                         cartUpdateListener.onCartUpdated();
                     }
                } else {
                    Log.e(TAG, "Error al actualizar la cantidad del item. Código: " + response.code() + " - " + response.message());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "Error body: " + errorBody);
                        Toast.makeText(getContext(), "Error al actualizar la cantidad: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                        Toast.makeText(getContext(), "Error al actualizar la cantidad.", Toast.LENGTH_SHORT).show();
                    }
                    obtenerDatosCarrito();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ItemCarrito> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo la conexión al actualizar cantidad: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error de conexión al actualizar la cantidad.", Toast.LENGTH_SHORT).show();
                obtenerDatosCarrito();
            }
        });
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
                    Toast.makeText(getContext(), "Producto eliminado del carrito.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Item eliminado del carrito exitosamente.");
                    obtenerDatosCarrito();
                } else {
                    Log.e(TAG, "Error al eliminar el item del carrito. Código: " + response.code() + " - " + response.message());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "Error body eliminar carrito: " + errorBody);
                        Toast.makeText(getContext(), "Error al eliminar del carrito: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error al parsear error body eliminar carrito", e);
                        Toast.makeText(getContext(), "Error al eliminar del carrito.", Toast.LENGTH_SHORT).show();
                    }
                    obtenerDatosCarrito();
                }
            }

            @Override
            public void onFailure (@NonNull Call < Void > call, @NonNull Throwable t){
                Log.e(TAG, "Fallo la conexión al eliminar item: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error de conexión al eliminar del carrito.", Toast.LENGTH_SHORT).show();
                obtenerDatosCarrito();
            }
        });
    }
}
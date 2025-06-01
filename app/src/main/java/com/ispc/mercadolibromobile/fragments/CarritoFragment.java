package com.ispc.mercadolibromobile.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.adapters.CarritoAdapter;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.dtos.ItemCarritoUpdateDto;
import com.ispc.mercadolibromobile.dtos.MercadoPagoPreferenceRequest;
import com.ispc.mercadolibromobile.dtos.MercadoPagoPreferenceResponse;
import com.ispc.mercadolibromobile.models.Direccion;
import com.ispc.mercadolibromobile.models.ItemCarrito;
import com.ispc.mercadolibromobile.models.ProductoParaMP;
import com.ispc.mercadolibromobile.utils.SessionUtils;
import com.mercadopago.android.px.core.MercadoPagoCheckout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;

public class CarritoFragment extends Fragment implements CarritoAdapter.CarritoListener {
    public static final String KEY_SELECTED_ADDRESS = "selected_address_from_list";
    private static final String TAG = "CarritoFragment";

    public interface CartUpdateListener {
        void onCartUpdated();
    }

    private TextView tvTotalFinal, tvDireccionCompleta;
    private Button btnFinalizarCompra, btnCambiarDireccion;
    private CarritoAdapter adapter;
    private List<ItemCarrito> itemsCarrito;
    private Direccion direccionSeleccionada;
    private ApiService apiService;
    private CartUpdateListener cartUpdateListener;

    @SuppressLint("NotifyDataSetChanged")
    private final ActivityResultLauncher<Intent> mercadoPagoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (!isAdded()) {
                    Log.w(TAG, "Fragmento no adjunto, no se puede procesar el resultado de Mercado Pago.");
                    return;
                }

                if (result.getResultCode() == Activity.RESULT_OK) {
                    assert result.getData() != null;
                    String transactionResult = result.getData().getStringExtra("transaction_result");
                    Log.d(TAG, "Pago exitoso. Resultado de la transacción: " + transactionResult);
                    Toast.makeText(getContext(), "¡Compra finalizada con éxito! Recibirás una confirmación.", Toast.LENGTH_LONG).show();

                    itemsCarrito.clear();
                    adapter.notifyDataSetChanged();
                    actualizarPrecioTotal();

                    if (cartUpdateListener != null) {
                        cartUpdateListener.onCartUpdated();
                    }

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new PedidoFragment())
                            .addToBackStack(null)
                            .commit();

                } else {
                    Log.e(TAG, "Pago cancelado o con error. Código: " + result.getResultCode());
                    String errorMessage = result.getData() != null ? result.getData().getStringExtra("error_message") : "Pago cancelado sin mensaje de error."; // Podría haber un extra para el error
                    Toast.makeText(getContext(), "Pago cancelado o con error: " + errorMessage, Toast.LENGTH_LONG).show();
                    obtenerDatosCarrito();
                }
            });



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CartUpdateListener) {
            cartUpdateListener = (CartUpdateListener) context;
        } else {
            throw new RuntimeException(context + " must implement CartUpdateListener");
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

        RecyclerView recyclerViewCarrito = view.findViewById(R.id.recyclerViewCarrito);
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
                checkFinalizarCompraButtonState();
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

            iniciarPagoMercadoPago();
        });

        return view;
    }

    private void iniciarPagoMercadoPago() {
        String token = SessionUtils.getAuthToken(getContext());
        if (token == null) {
            Toast.makeText(getContext(), "No hay sesión activa. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ProductoParaMP> productosParaMP = new ArrayList<>();
        for (ItemCarrito item : itemsCarrito) {
            if (item.getLibro() > 0) {
                productosParaMP.add(new ProductoParaMP(item.getLibro(), item.getCantidad()));
            } else {
                Log.e(TAG, "ItemCarrito con ID de libro inválido (<= 0). No se puede crear preferencia de MP.");
                Toast.makeText(getContext(), "Error: Producto sin ID válido. No se puede procesar el pago.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Log.d(TAG, "Productos para MP a enviar: " + productosParaMP.size() + " items.");
        for (ProductoParaMP p : productosParaMP) {
            Log.d(TAG, "  - Libro ID: " + p.getIdLibro() + ", Cantidad: " + p.getCantidad());
        }

        MercadoPagoPreferenceRequest request = new MercadoPagoPreferenceRequest(productosParaMP, direccionSeleccionada.getId());

        apiService.crearPreferenciaMercadoPago("Bearer " + token, request).enqueue(new retrofit2.Callback<MercadoPagoPreferenceResponse>() {
            @Override
            public void onResponse(@NonNull Call<MercadoPagoPreferenceResponse> call, @NonNull retrofit2.Response<MercadoPagoPreferenceResponse> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String preferenceId = response.body().getPreferenceId();
                        Log.d(TAG, "Preferencia de Mercado Pago creada: " + preferenceId);
                        try {
                            MercadoPagoCheckout checkout = new MercadoPagoCheckout.Builder(
                                    "TEST-8172258200747869-051120-3beb4a6a51e00538722eefca692fc36e-128356048",
                                    preferenceId
                            ).build();

                            checkout.startPayment(requireActivity(), 123);

                        } catch (Exception e) {
                            Log.e(TAG, "Error al iniciar Mercado Pago Checkout: " + e.getMessage(), e);
                            Toast.makeText(getContext(), "Error al iniciar el pago. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorBody = "Error desconocido";
                        try {
                            if (response.errorBody() != null) {
                                errorBody = response.errorBody().string();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al leer errorBody en crearPreferenciaMercadoPago", e);
                        }
                        Log.e(TAG, "Error al crear preferencia de MP. Código: " + response.code() + " - Mensaje: " + response.message() + " - Body: " + errorBody);
                        Toast.makeText(getContext(), "Error al preparar el pago: " + errorBody, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MercadoPagoPreferenceResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "Fallo de conexión al crear preferencia de MP: " + t.getMessage(), t);
                    Toast.makeText(getContext(), "Error de conexión. Revisa tu internet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private void cargarDireccionInicial() {
        String token = SessionUtils.getAuthToken(getContext());
        if (token == null) {
            tvDireccionCompleta.setText("Por favor, inicia sesión para gestionar direcciones.");
            btnCambiarDireccion.setText("Iniciar Sesión");
            return;
        }

        apiService.getDirecciones("Bearer " + token).enqueue(new retrofit2.Callback<List<Direccion>>() {
            @Override
            public void onResponse(@NonNull Call<List<Direccion>> call, @NonNull retrofit2.Response<List<Direccion>> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        direccionSeleccionada = response.body().get(0);
                        tvDireccionCompleta.setText(direccionSeleccionada.getFullAddress());
                        btnCambiarDireccion.setText("Cambiar dirección");
                        Log.d(TAG, "Dirección inicial cargada: " + direccionSeleccionada.getFullAddress());
                    } else {
                        tvDireccionCompleta.setText("No hay dirección de envío seleccionada. Por favor, agrega una.");
                        btnCambiarDireccion.setText("Agregar dirección");
                        direccionSeleccionada = null;
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
        call.enqueue(new retrofit2.Callback<List<ItemCarrito>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<ItemCarrito>> call, @NonNull retrofit2.Response<List<ItemCarrito>> response) {
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
                    Log.e(TAG, "Error al obtener datos del carrito. Código: " + response.code() + " - " + response.message());
                    checkFinalizarCompraButtonState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ItemCarrito>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo de conexión al obtener carrito: " + t.getMessage(), t);
                checkFinalizarCompraButtonState();
            }
        });
    }

    private void actualizarPrecioTotal() {
        double total = 0.0;
        for (ItemCarrito item : itemsCarrito) {
            total += item.getTotal();
        }
        tvTotalFinal.setText(getString(R.string.total_price_format, String.format(Locale.getDefault(), "%.2f", total)));

        checkFinalizarCompraButtonState();
    }

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

        ItemCarritoUpdateDto itemUpdateDto = new ItemCarritoUpdateDto(nuevaCantidad);

        apiService.actualizarItemCarrito("Bearer " + token, itemId, itemUpdateDto).enqueue(new retrofit2.Callback<ItemCarrito>() {
            @Override
            public void onResponse(@NonNull Call<ItemCarrito> call, @NonNull retrofit2.Response<ItemCarrito> response) {
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
                        if (errorBody.contains("stock insuficiente")) {
                            Toast.makeText(getContext(), "Stock insuficiente para la cantidad solicitada.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Error al actualizar la cantidad: " + errorBody, Toast.LENGTH_LONG).show();
                        }
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

        apiService.eliminarDelCarrito("Bearer " + token, item.getId()).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {
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
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo la conexión al eliminar item: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error de conexión al eliminar del carrito.", Toast.LENGTH_SHORT).show();
                obtenerDatosCarrito();
            }
        });
    }
}
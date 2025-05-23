package com.ispc.mercadolibromobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.models.Direccion;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DireccionFragment extends Fragment {

    private static final String TAG = "DireccionFragment";

    private EditText etCalle, etNumero, etCiudad, etProvincia;
    private TextView tvCalleIngresada, tvNumeroIngresado, tvCiudadIngresada, tvProvinciaIngresada;
    private Button btnIrAlPago;
    private SharedPreferences appPrefs;
    private boolean direccionGuardada = false;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_direccion, container, false);

        // Inicializar vistas
        etCalle = view.findViewById(R.id.etCalle);
        etNumero = view.findViewById(R.id.etNumero);
        etCiudad = view.findViewById(R.id.etCiudad);
        etProvincia = view.findViewById(R.id.etProvincia);

        tvCalleIngresada = view.findViewById(R.id.tvCalleIngresada);
        tvNumeroIngresado = view.findViewById(R.id.tvNumeroIngresado);
        tvCiudadIngresada = view.findViewById(R.id.tvCiudadIngresada);
        tvProvinciaIngresada = view.findViewById(R.id.tvProvinciaIngresada);

        btnIrAlPago = view.findViewById(R.id.btnIrAlPago);

        appPrefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        apiService = RetrofitClient.getApiService(getContext());

        // Configurar el botón para guardar la dirección o ir al pago
        btnIrAlPago.setOnClickListener(v -> {
            if (!direccionGuardada) {
                guardarDireccion();
            } else {
                irAPagoFragment();
            }
        });

        // Cargar las direcciones al iniciar el fragmento
        cargarDirecciones();

        return view;
    }
    private void guardarDireccion() {
        // Obtener el token y el ID de usuario usando SessionUtils
        String token = SessionUtils.getAuthToken(getContext());
        int userId = SessionUtils.getUserId(getContext());

        Log.d(TAG, "Guardando dirección con token: " + token + ", UserId: " + userId);

        // Validar campos de entrada
        String calle = etCalle.getText().toString().trim();
        String numero = etNumero.getText().toString().trim();
        String ciudad = etCiudad.getText().toString().trim();
        String provincia = etProvincia.getText().toString().trim();

        if (calle.isEmpty()) {
            etCalle.setError(getString(R.string.error_street_required));
            etCalle.requestFocus();
            return;
        }
        if (numero.isEmpty()) {
            etNumero.setError(getString(R.string.error_number_required));
            etNumero.requestFocus();
            return;
        }
        if (ciudad.isEmpty()) {
            etCiudad.setError(getString(R.string.error_city_required));
            etCiudad.requestFocus();
            return;
        }
        if (provincia.isEmpty()) {
            etProvincia.setError(getString(R.string.error_province_required));
            etProvincia.requestFocus();
            return;
        }

        if (token != null && userId != -1) {
            Direccion nuevaDireccion = new Direccion(
                    0,
                    userId,
                    calle,
                    numero,
                    ciudad,
                    provincia
            );

            // Realizar el POST para guardar la dirección
            apiService.createDireccion("Bearer " + token, nuevaDireccion).enqueue(new Callback<Direccion>() {
                @Override
                public void onResponse(@NonNull Call<Direccion> call, @NonNull Response<Direccion> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Asegurarse de que el fragmento está adjunto antes de actualizar la UI
                        if (isAdded()) {
                            guardarDireccionEnSharedPreferences(response.body());
                            Toast.makeText(getContext(), getString(R.string.success_address_saved), Toast.LENGTH_SHORT).show();
                            direccionGuardada = true;
                            btnIrAlPago.setText(getString(R.string.button_go_to_payment));
                            cargarDirecciones(); // Llama al GET después de guardar para actualizar la UI
                        }
                    } else {
                        Log.e(TAG, "Error al guardar dirección. Código: " + response.code() + ", Mensaje: " + response.message());
                        try {
                            String errorMessage = response.errorBody() != null ? response.errorBody().string() : getString(R.string.error_unknown);
                            if (isAdded()) {
                                Toast.makeText(getContext(), getString(R.string.error_saving_address, errorMessage), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar el cuerpo de error al guardar dirección", e);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Direccion> call, @NonNull Throwable t) {
                    Log.e(TAG, "Fallo al guardar dirección: " + t.getMessage(), t);
                    if (isAdded()) {
                        Toast.makeText(getContext(), getString(R.string.error_network_connection_address), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), getString(R.string.error_token_not_found), Toast.LENGTH_SHORT).show();
        }
    }
    private void guardarDireccionEnSharedPreferences(Direccion direccion) {
        SharedPreferences.Editor editor = appPrefs.edit();
        editor.putString("direccion_calle", direccion.getCalle());
        editor.putString("direccion_numero", direccion.getNumero());
        editor.putString("direccion_ciudad", direccion.getCiudad());
        editor.putString("direccion_provincia", direccion.getProvincia());
        editor.apply();
    }
    private void cargarDirecciones() {
        // Obtener la dirección guardada de SharedPreferences de la aplicación
        String calle = appPrefs.getString("direccion_calle", getString(R.string.no_address_saved));
        String numero = appPrefs.getString("direccion_numero", getString(R.string.no_address_saved));
        String ciudad = appPrefs.getString("direccion_ciudad", getString(R.string.no_address_saved));
        String provincia = appPrefs.getString("direccion_provincia", getString(R.string.no_address_saved));

        // Mostrar los datos en los TextViews
        tvCalleIngresada.setText(getString(R.string.label_street_entered, calle));
        tvNumeroIngresado.setText(getString(R.string.label_number_entered, numero));
        tvCiudadIngresada.setText(getString(R.string.label_city_entered, ciudad));
        tvProvinciaIngresada.setText(getString(R.string.label_province_entered, provincia));

        Log.d(TAG, "Dirección cargada desde SharedPreferences: Calle=" + calle + ", Número=" + numero + ", Ciudad=" + ciudad + ", Provincia=" + provincia);

        // Actualizar el estado de direccionGuardada si hay una dirección guardada
        if (!getString(R.string.no_address_saved).equals(calle)) {
            direccionGuardada = true;
            btnIrAlPago.setText(getString(R.string.button_go_to_payment));
        } else {
            direccionGuardada = false;
            btnIrAlPago.setText(getString(R.string.button_save_data));
        }
    }
    private void irAPagoFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new PagoFragment());
        transaction.addToBackStack(null);  // Agregar a la pila de retroceso
        transaction.commit();
    }
}

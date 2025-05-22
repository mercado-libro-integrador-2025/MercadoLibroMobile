package com.example.mercadolibromobile.fragments;

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

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.api.ApiService; // Usar ApiService
import com.example.mercadolibromobile.api.RetrofitClient;
import com.example.mercadolibromobile.models.Direccion;
import com.example.mercadolibromobile.utils.SessionUtils; // Importar SessionUtils

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_direccion, container, false);

        etCalle = view.findViewById(R.id.etCalle);
        etNumero = view.findViewById(R.id.etNumero);
        etCiudad = view.findViewById(R.id.etCiudad);
        etProvincia = view.findViewById(R.id.etProvincia);

        tvCalleIngresada = view.findViewById(R.id.tvCalleIngresada);
        tvNumeroIngresado = view.findViewById(R.id.tvNumeroIngresado);
        tvCiudadIngresada = view.findViewById(R.id.tvCiudadIngresada);
        tvProvinciaIngresada = view.findViewById(R.id.tvProvinciaIngresada);

        btnIrAlPago = view.findViewById(R.id.btnIrAlPago);

        appPrefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        btnIrAlPago.setOnClickListener(v -> {
            if (!direccionGuardada) {
                guardarDireccion();
            } else {
                irAPagoFragment();
            }
        });

        cargarDirecciones();

        return view;
    }

    private void guardarDireccion() {
        String token = SessionUtils.getAuthToken(getContext());
        Log.d(TAG, "Guardando dirección con token: " + token);

        if (token != null) {
            ApiService apiService = RetrofitClient.getApiService(getContext());

            Direccion nuevaDireccion = new Direccion(
                    0,
                    0,
                    etCalle.getText().toString(),
                    etNumero.getText().toString(),
                    etCiudad.getText().toString(),
                    etProvincia.getText().toString()
            );

            apiService.createDireccion("Bearer " + token, nuevaDireccion).enqueue(new Callback<Direccion>() {
                @Override
                public void onResponse(Call<Direccion> call, Response<Direccion> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        guardarDireccionEnSharedPreferences(response.body());

                        Toast.makeText(getActivity(), "Dirección guardada exitosamente", Toast.LENGTH_SHORT).show();
                        direccionGuardada = true;
                        btnIrAlPago.setText("Ir al Pago");
                        cargarDirecciones();
                    } else {
                        try {
                            String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                            Toast.makeText(getActivity(), "Error al guardar dirección: " + errorMessage, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error al guardar dirección: " + errorMessage);
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar el cuerpo de error al guardar dirección", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Direccion> call, Throwable t) {
                    Toast.makeText(getActivity(), "Error de conexión al guardar dirección", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Fallo al guardar dirección: ", t);
                }
            });
        } else {
            Toast.makeText(getActivity(), "Token no encontrado, por favor inicie sesión.", Toast.LENGTH_SHORT).show();
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
        String calle = appPrefs.getString("direccion_calle", "No hay dirección guardada");
        String numero = appPrefs.getString("direccion_numero", "No hay dirección guardada");
        String ciudad = appPrefs.getString("direccion_ciudad", "No hay dirección guardada");
        String provincia = appPrefs.getString("direccion_provincia", "No hay dirección guardada");

        tvCalleIngresada.setText("Calle ingresada: " + calle);
        tvNumeroIngresado.setText("Número ingresado: " + numero);
        tvCiudadIngresada.setText("Ciudad ingresada: " + ciudad);
        tvProvinciaIngresada.setText("Provincia ingresada: " + provincia);

        Log.d(TAG, "Dirección cargada desde SharedPreferences: Calle=" + calle + ", Número=" + numero + ", Ciudad=" + ciudad + ", Provincia=" + provincia);

        if (!"No hay dirección guardada".equals(calle)) {
            direccionGuardada = true;
            btnIrAlPago.setText("Ir al Pago");
        } else {
            direccionGuardada = false;
            btnIrAlPago.setText("Guardar Dirección");
        }
    }

    private void irAPagoFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new PagoFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}

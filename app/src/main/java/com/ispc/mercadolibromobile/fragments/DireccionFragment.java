package com.ispc.mercadolibromobile.fragments;

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
    private static final String ARG_DIRECCION = "direccion_object";

    private EditText etCalle, etNumero, etCiudad, etProvincia;
    private TextView tvIngresarDireccionTitle;
    private Button btnSaveOrContinue;

    private Direccion currentDireccion;
    private ApiService apiService;

    public static DireccionFragment newInstance(@Nullable Direccion direccion) {
        DireccionFragment fragment = new DireccionFragment();
        Bundle args = new Bundle();
        if (direccion != null) {
            args.putSerializable(ARG_DIRECCION, direccion);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentDireccion = (Direccion) getArguments().getSerializable(ARG_DIRECCION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_direccion, container, false);

        tvIngresarDireccionTitle = view.findViewById(R.id.tvIngresarDireccion);
        etCalle = view.findViewById(R.id.etCalle);
        etNumero = view.findViewById(R.id.etNumero);
        etCiudad = view.findViewById(R.id.etCiudad);
        etProvincia = view.findViewById(R.id.etProvincia);
        btnSaveOrContinue = view.findViewById(R.id.btnIrAlPago);

        apiService = RetrofitClient.getApiService(requireContext());

        if (currentDireccion != null) {
            tvIngresarDireccionTitle.setText(getString(R.string.title_edit_address));
            etCalle.setText(currentDireccion.getCalle());
            etNumero.setText(currentDireccion.getNumero());
            etCiudad.setText(currentDireccion.getCiudad());
            etProvincia.setText(currentDireccion.getProvincia());
            btnSaveOrContinue.setText(getString(R.string.button_update_address));
        } else {
            tvIngresarDireccionTitle.setText(getString(R.string.title_add_new_address));
            btnSaveOrContinue.setText(getString(R.string.button_save_address));
        }

        btnSaveOrContinue.setOnClickListener(v -> saveOrUpdateDireccion());
        return view;
    }

    private void saveOrUpdateDireccion() {
        String token = SessionUtils.getAuthToken(requireContext());
        int userId = SessionUtils.getUserId(requireContext());

        if (token == null || userId == -1) {
            Toast.makeText(requireContext(), getString(R.string.error_token_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

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

        Direccion direccionToSave = new Direccion(
                currentDireccion != null ? currentDireccion.getId() : 0,
                userId,
                calle,
                numero,
                ciudad,
                provincia
        );

        if (currentDireccion != null) {
            apiService.updateDireccion(direccionToSave.getId(), "Bearer " + token, direccionToSave).enqueue(new Callback<Direccion>() {
                @Override
                public void onResponse(@NonNull Call<Direccion> call, @NonNull Response<Direccion> response) {
                    if (isAdded()) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(), getString(R.string.success_address_updated), Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        } else {
                            Log.e(TAG, "Error al actualizar dirección. Código: " + response.code() + ", Mensaje: " + response.message());
                            Toast.makeText(requireContext(), getString(R.string.error_updating_address), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Direccion> call, @NonNull Throwable t) {
                    if (isAdded()) {
                        Log.e(TAG, "Fallo de red al actualizar dirección: " + t.getMessage(), t);
                        Toast.makeText(requireContext(), getString(R.string.error_network_connection_address), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            apiService.createDireccion("Bearer " + token, direccionToSave).enqueue(new Callback<Direccion>() {
                @Override
                public void onResponse(@NonNull Call<Direccion> call, @NonNull Response<Direccion> response) {
                    if (isAdded()) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(), getString(R.string.success_address_saved), Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        } else {
                            Log.e(TAG, "Error al guardar dirección. Código: " + response.code() + ", Mensaje: " + response.message());
                            Toast.makeText(requireContext(), getString(R.string.error_saving_address, response.message()), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Direccion> call, @NonNull Throwable t) {
                    if (isAdded()) {
                        Log.e(TAG, "Fallo de red al guardar dirección: " + t.getMessage(), t);
                        Toast.makeText(requireContext(), getString(R.string.error_network_connection_address), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
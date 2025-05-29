package com.ispc.mercadolibromobile.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; 

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.databinding.FragmentContactBinding;
import com.ispc.mercadolibromobile.models.Contacto;

import java.util.Objects;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactFragment extends Fragment {
    private static final String TAG = ContactFragment.class.getSimpleName();

    private ApiService apiService;
    private FragmentContactBinding binding;

    private String loggedInUserName = "Nombre del Usuario";
    private String loggedInUserEmail = "usuario.logueado@ejemplo.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater, container, false);

        apiService = RetrofitClient.getApiService(getContext());

        binding.btnEnviarConsulta.setOnClickListener(v -> validarYEnviarConsulta());

        binding.btnEnviarConsulta.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));

        agregarTextWatchers();

        return binding.getRoot();
    }

    private void agregarTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (binding.etAsunto.getText().toString().trim().isEmpty()) {
                    binding.tilAsunto.setError(null);
                }
                if (binding.etConsulta.getText().toString().trim().isEmpty()) {
                    binding.tilConsulta.setError(null);
                }

                actualizarEstadoBotonEnviar();
            }
        };

        binding.etAsunto.addTextChangedListener(textWatcher);
        binding.etConsulta.addTextChangedListener(textWatcher);
    }

    private void validarYEnviarConsulta() {
        String nombre = loggedInUserName;
        String email = loggedInUserEmail;

        String asunto = binding.etAsunto.getText().toString().trim();
        String consulta = binding.etConsulta.getText().toString().trim();
        boolean nombreValido = !nombre.isEmpty() && NAME_PATTERN.matcher(nombre).matches() &&
                nombre.length() >= MIN_LENGTH && nombre.length() <= MAX_LENGTH;
        if (nombre.isEmpty()) {
            binding.tilNombre.setError(getString(R.string.error_name_required));
        } else if (!NAME_PATTERN.matcher(nombre).matches()) {
            binding.tilNombre.setError(getString(R.string.error_name_invalid_characters));
        } else if (nombre.length() < MIN_LENGTH) {
            binding.tilNombre.setError(getString(R.string.error_min_length, MIN_LENGTH));
        } else if (nombre.length() > MAX_LENGTH) {
            binding.tilNombre.setError(getString(R.string.error_max_length, MAX_LENGTH));
        } else {
            binding.tilNombre.setError(null);
        }

        boolean asuntoValido = !asunto.isEmpty() && asunto.length() >= MIN_LENGTH && asunto.length() <= MAX_LENGTH;
        if (asunto.isEmpty()) {
            binding.tilAsunto.setError(getString(R.string.error_asunto_required));
            binding.etAsunto.requestFocus();
        } else if (consulta.isEmpty()) {
            binding.tilConsulta.setError(getString(R.string.error_query_required));
            binding.etConsulta.requestFocus();
        } else if (consulta.length() < 10) {
            binding.tilConsulta.setError(getString(R.string.error_query_min_length));
            binding.etConsulta.requestFocus();
        } else {
            if (nombre.isEmpty() || email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Log.e(TAG, "Error: Nombre o email del usuario logueado no disponibles o inválidos. No se puede enviar la consulta.");
                if (isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.error_user_id_not_found), Toast.LENGTH_LONG).show();

                }
                return;
            }
            enviarConsulta(new Contacto(nombre, email, asunto, consulta));
        }
    }


    private void enviarConsulta(@NonNull Contacto contacto) {
        Call<Void> call = apiService.enviarConsulta(contacto);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), getString(R.string.success_query_sent), Toast.LENGTH_SHORT).show();
                        actualizarEstadoBotonEnviar(); // Update button state after clearing
                        limpiarCampos();
                    }
                } else {
                    Log.e(TAG, "Error al enviar consulta. Código: " + response.code() + ", Mensaje: " + response.message());
                    if (isAdded()) {
                        Toast.makeText(getContext(), getString(R.string.error_sending_query), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo en la conexión al enviar consulta: " + t.getMessage(), t);
                if (isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.error_network_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void limpiarCampos() {
        binding.etAsunto.setText("");
        binding.etConsulta.setText("");
        binding.tilAsunto.setError(null);
        binding.tilConsulta.setError(null);
    }

}
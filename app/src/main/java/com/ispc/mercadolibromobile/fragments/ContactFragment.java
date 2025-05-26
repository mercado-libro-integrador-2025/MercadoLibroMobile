package com.ispc.mercadolibromobile.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.databinding.FragmentContactBinding;
import com.ispc.mercadolibromobile.models.Contacto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactFragment extends Fragment {
    private static final String TAG = ContactFragment.class.getSimpleName();

    private ApiService apiService;
    private FragmentContactBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater);

        apiService = RetrofitClient.getApiService(getContext());

        binding.btnEnviarConsulta.setOnClickListener(v -> validarYEnviarConsulta());

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
                // Limpiar los errores cuando el usuario empieza a escribir
                if (binding.etNombre.getText().toString().trim().isEmpty()) {
                    binding.etNombre.setError(null);
                }
                if (binding.etAsunto.getText().toString().trim().isEmpty()) {
                    binding.etAsunto.setError(null);
                }
                if (binding.etEmail.getText().toString().trim().isEmpty()) {
                    binding.etEmail.setError(null);
                }
                if (binding.etConsulta.getText().toString().trim().isEmpty()) {
                    binding.etConsulta.setError(null);
                }
            }
        };

        binding.etNombre.addTextChangedListener(textWatcher);
        binding.etAsunto.addTextChangedListener(textWatcher);
        binding.etEmail.addTextChangedListener(textWatcher);
        binding.etConsulta.addTextChangedListener(textWatcher);
    }

    private void validarYEnviarConsulta() {
        String nombre = binding.etNombre.getText().toString().trim();
        String asunto = binding.etAsunto.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String consulta = binding.etConsulta.getText().toString().trim();

        if (asunto.isEmpty()) {
            binding.etAsunto.setError(getString(R.string.error_asunto_required));
            binding.etAsunto.requestFocus();
        } else if (nombre.isEmpty()) {
            binding.etNombre.setError(getString(R.string.error_name_required));
            binding.etNombre.requestFocus();
        } else if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError(getString(R.string.error_email_invalid));
            binding.etEmail.requestFocus();
        } else if (consulta.isEmpty()) {
            binding.etConsulta.setError(getString(R.string.error_query_required));
            binding.etConsulta.requestFocus();
        } else if (consulta.length() < 10) {
            binding.etConsulta.setError(getString(R.string.error_query_min_length));
            binding.etConsulta.requestFocus();
        } else {
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
        binding.etNombre.setText("");
        binding.etAsunto.setText("");
        binding.etEmail.setText("");
        binding.etConsulta.setText("");
        binding.etNombre.setError(null);
        binding.etAsunto.setError(null);
        binding.etEmail.setError(null);
        binding.etConsulta.setError(null);
    }
}
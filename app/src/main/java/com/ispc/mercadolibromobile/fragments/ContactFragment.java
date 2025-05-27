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

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                    + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"
    );
    private static final int MAX_LENGTH = 50; // Maximum length for all fields
    private static final int MIN_LENGTH = 6;  // Minimum length for all fields

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater);

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
                // Clear errors on typing (though validation will re-apply them if needed)
                // This clears error text ONLY if the field becomes empty,
                // otherwise, `actualizarEstadoBotonEnviar` will set appropriate errors.
                if (Objects.requireNonNull(binding.etNombre.getText()).toString().trim().isEmpty()) {
                    binding.tilNombre.setError(null);
                }
                if (Objects.requireNonNull(binding.etAsunto.getText()).toString().trim().isEmpty()) {
                    binding.tilAsunto.setError(null);
                }
                if (Objects.requireNonNull(binding.etEmail.getText()).toString().trim().isEmpty()) {
                    binding.tilEmail.setError(null);
                }
                if (Objects.requireNonNull(binding.etConsulta.getText()).toString().trim().isEmpty()) {
                    binding.tilConsulta.setError(null);
                }

                actualizarEstadoBotonEnviar();
            }
        };

        binding.etNombre.addTextChangedListener(textWatcher);
        binding.etAsunto.addTextChangedListener(textWatcher);
        binding.etEmail.addTextChangedListener(textWatcher);
        binding.etConsulta.addTextChangedListener(textWatcher);
    }

    private void actualizarEstadoBotonEnviar() {
        String nombre = Objects.requireNonNull(binding.etNombre.getText()).toString().trim();
        String asunto = Objects.requireNonNull(binding.etAsunto.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String consulta = Objects.requireNonNull(binding.etConsulta.getText()).toString().trim();

        // --- Real-time validations for enabling/disabling the button and showing/hiding errors ---

        // Name: not empty, letters/spaces only, min 6, max 50 characters
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

        // Subject: not empty, min 6, max 50 characters
        boolean asuntoValido = !asunto.isEmpty() && asunto.length() >= MIN_LENGTH && asunto.length() <= MAX_LENGTH;
        if (asunto.isEmpty()) {
            binding.tilAsunto.setError(getString(R.string.error_asunto_required));
        } else if (asunto.length() < MIN_LENGTH) {
            binding.tilAsunto.setError(getString(R.string.error_min_length, MIN_LENGTH));
        } else if (asunto.length() > MAX_LENGTH) {
            binding.tilAsunto.setError(getString(R.string.error_max_length, MAX_LENGTH));
        } else {
            binding.tilAsunto.setError(null);
        }

        // Email: not empty, valid format, min 6, max 50 characters
        boolean emailValido = !email.isEmpty() && EMAIL_PATTERN.matcher(email).matches() &&
                email.length() >= MIN_LENGTH && email.length() <= MAX_LENGTH;
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_email_required));
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
        } else if (email.length() < MIN_LENGTH) {
            binding.tilEmail.setError(getString(R.string.error_min_length, MIN_LENGTH));
        } else if (email.length() > MAX_LENGTH) {
            binding.tilEmail.setError(getString(R.string.error_max_length, MAX_LENGTH));
        } else {
            binding.tilEmail.setError(null);
        }

        // Query: not empty, min 6, max 50 characters
        boolean consultaValida = !consulta.isEmpty() && consulta.length() >= MIN_LENGTH && consulta.length() <= MAX_LENGTH;
        if (consulta.isEmpty()) {
            binding.tilConsulta.setError(getString(R.string.error_query_required));
        } else if (consulta.length() < MIN_LENGTH) {
            binding.tilConsulta.setError(getString(R.string.error_min_length, MIN_LENGTH));
        } else if (consulta.length() > MAX_LENGTH) {
            binding.tilConsulta.setError(getString(R.string.error_max_length, MAX_LENGTH));
        } else {
            binding.tilConsulta.setError(null);
        }

        // --- Button state update ---
        if (nombreValido && asuntoValido && emailValido && consultaValida) {
            binding.btnEnviarConsulta.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.system_error_900));
        } else {
            binding.btnEnviarConsulta.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        }
    }


    private void validarYEnviarConsulta() {
        // This method performs final validations and shows specific errors
        // if the user tries to submit with invalid fields.
        // The main real-time validation logic is in actualizarEstadoBotonEnviar().

        String nombre = Objects.requireNonNull(binding.etNombre.getText()).toString().trim();
        String asunto = Objects.requireNonNull(binding.etAsunto.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String consulta = Objects.requireNonNull(binding.etConsulta.getText()).toString().trim();

        // Apply all validations for the submission action
        if (nombre.isEmpty()) {
            binding.tilNombre.setError(getString(R.string.error_name_required));
            binding.etNombre.requestFocus();
        } else if (!NAME_PATTERN.matcher(nombre).matches()) {
            binding.tilNombre.setError(getString(R.string.error_name_invalid_characters));
            binding.etNombre.requestFocus();
        } else if (nombre.length() < MIN_LENGTH) {
            binding.tilNombre.setError(getString(R.string.error_min_length, MIN_LENGTH));
            binding.etNombre.requestFocus();
        } else if (nombre.length() > MAX_LENGTH) {
            binding.tilNombre.setError(getString(R.string.error_max_length, MAX_LENGTH));
            binding.etNombre.requestFocus();
        } else if (asunto.isEmpty()) {
            binding.tilAsunto.setError(getString(R.string.error_asunto_required));
            binding.etAsunto.requestFocus();
        } else if (asunto.length() < MIN_LENGTH) {
            binding.tilAsunto.setError(getString(R.string.error_min_length, MIN_LENGTH));
            binding.etAsunto.requestFocus();
        } else if (asunto.length() > MAX_LENGTH) {
            binding.tilAsunto.setError(getString(R.string.error_max_length, MAX_LENGTH));
            binding.etAsunto.requestFocus();
        } else if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_email_required));
            binding.etEmail.requestFocus();
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            binding.etEmail.requestFocus();
        } else if (email.length() < MIN_LENGTH) {
            binding.tilEmail.setError(getString(R.string.error_min_length, MIN_LENGTH));
            binding.etEmail.requestFocus();
        } else if (email.length() > MAX_LENGTH) {
            binding.tilEmail.setError(getString(R.string.error_max_length, MAX_LENGTH));
            binding.etEmail.requestFocus();
        } else if (consulta.isEmpty()) {
            binding.tilConsulta.setError(getString(R.string.error_query_required));
            binding.etConsulta.requestFocus();
        } else if (consulta.length() < MIN_LENGTH) {
            binding.tilConsulta.setError(getString(R.string.error_min_length, MIN_LENGTH));
            binding.etConsulta.requestFocus();
        } else if (consulta.length() > MAX_LENGTH) {
            binding.tilConsulta.setError(getString(R.string.error_max_length, MAX_LENGTH));
            binding.etConsulta.requestFocus();
        } else {
            // If all validations pass, send the query
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
        binding.etNombre.setText("");
        binding.etAsunto.setText("");
        binding.etEmail.setText("");
        binding.etConsulta.setText("");
        binding.tilNombre.setError(null);
        binding.tilAsunto.setError(null);
        binding.tilEmail.setError(null);
        binding.tilConsulta.setError(null);
    }
}
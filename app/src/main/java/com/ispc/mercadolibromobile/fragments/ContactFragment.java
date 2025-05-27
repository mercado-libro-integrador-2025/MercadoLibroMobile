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
import androidx.core.content.ContextCompat;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.databinding.FragmentContactBinding;
import com.ispc.mercadolibromobile.models.Contacto;

import java.util.Objects;
import java.util.regex.Pattern; // Importar Pattern para expresiones regulares

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactFragment extends Fragment {
    private static final String TAG = ContactFragment.class.getSimpleName();

    private ApiService apiService;
    private FragmentContactBinding binding;

    // Regex para validar el nombre (solo letras y espacios)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$");
    private static final int MAX_LENGTH = 50; // Longitud máxima para todos los campos

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater);

        apiService = RetrofitClient.getApiService(getContext());

        binding.btnEnviarConsulta.setOnClickListener(v -> validarYEnviarConsulta());

        // Configura el botón inicialmente deshabilitado y con color gris
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
                // Limpiar errores (esto se manejará mejor en actualizarEstadoBotonEnviar para consistencia)
                // Es buena práctica que la validación se realice de forma consistente
                // y no solo borrar el error cuando se escribe.
                // Sin embargo, para mantener el comportamiento actual de limpiar, lo dejamos.
                if (Objects.requireNonNull(binding.etNombre.getText()).toString().trim().isEmpty()) {
                    binding.etNombre.setError(null);
                }
                if (Objects.requireNonNull(binding.etAsunto.getText()).toString().trim().isEmpty()) {
                    binding.etAsunto.setError(null);
                }
                if (Objects.requireNonNull(binding.etEmail.getText()).toString().trim().isEmpty()) {
                    binding.etEmail.setError(null);
                }
                if (Objects.requireNonNull(binding.etConsulta.getText()).toString().trim().isEmpty()) {
                    binding.etConsulta.setError(null);
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

        // Validaciones para habilitar/deshabilitar el botón y mostrar/ocultar errores en tiempo real
        // Nombre: no vacío, solo letras/espacios, máximo 50 caracteres
        boolean nombreValido = !nombre.isEmpty() && NAME_PATTERN.matcher(nombre).matches() && nombre.length() <= MAX_LENGTH;
        if (!nombre.isEmpty() && !NAME_PATTERN.matcher(nombre).matches()) {
            binding.tilNombre.setError(getString(R.string.error_name_invalid_characters));
        } else if (nombre.length() > MAX_LENGTH) {
            binding.tilNombre.setError(getString(R.string.error_max_length, MAX_LENGTH));
        } else {
            binding.tilNombre.setError(null);
        }

        // Asunto: no vacío, 2 caracteres o más, máximo 50 caracteres
        boolean asuntoValido = !asunto.isEmpty() && asunto.length() >= 2 && asunto.length() <= MAX_LENGTH;
        if (asunto.length() == 1) {
            binding.tilAsunto.setError(getString(R.string.error_asunto_min_length));
        } else if (asunto.length() > MAX_LENGTH) {
            binding.tilAsunto.setError(getString(R.string.error_max_length, MAX_LENGTH));
        } else {
            binding.tilAsunto.setError(null);
        }


        // Email: no vacío, formato válido, máximo 50 caracteres
        boolean emailValido = !email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.length() <= MAX_LENGTH;
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
        } else if (email.length() > MAX_LENGTH) {
            binding.tilEmail.setError(getString(R.string.error_max_length, MAX_LENGTH));
        } else {
            binding.tilEmail.setError(null);
        }

        // Consulta: no vacío, 10 caracteres o más, máximo 50 caracteres
        boolean consultaValida = !consulta.isEmpty() && consulta.length() >= 10 && consulta.length() <= MAX_LENGTH;
        if (!consulta.isEmpty() && consulta.length() < 10) {
            binding.tilConsulta.setError(getString(R.string.error_query_min_length));
        } else if (consulta.length() > MAX_LENGTH) {
            binding.tilConsulta.setError(getString(R.string.error_max_length, MAX_LENGTH));
        } else {
            binding.tilConsulta.setError(null);
        }


        // Si todas las validaciones pasan, habilitar y colorear el botón
        if (nombreValido && asuntoValido && emailValido && consultaValida) {
            binding.btnEnviarConsulta.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.system_error_900));
        } else {
            // Si alguna validación falla, deshabilitar y poner en gris el botón
            binding.btnEnviarConsulta.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        }
    }


    private void validarYEnviarConsulta() {
        // En este método, solo se realizan las validaciones finales y se muestra el error
        // específico si el usuario intenta enviar con campos inválidos.
        // La lógica principal de validación en tiempo real está en actualizarEstadoBotonEnviar().

        String nombre = Objects.requireNonNull(binding.etNombre.getText()).toString().trim();
        String asunto = Objects.requireNonNull(binding.etAsunto.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String consulta = Objects.requireNonNull(binding.etConsulta.getText()).toString().trim();

        // Aplicamos todas las validaciones aquí para la acción de enviar
        if (nombre.isEmpty()) {
            binding.tilNombre.setError(getString(R.string.error_name_required));
            binding.etNombre.requestFocus();
        } else if (!NAME_PATTERN.matcher(nombre).matches()) {
            binding.tilNombre.setError(getString(R.string.error_name_invalid_characters));
            binding.etNombre.requestFocus();
        } else if (nombre.length() > MAX_LENGTH) {
            binding.tilNombre.setError(getString(R.string.error_max_length, MAX_LENGTH));
            binding.etNombre.requestFocus();
        } else if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_email_required)); // Error si el email está vacío
            binding.etEmail.requestFocus();
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            binding.etEmail.requestFocus();
        } else if (email.length() > MAX_LENGTH) {
            binding.tilEmail.setError(getString(R.string.error_max_length, MAX_LENGTH));
            binding.etEmail.requestFocus();
        } else if (asunto.isEmpty()) {
            binding.tilAsunto.setError(getString(R.string.error_asunto_required));
            binding.etAsunto.requestFocus();
        } else if (asunto.length() < 2) {
            binding.tilAsunto.setError(getString(R.string.error_asunto_min_length));
            binding.etAsunto.requestFocus();
        } else if (asunto.length() > MAX_LENGTH) {
            binding.tilAsunto.setError(getString(R.string.error_max_length, MAX_LENGTH));
            binding.etAsunto.requestFocus();
        } else if (consulta.isEmpty()) {
            binding.tilConsulta.setError(getString(R.string.error_query_required));
            binding.etConsulta.requestFocus();
        } else if (consulta.length() < 10) {
            binding.tilConsulta.setError(getString(R.string.error_query_min_length));
            binding.etConsulta.requestFocus();
        } else if (consulta.length() > MAX_LENGTH) {
            binding.tilConsulta.setError(getString(R.string.error_max_length, MAX_LENGTH));
            binding.etConsulta.requestFocus();
        } else {
            // Si todas las validaciones pasan, enviar la consulta
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
                        actualizarEstadoBotonEnviar(); // Actualizar estado del botón después de limpiar
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
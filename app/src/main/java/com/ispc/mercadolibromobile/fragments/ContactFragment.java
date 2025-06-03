package com.ispc.mercadolibromobile.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Button and EditText imports are no longer strictly needed if not used directly

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.databinding.FragmentContactBinding;
import com.ispc.mercadolibromobile.models.Contacto;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactFragment extends Fragment {
    private static final String TAG = ContactFragment.class.getSimpleName();

    private ApiService apiService;
    private FragmentContactBinding binding;

    public static final String ARG_FROM_FEEDBACK = "from_feedback";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater, container, false);

        Bundle args = getArguments();
        if (args != null) {
            String modo = args.getString("modo", "");
            if (!modo.isEmpty()) {
                binding.etAsunto.setText(args.getString("asunto"));
                binding.etConsulta.setText(args.getString("mensaje"));
                if (modo.equals("ver")) {
                    binding.etAsunto.setEnabled(false);
                    binding.etConsulta.setEnabled(false);
                    binding.btnEnviarConsulta.setVisibility(View.GONE);
                }
                binding.btnVolver.setVisibility(View.VISIBLE);
                binding.btnVolver.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
            }
        }
        // Mostrar botón "Volver" solo si se vino desde FeedbackFragment
        boolean fromFeedback = args != null && args.getBoolean(ARG_FROM_FEEDBACK, false);
        if (fromFeedback) {
            binding.btnVolver.setVisibility(View.VISIBLE);
            binding.btnVolver.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        }

        apiService = RetrofitClient.getApiService(requireContext());

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
                if (Objects.requireNonNull(binding.etAsunto.getText()).toString().trim().isEmpty()) {
                    binding.tilAsunto.setError(null);
                }
                if (Objects.requireNonNull(binding.etConsulta.getText()).toString().trim().isEmpty()) {
                    binding.tilConsulta.setError(null);
                }
            }
        };

        binding.etAsunto.addTextChangedListener(textWatcher);
        binding.etConsulta.addTextChangedListener(textWatcher);
    }

    private void validarYEnviarConsulta() {
        String nombre = SessionUtils.getUserName(requireContext());
        String email = SessionUtils.getUserEmail(requireContext());

        String asunto = Objects.requireNonNull(binding.etAsunto.getText()).toString().trim();
        String consulta = Objects.requireNonNull(binding.etConsulta.getText()).toString().trim();

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
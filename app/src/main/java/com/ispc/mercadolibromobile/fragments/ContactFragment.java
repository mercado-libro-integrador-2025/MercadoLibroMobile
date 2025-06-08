package com.ispc.mercadolibromobile.fragments;

import android.content.Context;
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

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.databinding.FragmentContactBinding;
import com.ispc.mercadolibromobile.models.Contacto;
import com.ispc.mercadolibromobile.models.UserInfo;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactFragment extends Fragment {
    private static final String TAG = ContactFragment.class.getSimpleName();

    private ApiService apiService;
    private FragmentContactBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater, container, false);

        apiService = RetrofitClient.getApiService(requireContext());

        obtenerNombre(getContext());

        binding.btnEnviarConsulta.setOnClickListener(v -> validarYEnviarConsulta());

        agregarTextWatchers();

        binding.fabChat.setOnClickListener(v -> {
            // Aquí puedes abrir el ChatDialogFragment
            ChatFragment chatDialog = ChatFragment.newInstance();
            chatDialog.show(getParentFragmentManager(), "chat_dialog");
        });

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
                // Clear errors as user types
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
        String nombre = SessionUtils.getUserEmail(requireContext());
        String email = SessionUtils.getUserEmail(requireContext());

        String asunto = Objects.requireNonNull(binding.etAsunto.getText()).toString().trim();
        String consulta = Objects.requireNonNull(binding.etConsulta.getText()).toString().trim();

        if (asunto.isEmpty()) {
            binding.tilAsunto.setError(getString(R.string.error_asunto_required));
            binding.etAsunto.requestFocus();
            return;
        }
        if (consulta.isEmpty()) {
            binding.tilConsulta.setError(getString(R.string.error_query_required));
            binding.etConsulta.requestFocus();
            return;
        }
        if (consulta.length() < 10) {
            binding.tilConsulta.setError(getString(R.string.error_query_min_length));
            binding.etConsulta.requestFocus();
            return;
        }
        if (nombre == null || nombre.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(nombre).matches()) {
            Log.e(TAG, "Error: Email del usuario logueado no disponible o inválido para usar como identificador. No se puede enviar la consulta.");
            if (isAdded()) {
                Toast.makeText(getContext(), getString(R.string.error_user_email_not_found), Toast.LENGTH_LONG).show();
            }
            return;
        }
        if (email == null || email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.e(TAG, "Error: Email del usuario logueado no disponible o inválido. No se puede enviar la consulta.");
            if (isAdded()) {
                Toast.makeText(getContext(), getString(R.string.error_user_email_not_found), Toast.LENGTH_LONG).show();
            }
            return;
        }


        Contacto nuevaConsulta = new Contacto(nombre, email, asunto, consulta);
        enviarConsulta(nuevaConsulta);
    }

    public void obtenerNombre(Context context) {
        Call<List<UserInfo>> call = apiService.getUsers();

        call.enqueue(new Callback<List<UserInfo>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserInfo> usuarios = response.body();
                    String emailGuardado = SessionUtils.getUserEmail(context);

                    // Filtrar usuarios por email que coincida con el guardado
                    List<UserInfo> filtrados = new ArrayList<>();
                    for (UserInfo user : usuarios) {
                        if (emailGuardado.equals(user.getEmail())) {
                            filtrados.add(user);
                        }
                    }

                    if (filtrados.size() == 1) {
                        String username = filtrados.get(0).getUsername();
                        SessionUtils.saveUserName(context, username);
                        Log.d("USERNAME", "Usuario autenticado: " + username);
                    } else {
                        Log.e("USERNAME", "Error: se esperaba 1 usuario con email " + emailGuardado +
                                ", pero se encontraron " + filtrados.size());
                    }

                } else {
                    Log.e("USERNAME", "Respuesta fallida del servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {
                Log.e("USERNAME", "Error de red: " + t.getMessage());
            }
        });
    }

    private void enviarConsulta(@NonNull Contacto contacto) {
        Call<Void> call = apiService.enviarConsulta(contacto);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), getString(R.string.success_query_sent), Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Log.e(TAG, "Error al enviar consulta. Código: " + response.code() + ", Mensaje: " + response.message());
                        Toast.makeText(getContext(), getString(R.string.error_sending_query), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "Fallo en la conexión al enviar consulta: " + t.getMessage(), t);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
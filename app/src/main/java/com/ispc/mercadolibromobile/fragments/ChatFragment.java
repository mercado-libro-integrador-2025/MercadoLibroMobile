package com.ispc.mercadolibromobile.fragments;

import static com.ispc.mercadolibromobile.adapters.ContactoAdapter.LAST_FILTERED_ASUNTO_KEY;
import static com.ispc.mercadolibromobile.adapters.ContactoAdapter.PREFS_NAME;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.adapters.ContactoAdapter;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.databinding.FragmentChatBinding;
import com.ispc.mercadolibromobile.models.Contacto;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends DialogFragment {

    public static final String TAG = ChatFragment.class.getSimpleName();

    private ContactoAdapter contactoAdapter;
    private FragmentChatBinding binding;
    private ApiService apiService;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = RetrofitClient.getApiService(requireContext());

        setupRecyclerView();
        setupSearchEditText();
        setupSendButton();
        setupCloseButton();
        setupQueryInputValidation(); // Para la validación del campo de consulta
        loadConsultas(); // Carga los contactos/conversaciones al iniciar
    }

    // --- Métodos de Setup ---

    private void setupRecyclerView() {
        binding.rvContactos.setLayoutManager(new LinearLayoutManager(getContext()));
        contactoAdapter = new ContactoAdapter(getContext(), binding.getRoot()); // Asegúrate de que el adapter reciba el contexto adecuado
        binding.rvContactos.setAdapter(contactoAdapter);
    }

    private void setupSearchEditText() {
        binding.etBuscarContacto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contactoAdapter.filtrar(s.toString());
            }
        });
    }

    private void setupSendButton() {
        binding.btnEnviarConsulta.setOnClickListener(v -> {
            // Aquí se valida la consulta y se envía
            validarYEnviarConsulta();
        });
    }

    private void setupCloseButton() {
        binding.fabCerrarBusqueda.setOnClickListener(v -> {
            dismiss(); // Cierra el DialogFragment
        });
    }

    // --- Lógica de Negocio y API Calls ---

    // 🔄 Recarga de contactos desde API (método mejorado)
    public void loadConsultas() {
        // Muestra la barra de progreso (si existe en el layout del dialog)
        binding.progressBar.setVisibility(View.VISIBLE);

        apiService.obtenerConsultas().enqueue(new Callback<List<Contacto>>() {
            @Override
            public void onResponse(@NonNull Call<List<Contacto>> call, @NonNull Response<List<Contacto>> response) {
                // Oculta la barra de progreso
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    String userEmail = SessionUtils.getUserEmail(requireContext());
                    List<Contacto> filteredContacts = new ArrayList<>();

                    for (Contacto contacto : response.body()) {
                        if (contacto.getEmail() != null && contacto.getEmail().equalsIgnoreCase(userEmail)) {
                            filteredContacts.add(contacto);
                        }
                    }
                    contactoAdapter.updateContacts(filteredContacts); // Método para actualizar los datos en el adapter
                } else {
                    Log.e(TAG, "Error al obtener contactos: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Contacto>> call, @NonNull Throwable t) {
                // Oculta la barra de progreso
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Fallo en la conexión al obtener contactos: " + t.getMessage(), t);
                if (isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.error_network_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupQueryInputValidation() {
        binding.etConsulta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Limpia el error si el campo ya no está vacío
                if (s.toString().trim().isEmpty()) {
                    // Si estás usando TextInputLayout, limpia el error así:
                    binding.tilConsulta.setError(null);
                }
            }
        });
    }

    public String getLastFilteredAsunto() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LAST_FILTERED_ASUNTO_KEY, ""); // Devuelve "" si no se encuentra la clave
    }
    private void validarYEnviarConsulta() {
        String nombre = SessionUtils.getUserName(requireContext());
        String email = SessionUtils.getUserEmail(requireContext());
        String consulta = Objects.requireNonNull(binding.etConsulta.getText()).toString().trim();
        String asunto = getLastFilteredAsunto();

        if (asunto.isEmpty()) { // Si filtrar devuelve cadena vacía si no hay coincidencias
            binding.tilConsulta.setError("No hay asunto para esta consulta o el filtro no encontró uno.");
            binding.etConsulta.requestFocus();
            return; // Detener la ejecución
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

        if (nombre.isEmpty() || email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.e(TAG, "Error: Nombre o email del usuario logueado no disponibles o inválidos. No se puede enviar la consulta.");
            if (isAdded()) {
                Toast.makeText(getContext(), getString(R.string.error_user_id_not_found), Toast.LENGTH_LONG).show();
            }
            return;
        }

        enviarConsulta(new Contacto(nombre, email, asunto, consulta));
    }


    private void enviarConsulta(@NonNull Contacto contacto) {
        // Deshabilita el botón o muestra un ProgressBar para indicar que se está enviando
        binding.btnEnviarConsulta.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        apiService.enviarConsulta(contacto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // Vuelve a habilitar el botón y oculta el ProgressBar
                binding.btnEnviarConsulta.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), getString(R.string.success_query_sent), Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                        // Opcional: recargar los contactos si la consulta enviada debería aparecer en la lista
                        // loadConsultas();
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
                // Vuelve a habilitar el botón y oculta el ProgressBar
                binding.btnEnviarConsulta.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);

                Log.e(TAG, "Fallo en la conexión al enviar consulta: " + t.getMessage(), t);
                if (isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.error_network_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });
        loadConsultas();
    }

    private void limpiarCampos() {
        binding.etConsulta.setText("");
        binding.tilConsulta.setError(null);
    }

    public ChatFragment() {
        // Constructor público vacío es necesario para DialogFragment
    }

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }
}
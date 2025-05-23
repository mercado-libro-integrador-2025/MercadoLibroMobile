package com.example.mercadolibromobile.fragments; // O com.ispc.mercadolibromobile.fragments si ya refactorizaste el paquete

import android.os.Bundle;
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

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.api.ApiService;
import com.example.mercadolibromobile.api.RetrofitClient; // Importar RetrofitClient
import com.example.mercadolibromobile.models.Contacto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactFragment extends Fragment {

    private ApiService apiService;
    private EditText nombreEditText, asuntoEditText, emailEditText, consultaEditText;

    private static final String TAG = "ContactFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        apiService = RetrofitClient.getApiService(getContext());

        // Inicializar las vistas
        nombreEditText = view.findViewById(R.id.etNombre);
        asuntoEditText = view.findViewById(R.id.etAsunto);
        emailEditText = view.findViewById(R.id.etEmail);
        consultaEditText = view.findViewById(R.id.etConsulta);
        Button enviarConsultaButton = view.findViewById(R.id.btnEnviarConsulta);

        // Establecer el comportamiento del botón "Enviar consulta"
        enviarConsultaButton.setOnClickListener(v -> {
            String nombre = nombreEditText.getText().toString().trim();
            String asunto = asuntoEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String consulta = consultaEditText.getText().toString().trim();

            if (asunto.isEmpty()) {
                asuntoEditText.setError(getString(R.string.error_asunto_required));
                asuntoEditText.requestFocus();
            } else if (nombre.isEmpty()) {
                nombreEditText.setError(getString(R.string.error_name_required));
                nombreEditText.requestFocus();
            } else if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError(getString(R.string.error_email_invalid));
                emailEditText.requestFocus();
            } else if (consulta.isEmpty()) {
                consultaEditText.setError(getString(R.string.error_query_required));
                consultaEditText.requestFocus();
            } else if (consulta.length() < 10) {
                consultaEditText.setError(getString(R.string.error_query_min_length));
                consultaEditText.requestFocus();
            } else {
                Contacto contacto = new Contacto(nombre, email, asunto, consulta);
                enviarConsulta(contacto);
            }
        });

        return view;
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
                    Toast.makeText(getContext(), getString(R.string.error_network_connection, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void limpiarCampos() {
        nombreEditText.setText("");
        asuntoEditText.setText("");
        emailEditText.setText("");
        consultaEditText.setText("");
        nombreEditText.setError(null);
        asuntoEditText.setError(null);
        emailEditText.setError(null);
        consultaEditText.setError(null);
    }
}

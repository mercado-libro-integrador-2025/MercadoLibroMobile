package com.ispc.mercadolibromobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.activities.SplashActivity;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.models.User;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private TextView emailTextView;
    private String authToken;
    private ApiService apiService;

    private static final String TAG = "ProfileFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        emailTextView = rootView.findViewById(R.id.textView9);
        apiService = RetrofitClient.getApiService(requireContext());

        Button estadoEnvioButton = rootView.findViewById(R.id.button8);
        estadoEnvioButton.setOnClickListener(v -> {
            Log.d(TAG, "Botón estadoEnvioButton presionado - Iniciando PedidosFragment");
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new PedidosFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        // Obtener el email y el token de autenticación usando SessionUtils
        String userEmail = SessionUtils.getUserEmail(requireContext()); // Usar requireContext()
        authToken = SessionUtils.getAuthToken(requireContext()); // Usar requireContext()
        Log.d(TAG, "Token: " + authToken);

        // Mostrar el email del usuario
        emailTextView.setText(userEmail != null ? userEmail : getString(R.string.email_not_found)); // Usar recurso de string

        /*
        // Botón de Mis Reseñas
        Button reviewsButton = rootView.findViewById(R.id.button2);
        reviewsButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MisResenasActivity.class);
            startActivity(intent);
        });
        */

        // Botón de Editar mis datos (si se implementa)
        Button editProfileButton = rootView.findViewById(R.id.button10);
        editProfileButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), getString(R.string.feature_not_implemented), Toast.LENGTH_SHORT).show();
        });


        Button deleteUserButton = rootView.findViewById(R.id.button9);
        deleteUserButton.setOnClickListener(v -> {
            if (authToken != null) {
                confirmarEliminacionUsuario();
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_no_active_session_delete), Toast.LENGTH_SHORT).show(); // Usar recurso de string
                Log.d(TAG, "Token inválido: No se puede obtener el ID del usuario");
            }
        });

        return rootView;
    }
    private void confirmarEliminacionUsuario() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialogView.findViewById(R.id.positive_button);
            Button negativeButton = dialogView.findViewById(R.id.negative_button);

            positiveButton.setOnClickListener(v -> {
                dialog.dismiss();
                // Obtener el ID del usuario autenticado antes de intentar eliminarlo
                obtenerUsuarioAutenticado("Bearer " + authToken);
            });

            negativeButton.setOnClickListener(v -> dialog.dismiss());
        });

        dialog.show();
    }
    private void obtenerUsuarioAutenticado(@NonNull String authToken) {
        // Usar la instancia de ApiService obtenida de RetrofitClient
        Call<User> call = apiService.getAuthenticatedUser(authToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                // Asegurarse de que el fragmento está adjunto antes de actualizar la UI
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        int userId = response.body().getUser().getId(); // Acceder al ID del usuario a través de UserInfo
                        Log.d(TAG, "User ID obtenido: " + userId);
                        eliminarUsuario(userId, authToken);
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.error_getting_user_data), Toast.LENGTH_SHORT).show(); // Usar recurso de string
                        Log.d(TAG, "Error al obtener usuario: " + response.code() + " - " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                // Asegurarse de que el fragmento está adjunto antes de mostrar Toast
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_user_data), Toast.LENGTH_SHORT).show(); // Usar recurso de string
                    Log.e(TAG, "Fallo en la llamada de usuario autenticado: " + t.getMessage(), t);
                }
            }
        });
    }
    private void eliminarUsuario(int userId, @NonNull String authToken) {
        // Usar la instancia de ApiService obtenida de RetrofitClient
        Call<Void> call = apiService.deleteUser(userId, authToken);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // Asegurarse de que el fragmento está adjunto antes de actualizar la UI
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Usuario eliminado con éxito");
                        Toast.makeText(requireContext(), getString(R.string.success_user_deleted), Toast.LENGTH_SHORT).show(); // Usar recurso de string
                        SessionUtils.clearSession(requireContext()); // Limpiar la sesión usando SessionUtils
                        // Redirigir a SplashActivity para reiniciar la aplicación
                        Intent intent = new Intent(requireActivity(), SplashActivity.class); // Usar requireActivity()
                        startActivity(intent);
                        requireActivity().finish(); // Finaliza la actividad actual
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.error_deleting_user), Toast.LENGTH_SHORT).show(); // Usar recurso de string
                        Log.d(TAG, "Error al eliminar usuario: " + response.code() + " - " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                // Asegurarse de que el fragmento está adjunto antes de mostrar Toast
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_delete_user), Toast.LENGTH_SHORT).show(); // Usar recurso de string
                    Log.e(TAG, "Fallo en la llamada de eliminación de usuario: " + t.getMessage(), t);
                }
            }
        });
    }
}

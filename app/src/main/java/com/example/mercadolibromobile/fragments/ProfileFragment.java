package com.example.mercadolibromobile.fragments;

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

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.activities.SplashActivity;
import com.example.mercadolibromobile.api.ApiService; // Usar ApiService
import com.example.mercadolibromobile.api.RetrofitClient; // Usar RetrofitClient
import com.example.mercadolibromobile.models.User;
import com.example.mercadolibromobile.utils.SessionUtils; // Usar SessionUtils

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private TextView emailTextView;
    private String authToken;
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        emailTextView = rootView.findViewById(R.id.textView9);
        apiService = RetrofitClient.getApiService(getContext());

        Button estadoEnvioButton = rootView.findViewById(R.id.button8);
        estadoEnvioButton.setOnClickListener(v -> {
            Log.d("ProfileFragment", "Botón estadoEnvioButton presionado - Iniciando PedidosFragment");
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new PedidosFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        String userEmail = SessionUtils.getUserEmail(getContext());
        authToken = SessionUtils.getAuthToken(getContext());
        Log.d("ProfileFragment", "Token: " + authToken);

        emailTextView.setText(userEmail != null ? userEmail : "Email no encontrado");

        /*Button reviewsButton = rootView.findViewById(R.id.button2);
        reviewsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MisResenasActivity.class);
            startActivity(intent);
        });*/

        Button deleteUserButton = rootView.findViewById(R.id.button9);
        deleteUserButton.setOnClickListener(v -> {
            if (authToken != null) {
                confirmarEliminacionUsuario();
            } else {
                Toast.makeText(getContext(), "No hay sesión activa para eliminar el usuario.", Toast.LENGTH_SHORT).show();
                Log.d("ProfileFragment", "Token inválido: No se puede obtener el ID del usuario");
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
                obtenerUsuarioAutenticado("Bearer " + authToken);
            });

            negativeButton.setOnClickListener(v -> dialog.dismiss());
        });

        dialog.show();
    }
    private void obtenerUsuarioAutenticado(String authToken) {
        Call<User> call = apiService.getAuthenticatedUser(authToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int userId = response.body().getUser().getId();
                    Log.d("ProfileFragment", "User ID obtenido: " + userId);
                    eliminarUsuario(userId, authToken);
                } else {
                    Toast.makeText(getContext(), "Error al obtener datos del usuario.", Toast.LENGTH_SHORT).show();
                    Log.d("ProfileFragment", "Error al obtener usuario: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo en la conexión para obtener usuario.", Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Fallo en la llamada de usuario autenticado: " + t.getMessage());
            }
        });
    }
    private void eliminarUsuario(int userId, String authToken) {
        Call<Void> call = apiService.deleteUser(userId, authToken);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ProfileFragment", "Usuario eliminado con éxito");
                    Toast.makeText(getContext(), "Usuario eliminado con éxito.", Toast.LENGTH_SHORT).show();
                    SessionUtils.clearSession(requireContext());
                    Intent intent = new Intent(getActivity(), SplashActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Error al eliminar el usuario.", Toast.LENGTH_SHORT).show();
                    Log.d("ProfileFragment", "Error al eliminar usuario: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo en la conexión para eliminar usuario.", Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Fallo en la llamada de eliminación de usuario: " + t.getMessage());
            }
        });
    }
}

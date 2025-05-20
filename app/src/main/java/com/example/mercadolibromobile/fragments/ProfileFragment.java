package com.example.mercadolibromobile.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.activities.SplashActivity;
import com.example.mercadolibromobile.activities.MisResenasActivity;
import com.example.mercadolibromobile.api.deleteApi;
import com.example.mercadolibromobile.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {
    private TextView emailTextView;
    private String authToken;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Referencia al TextView del email
        emailTextView = rootView.findViewById(R.id.textView9);

        // Configuración del botón para ver el estado de envío
        Button estadoEnvioButton = rootView.findViewById(R.id.button8);
        estadoEnvioButton.setOnClickListener(v -> {
            Log.d("ProfileFragment", "Botón estadoEnvioButton presionado - Iniciando PedidosFragment");
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new PedidosFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        // Obtener el correo electrónico desde SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("user_email", "No email found");
        authToken = sharedPreferences.getString("access_token", null);
        Log.d("ProfileFragment", "Token: " + authToken);

        // Mostrar el correo electrónico en el TextView
        emailTextView.setText(userEmail);

        // Configurar el botón "Mis Reseñas"
        Button reviewsButton = rootView.findViewById(R.id.button2);
        reviewsButton.setOnClickListener(v -> {
            // Abrir MisResenasActivity
            Intent intent = new Intent(getActivity(), MisResenasActivity.class);
            startActivity(intent);
        });

        // Configurar el botón "Dar de baja usuario"
        Button deleteUserButton = rootView.findViewById(R.id.button9);
        deleteUserButton.setOnClickListener(v -> {
            if (authToken != null) {
                confirmarEliminacionUsuario();
            } else {
                Log.d("ProfileFragment", "Token inválido: No se puede obtener el ID del usuario");
            }
        });

        return rootView;
    }

    private void confirmarEliminacionUsuario() {
        // Inflar el layout personalizado del diálogo
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete, null);

        // Crear el AlertDialog con el layout personalizado
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // Configurar los botones en el diálogo
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialogView.findViewById(R.id.positive_button);
            Button negativeButton = dialogView.findViewById(R.id.negative_button);

            // Configurar botón "Sí" para confirmar la eliminación
            positiveButton.setOnClickListener(v -> {
                dialog.dismiss();
                obtenerUsuarioAutenticado("Bearer " + authToken);
            });

            // Configurar botón "No" para cerrar el diálogo
            negativeButton.setOnClickListener(v -> dialog.dismiss());
        });

        // Mostrar el diálogo
        dialog.show();
    }

    private void obtenerUsuarioAutenticado(String authToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-mercado-libro-mobile.onrender.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deleteApi apiService = retrofit.create(deleteApi.class);
        Call<User> call = apiService.getAuthenticatedUser(authToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int userId = response.body().getId();
                    Log.d("ProfileFragment", "User ID obtenido: " + userId);
                    eliminarUsuario(userId, authToken);
                } else {
                    Log.d("ProfileFragment", "Error al obtener usuario: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("ProfileFragment", "Fallo en la llamada de usuario autenticado: " + t.getMessage());
            }
        });
    }

    private void eliminarUsuario(int userId, String authToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-mercado-libro-mobile.onrender.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deleteApi apiService = retrofit.create(deleteApi.class);
        Call<Void> call = apiService.deleteUser(userId, authToken);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ProfileFragment", "Usuario eliminado con éxito");
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    sharedPreferences.edit().clear().apply();
                    Intent intent = new Intent(getActivity(), SplashActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    Log.d("ProfileFragment", "Error al eliminar usuario: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ProfileFragment", "Fallo en la llamada de eliminación de usuario: " + t.getMessage());
            }
        });
    }
}

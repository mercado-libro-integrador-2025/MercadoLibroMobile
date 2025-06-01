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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.activities.SplashActivity;
import com.ispc.mercadolibromobile.adapters.DireccionAdapter;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.models.User; // <--- Este es el modelo principal con tokens y UserInfo
import com.ispc.mercadolibromobile.models.Direccion;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment implements DireccionAdapter.OnDireccionInteractionListener { // <-- Interfaz del adaptador actualizada

    private TextView emailTextView;
    private String authToken;
    private ApiService apiService;
    private RecyclerView rvAddresses;
    private DireccionAdapter addressAdapter;
    private List<Direccion> addressList;

    private static final String TAG = "ProfileFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        emailTextView = rootView.findViewById(R.id.textView9);
        apiService = RetrofitClient.getApiService(requireContext());

        rvAddresses = rootView.findViewById(R.id.rvAddresses);
        rvAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));
        addressList = new ArrayList<>();
        addressAdapter = new DireccionAdapter(addressList, this);
        rvAddresses.setAdapter(addressAdapter);

        Button estadoEnvioButton = rootView.findViewById(R.id.button8);
        estadoEnvioButton.setOnClickListener(v -> {
            Log.d(TAG, "Botón estadoEnvioButton presionado - Iniciando PedidosFragment");
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            //fragmentTransaction.replace(R.id.fragment_container, new PedidosFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        String userEmail = SessionUtils.getUserEmail(requireContext());
        authToken = SessionUtils.getAuthToken(requireContext());
        Log.d(TAG, "Token: " + authToken);

        emailTextView.setText(userEmail != null ? userEmail : getString(R.string.email_not_found));

        Button btnMyReviews = rootView.findViewById(R.id.button2);
        btnMyReviews.setOnClickListener(v -> {
            if (isAdded()) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MyReviewsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        Button btnAddAddress = rootView.findViewById(R.id.btnAddAddress);
        btnAddAddress.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, DireccionFormFragment.newInstance(null))
                    .addToBackStack(null)
                    .commit();
        });

        Button deleteUserButton = rootView.findViewById(R.id.button9);
        deleteUserButton.setOnClickListener(v -> {
            if (authToken != null) {
                confirmarEliminacionUsuario();
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_no_active_session_delete), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Token inválido: No se puede obtener el ID del usuario");
            }
        });

        getParentFragmentManager().setFragmentResultListener("requestKeyDireccionSaved", this, (requestKey, bundle) -> {
            Direccion savedDireccion = (Direccion) bundle.getSerializable("saved_direccion");
            if (savedDireccion != null) {
                Toast.makeText(getContext(), "Dirección guardada/actualizada con éxito.", Toast.LENGTH_SHORT).show();
                loadAddresses();
            }
        });

        if (authToken != null) {
            loadAddresses();
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_auth_required_addresses), Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (authToken != null) {
            loadAddresses();
            String userEmail = SessionUtils.getUserEmail(requireContext());
            emailTextView.setText(userEmail != null ? userEmail : getString(R.string.email_not_found));
        }
    }

    private void loadAddresses() {
        if (authToken == null) {
            Log.e(TAG, "No hay token de autenticación para cargar direcciones.");
            addressList.clear();
            addressAdapter.notifyDataSetChanged();
            return;
        }

        apiService.getDirecciones("Bearer " + authToken).enqueue(new Callback<List<Direccion>>() {
            @Override
            public void onResponse(@NonNull Call<List<Direccion>> call, @NonNull Response<List<Direccion>> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        addressList.clear();
                        addressList.addAll(response.body());
                        addressAdapter.notifyDataSetChanged();
                        if (addressList.isEmpty()) {
                            Toast.makeText(requireContext(), getString(R.string.no_addresses_saved), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error al cargar direcciones: " + response.code() + " - " + response.message());
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                            Log.e(TAG, "Error body al cargar direcciones: " + errorBody);
                            Toast.makeText(requireContext(), getString(R.string.error_loading_addresses) + " " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body al cargar direcciones", e);
                            Toast.makeText(requireContext(), getString(R.string.error_loading_addresses), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Direccion>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "Fallo de red al cargar direcciones: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_addresses), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onDireccionClick(Direccion direccion) {
        onEditDireccion(direccion);
    }

    @Override
    public void onEditDireccion(Direccion direccion) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, DireccionFormFragment.newInstance(direccion))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDeleteDireccion(int direccionId) {
        if (authToken == null) {
            Toast.makeText(requireContext(), getString(R.string.error_auth_required_delete_address), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_delete_address_title))
                .setMessage(getString(R.string.confirm_delete_address_message))
                .setPositiveButton(getString(R.string.dialog_confirm_delete_yes_button), (dialog, which) -> {
                    apiService.deleteDireccion(direccionId, "Bearer " + authToken).enqueue(new Callback<Void>() { // Asegúrate de que deleteDireccion en ApiService toma el token como segundo parámetro
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            if (isAdded()) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(requireContext(), getString(R.string.success_address_deleted), Toast.LENGTH_SHORT).show();
                                    loadAddresses();
                                } else {
                                    Log.e(TAG, "Error al eliminar dirección: " + response.code() + " - " + response.message());
                                    try {
                                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                        Log.e(TAG, "Error body al eliminar dirección: " + errorBody);
                                        Toast.makeText(requireContext(), getString(R.string.error_deleting_address) + " " + errorBody, Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing error body al eliminar dirección", e);
                                        Toast.makeText(requireContext(), getString(R.string.error_deleting_address), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            if (isAdded()) {
                                Log.e(TAG, "Fallo de red al eliminar dirección: " + t.getMessage(), t);
                                Toast.makeText(requireContext(), getString(R.string.error_network_connection_delete_address), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton(getString(R.string.dialog_confirm_delete_no_button), null)
                .show();
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

    private void obtenerUsuarioAutenticado(@NonNull String authToken) {
        Call<User> call = apiService.getAuthenticatedUser(authToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                        int userId = response.body().getUser().getId();
                        SessionUtils.saveUserId(requireContext(), userId);
                        Log.d(TAG, "User ID obtenido: " + userId);
                        eliminarUsuario(userId, authToken);
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.error_getting_user_data), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Error al obtener usuario: " + response.code() + " - " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_user_data), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Fallo en la llamada de usuario autenticado: " + t.getMessage(), t);
                }
            }
        });
    }

    private void eliminarUsuario(int userId, @NonNull String authToken) {
        Call<Void> call = apiService.deleteUser(userId, authToken);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Usuario eliminado con éxito");
                        Toast.makeText(requireContext(), getString(R.string.success_user_deleted), Toast.LENGTH_SHORT).show();
                        SessionUtils.clearSession(requireContext());
                        Intent intent = new Intent(requireActivity(), SplashActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.error_deleting_user), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Error al eliminar usuario: " + response.code() + " - " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_delete_user), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Fallo en la llamada de eliminación de usuario: " + t.getMessage(), t);
                }
            }
        });
    }
}
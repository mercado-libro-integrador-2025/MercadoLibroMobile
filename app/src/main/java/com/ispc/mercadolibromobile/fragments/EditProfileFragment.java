package com.ispc.mercadolibromobile.fragments;

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

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.models.User;
import com.ispc.mercadolibromobile.models.UserInfo;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";

    private EditText etUsername;
    private Button btnSaveProfile;

    private ApiService apiService;
    private String authToken;
    private int currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        etUsername = view.findViewById(R.id.etUsername);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);

        apiService = RetrofitClient.getApiService(requireContext());
        authToken = SessionUtils.getAuthToken(requireContext());
        currentUserId = SessionUtils.getUserId(requireContext());

        if (authToken == null || currentUserId == -1) {
            Toast.makeText(requireContext(), getString(R.string.error_auth_required), Toast.LENGTH_SHORT).show();
            if (isAdded()) {
                getParentFragmentManager().popBackStack();
            }
            return view;
        }

        loadUserData();

        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());

        return view;
    }

    private void loadUserData() {
        apiService.getAuthenticatedUser("Bearer " + authToken).enqueue(new Callback<User>() { // <--- Espera el modelo User completo
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                        UserInfo userInfo = response.body().getUser();
                        if (userInfo.getUsername() != null) {
                            etUsername.setText(userInfo.getUsername());
                        } else {
                            etUsername.setHint(getString(R.string.hint_enter_username));
                        }
                        // if (etUserEmail != null && userInfo.getEmail() != null) {
                        //     etUserEmail.setText(userInfo.getEmail());
                        //     etUserEmail.setEnabled(false);
                        // }
                    } else {
                        Log.e(TAG, "Error al cargar datos del usuario: " + response.code() + " - " + response.message());
                        Toast.makeText(requireContext(), getString(R.string.error_loading_user_data), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "Fallo de red al cargar datos del usuario: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_user_data), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveProfileChanges() {
        String newUsername = etUsername.getText().toString().trim(); // Cambiado a newUsername

        if (newUsername.isEmpty()) {
            etUsername.setError(getString(R.string.error_username_required));
            etUsername.requestFocus();
            return;
        }

        UserInfo updatedUserInfo = new UserInfo();
        updatedUserInfo.setId(currentUserId);
        updatedUserInfo.setUsername(newUsername);

        apiService.updateUser(currentUserId, "Bearer " + authToken, updatedUserInfo).enqueue(new Callback<UserInfo>() { // <--- Espera UserInfo
            @Override
            public void onResponse(@NonNull Call<UserInfo> call, @NonNull Response<UserInfo> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(requireContext(), getString(R.string.success_profile_updated), Toast.LENGTH_SHORT).show();

                        SessionUtils.saveUserName(requireContext(), response.body().getUsername());
                        SessionUtils.saveUserEmail(requireContext(), response.body().getEmail());

                        getParentFragmentManager().popBackStack();
                    } else {
                        Log.e(TAG, "Error al actualizar perfil: " + response.code() + " - " + response.message() + " - " + (response.errorBody() != null ? response.errorBody().toString() : ""));
                        Toast.makeText(requireContext(), getString(R.string.error_updating_profile), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "Fallo de red al actualizar perfil: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_profile), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
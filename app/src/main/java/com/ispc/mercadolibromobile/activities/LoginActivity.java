package com.ispc.mercadolibromobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.models.AuthModels;
import com.ispc.mercadolibromobile.utils.SessionUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout usernameLayout, passwordLayout, nameLayout;
    private TextInputEditText usernameEditText, passwordEditText, nameEditText;
    private Button loginButton, toggleModeButton, poliButton;
    private ProgressBar progressBar;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setListeners();
        validateButtonState();
    }

    private void initViews() {
        usernameLayout = findViewById(R.id.textInputLayoutUsername);
        passwordLayout = findViewById(R.id.textInputLayoutPassword);
        nameLayout = findViewById(R.id.textInputLayoutName);

        usernameEditText = findViewById(R.id.textInputEditTextUsername);
        passwordEditText = findViewById(R.id.textInputEditTextPassword);
        nameEditText = findViewById(R.id.textInputEditTextName);

        loginButton = findViewById(R.id.buttonMainAction);
        toggleModeButton = findViewById(R.id.buttonToggleMode);
        poliButton = findViewById(R.id.buttonpoli);

        progressBar = findViewById(R.id.progressBar);
    }

    private void setListeners() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        toggleModeButton.setOnClickListener(v -> toggleLoginMode(fadeIn));

        loginButton.setOnClickListener(v -> {
            if (isLoginMode) {
                loginUser();
            } else {
                registerUser();
            }
        });

        poliButton.setOnClickListener(v -> startActivity(new Intent(this, Politicas.class)));

        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateButtonState();
            }
        };

        usernameEditText.addTextChangedListener(watcher);
        passwordEditText.addTextChangedListener(watcher);
        nameEditText.addTextChangedListener(watcher);
    }

    private void toggleLoginMode(Animation fadeIn) {
        isLoginMode = !isLoginMode;
        loginButton.setText(isLoginMode ? R.string.button_login_text : R.string.button_register_text);
        toggleModeButton.setText(isLoginMode ? R.string.button_register_text : R.string.button_back_text);

        if (isLoginMode) {
            nameLayout.setVisibility(View.GONE);
            findViewById(R.id.textViewName).setVisibility(View.GONE);
        } else {
            nameLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.textViewName).setVisibility(View.VISIBLE);
            findViewById(R.id.textViewName).startAnimation(fadeIn);
        }

        clearErrors();
        validateButtonState();
    }

    private void clearErrors() {
        usernameLayout.setError(null);
        passwordLayout.setError(null);
        nameLayout.setError(null);
    }

    private void validateButtonState() {
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();

        boolean isEnabled = !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
                && (isLoginMode || !TextUtils.isEmpty(name));

        loginButton.setEnabled(isEnabled);
    }

    private void loginUser() {
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            usernameLayout.setError(getString(R.string.error_invalid_email));
            usernameEditText.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.error_password_required));
            passwordEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getApiService(this).login(email, password).enqueue(new Callback<AuthModels.LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthModels.LoginResponse> call, @NonNull Response<AuthModels.LoginResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    AuthModels.LoginResponse data = response.body();
                    SessionUtils.saveAuthToken(LoginActivity.this, data.getAccess());
                    SessionUtils.saveRefreshToken(LoginActivity.this, data.getRefresh());
                    SessionUtils.saveUserId(LoginActivity.this, data.getUserId());
                    SessionUtils.saveUserEmail(LoginActivity.this, email);

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Usar recurso de string para el mensaje de error
                    usernameLayout.setError(getString(R.string.error_invalid_credentials));
                    Toast.makeText(LoginActivity.this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Login failed: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthModels.LoginResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, getString(R.string.error_network_connection), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Login network error: ", t);
            }
        });
    }

    private void registerUser() {
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = nameEditText.getText().toString().trim();

        clearErrors();

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            usernameLayout.setError(getString(R.string.error_invalid_email));
            usernameEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.error_password_min_length));
            passwordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            nameLayout.setError(getString(R.string.error_name_required));
            nameEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        AuthModels.SignupRequest request = new AuthModels.SignupRequest(email, password, username);
        RetrofitClient.getApiService(this).register(request).enqueue(new Callback<AuthModels.SignupResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthModels.SignupResponse> call, @NonNull Response<AuthModels.SignupResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    showSuccessDialog();
                } else {
                    usernameLayout.setError(getString(R.string.error_registration_failed));
                    Toast.makeText(LoginActivity.this, getString(R.string.error_registration_failed_toast), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Registration failed: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthModels.SignupResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                // Usar recurso de string para el mensaje de error de red
                Toast.makeText(LoginActivity.this, getString(R.string.error_network_connection), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Register network error: ", t);
            }
        });
    }

    private void showSuccessDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_alert, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.setOnShowListener(d -> {
            Button btn = dialogView.findViewById(R.id.positive_button);
            if (btn != null) {
                btn.setOnClickListener(v -> dialog.dismiss());
            } else {
                Log.e(TAG, "dialog_alert layout is missing positive_button");
            }
        });

        dialog.show();
    }
    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}

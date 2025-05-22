package com.example.mercadolibromobile.activities;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.api.RetrofitClient;
import com.example.mercadolibromobile.models.AuthModels;
import com.example.mercadolibromobile.utils.SessionUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

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
        usernameLayout = findViewById(R.id.textInputLayout2);
        passwordLayout = findViewById(R.id.textInputLayout);
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
        loginButton.setText(isLoginMode ? R.string.ingresar : R.string.registrarse);
        toggleModeButton.setText(isLoginMode ? R.string.registrarse : R.string.volver);

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
        progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getApiService(this).login(email, password).enqueue(new Callback<AuthModels.LoginResponse>() {
            @Override
            public void onResponse(Call<AuthModels.LoginResponse> call, Response<AuthModels.LoginResponse> response) {
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
                    usernameLayout.setError("Credenciales incorrectas");
                }
            }

            @Override
            public void onFailure(Call<AuthModels.LoginResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "Error: ", t);
            }
        });
    }

    private void registerUser() {
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = nameEditText.getText().toString().trim();

        clearErrors();

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            usernameLayout.setError("Correo inválido");
            return;
        }

        if (password.length() < 6) {
            passwordLayout.setError("Mínimo 6 caracteres");
            return;
        }

        if (TextUtils.isEmpty(username)) {
            nameLayout.setError("Nombre requerido");
            return;
        }

        AuthModels.SignupRequest request = new AuthModels.SignupRequest(email, password, username);
        RetrofitClient.getApiService(this).register(request).enqueue(new Callback<AuthModels.SignupResponse>() {
            @Override
            public void onResponse(Call<AuthModels.SignupResponse> call, Response<AuthModels.SignupResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    showSuccessDialog();
                } else {
                    usernameLayout.setError("No se pudo registrar. Intenta nuevamente.");
                }
            }

            @Override
            public void onFailure(Call<AuthModels.SignupResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                Log.e("Register", "Error: ", t);
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
            btn.setOnClickListener(v -> dialog.dismiss());
        });

        dialog.show();
    }

    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}

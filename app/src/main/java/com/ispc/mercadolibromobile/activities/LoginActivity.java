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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.models.AuthModels;
import com.ispc.mercadolibromobile.models.User;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout usernameLayout, passwordLayout, nameLayout, repeatPasswordLayout;
    private TextInputEditText usernameEditText, passwordEditText, nameEditText, repeatPasswordEditText;
    private Button loginButton, toggleModeButton, poliButton;
    private ProgressBar progressBar;
    private boolean isLoginMode = true;

    // Regex para validar contraseña: Al menos 1 mayúscula, 1 minúscula, 1 número, 1 carácter especial, longitud 8-16
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-])[A-Za-z\\d@$!%*?&_\\-]{8,16}$";
    // Regex para validar nombre de usuario: Solo letras y espacios
    private static final String USERNAME_REGEX = "^[a-zA-Z\\s]+$";

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
        repeatPasswordLayout = findViewById(R.id.textInputLayoutRepeatPassword); // Nuevo

        usernameEditText = findViewById(R.id.textInputEditTextUsername);
        passwordEditText = findViewById(R.id.textInputEditTextPassword);
        nameEditText = findViewById(R.id.textInputEditTextName);
        repeatPasswordEditText = findViewById(R.id.textInputEditTextRepeatPassword); // Nuevo

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
                clearErrors(); // Limpiar errores al escribir
                validateButtonState();
            }
        };

        usernameEditText.addTextChangedListener(watcher);
        passwordEditText.addTextChangedListener(watcher);
        nameEditText.addTextChangedListener(watcher);
        repeatPasswordEditText.addTextChangedListener(watcher); // Nuevo listener
    }

    private void toggleLoginMode(Animation fadeIn) {
        isLoginMode = !isLoginMode;
        loginButton.setText(isLoginMode ? R.string.button_login_text : R.string.button_register_text);
        toggleModeButton.setText(isLoginMode ? R.string.button_register_text : R.string.button_back_text);

        if (isLoginMode) {
            nameLayout.setVisibility(View.GONE);
            findViewById(R.id.textViewName).setVisibility(View.GONE);
            repeatPasswordLayout.setVisibility(View.GONE); // Ocultar al cambiar a login
            findViewById(R.id.textViewRepeatPassword).setVisibility(View.GONE); // Ocultar
        } else {
            nameLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.textViewName).setVisibility(View.VISIBLE);
            findViewById(R.id.textViewName).startAnimation(fadeIn);
            repeatPasswordLayout.setVisibility(View.VISIBLE); // Mostrar al cambiar a registro
            findViewById(R.id.textViewRepeatPassword).setVisibility(View.VISIBLE); // Mostrar
            findViewById(R.id.textViewRepeatPassword).startAnimation(fadeIn);
        }

        clearErrors();
        validateButtonState();
    }

    private void clearErrors() {
        usernameLayout.setError(null);
        passwordLayout.setError(null);
        nameLayout.setError(null);
        repeatPasswordLayout.setError(null); // Limpiar error de repetir contraseña
    }

    private void validateButtonState() {
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();

        boolean isEnabled;
        if (isLoginMode) {
            isEnabled = !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password);
        } else {
            isEnabled = !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
                    && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(repeatPassword);
        }

        loginButton.setEnabled(isEnabled);
    }

    private void loginUser() {
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        clearErrors();

        // Validaciones para Email (aplicadas tanto en login como en registro)
        if (TextUtils.isEmpty(email)) {
            usernameLayout.setError(getString(R.string.error_email_required));
            usernameEditText.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            usernameLayout.setError(getString(R.string.error_invalid_email));
            usernameEditText.requestFocus();
            return;
        }

        // Validaciones para Contraseña (AHORA APLICADAS TAMBIÉN PARA LOGIN)
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_password_required));
            passwordEditText.requestFocus();
            return;
        }
        // Se aplican las mismas reglas de longitud y formato que en el registro
        if (password.length() < 8 || password.length() > 16) {
            passwordLayout.setError(getString(R.string.error_password_length));
            passwordEditText.requestFocus();
            return;
        }
        if (!password.matches(PASSWORD_REGEX)) {
            passwordLayout.setError(getString(R.string.error_password_format));
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

                    int userId = SessionUtils.getUserIdFromJwt(data.getAccess());
                    Log.d(TAG, "User ID extracted from JWT in LoginActivity: " + userId);
                    if (userId != -1) {
                        SessionUtils.saveUserId(LoginActivity.this, userId);
                        Log.d(TAG, "User ID saved in SessionUtils from LoginActivity: " + SessionUtils.getUserId(LoginActivity.this));
                    } else {
                        Log.e(TAG, "No se pudo obtener el User ID del token JWT. El carrito puede no funcionar.");
                    }

                    SessionUtils.saveUserEmail(LoginActivity.this, email);

                } else {
                    usernameLayout.setError(null);
                    passwordLayout.setError(null);
                    Toast.makeText(LoginActivity.this, getString(R.string.error_invalid_credentials), Toast.LENGTH_SHORT).show();
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
        String repeatPassword = repeatPasswordEditText.getText().toString().trim(); // Obtener valor

        clearErrors();

        // Validaciones para Email
        if (TextUtils.isEmpty(email)) {
            usernameLayout.setError(getString(R.string.error_email_required));
            usernameEditText.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            usernameLayout.setError(getString(R.string.error_invalid_email));
            usernameEditText.requestFocus();
            return;
        }

        // Validaciones para Contraseña
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_password_required));
            passwordEditText.requestFocus();
            return;
        }
        if (password.length() < 8 || password.length() > 16) {
            passwordLayout.setError(getString(R.string.error_password_length));
            passwordEditText.requestFocus();
            return;
        }
        if (!password.matches(PASSWORD_REGEX)) {
            passwordLayout.setError(getString(R.string.error_password_format));
            passwordEditText.requestFocus();
            return;
        }

        // Validaciones para Repetir Contraseña
        if (TextUtils.isEmpty(repeatPassword)) {
            repeatPasswordLayout.setError(getString(R.string.error_repeat_password_required));
            repeatPasswordEditText.requestFocus();
            return;
        }
        if (!password.equals(repeatPassword)) {
            repeatPasswordLayout.setError(getString(R.string.error_password_mismatch));
            repeatPasswordEditText.requestFocus();
            return;
        }

        // Validaciones para Nombre de usuario (Registro)
        if (TextUtils.isEmpty(username)) {
            nameLayout.setError(getString(R.string.error_name_required));
            nameEditText.requestFocus();
            return;
        }
        if (username.length() < 3 || username.length() > 50) {
            nameLayout.setError(getString(R.string.error_username_length));
            nameEditText.requestFocus();
            return;
        }
        if (!username.matches(USERNAME_REGEX)) {
            nameLayout.setError(getString(R.string.error_username_format));
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
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}

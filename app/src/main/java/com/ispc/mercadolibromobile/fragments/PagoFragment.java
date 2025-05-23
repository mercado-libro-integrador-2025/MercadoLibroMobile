package com.ispc.mercadolibromobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.models.Pago;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PagoFragment extends Fragment {

    private static final String TAG = "PagoFragment";

    private EditText etNumeroTarjeta, etCVV, etVencimiento;
    private Spinner spTipoTarjeta;
    private TextView tvNumeroTarjetaMostrar, tvCVVMostrar, tvVencimientoMostrar, tvMostrarTipoTarjeta;
    private Button btnPagar;
    private SharedPreferences appPrefs;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pagar, container, false);

        etNumeroTarjeta = view.findViewById(R.id.etNumeroTarjeta);
        etCVV = view.findViewById(R.id.etCVV);
        etVencimiento = view.findViewById(R.id.etVencimiento);
        spTipoTarjeta = view.findViewById(R.id.spTipoTarjeta);

        tvNumeroTarjetaMostrar = view.findViewById(R.id.tvNumeroTarjetaMostrar);
        tvCVVMostrar = view.findViewById(R.id.tvCVVMostrar);
        tvVencimientoMostrar = view.findViewById(R.id.tvVencimientoMostrar);
        tvMostrarTipoTarjeta = view.findViewById(R.id.tvmostarTipoTarjeta);

        btnPagar = view.findViewById(R.id.btnPagar);

        // Inicializar SharedPreferences para datos de la aplicación (no de sesión)
        appPrefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        // Obtener la instancia de ApiService
        apiService = RetrofitClient.getApiService(getContext());

        // Configurar el Spinner con los tipos de tarjeta
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), // Usar requireContext()
                R.array.tipos_tarjeta_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipoTarjeta.setAdapter(adapter);

        btnPagar.setOnClickListener(v -> realizarPago());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarDatosUltimoPago();
    }
    private void realizarPago() {
        String numeroTarjeta = etNumeroTarjeta.getText().toString().trim();
        String cvv = etCVV.getText().toString().trim();
        String vencimiento = etVencimiento.getText().toString().trim();
        String tipoTarjeta = spTipoTarjeta.getSelectedItem().toString().toLowerCase(); // Obtener el tipo de tarjeta del Spinner

        // Validar los datos ingresados por el usuario
        if (!validarDatos(numeroTarjeta, cvv, vencimiento, tipoTarjeta)) {
            return; // Si la validación falla, se detiene la ejecución
        }

        // Obtener el token de acceso y el ID de usuario desde SessionUtils
        String token = SessionUtils.getAuthToken(getContext());
        int usuarioId = SessionUtils.getUserId(getContext());

        if (token != null && usuarioId != -1) {
            Pago pago = new Pago(usuarioId, numeroTarjeta, cvv, vencimiento, tipoTarjeta);

            apiService.realizarPago("Bearer " + token, pago).enqueue(new Callback<Pago>() {
                @Override
                public void onResponse(@NonNull Call<Pago> call, @NonNull Response<Pago> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Pago pagoRespuesta = response.body();
                        if (isAdded()) {
                            mostrarDetallesPago(pagoRespuesta);
                            guardarUltimoPago(pagoRespuesta);
                            Toast.makeText(getContext(), getString(R.string.success_payment_made), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error al realizar el pago. Código: " + response.code() + ", Mensaje: " + response.message());
                        try {
                            String errorMessage = response.errorBody() != null ? response.errorBody().string() : getString(R.string.error_unknown);
                            if (isAdded()) {
                                Toast.makeText(getContext(), getString(R.string.error_making_payment, errorMessage), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar el cuerpo de error", e);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Pago> call, @NonNull Throwable t) {
                    Log.e(TAG, "Fallo al realizar el pago: " + t.getMessage(), t);
                    if (isAdded()) {
                        Toast.makeText(getContext(), getString(R.string.error_network_connection_payment), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), getString(R.string.error_invalid_token_or_user_id), Toast.LENGTH_SHORT).show();
        }
    }
    private void mostrarDetallesPago(@NonNull Pago pagoRespuesta) {
        tvNumeroTarjetaMostrar.setText(getString(R.string.label_card_number_display, pagoRespuesta.getNumero_tarjeta()));
        tvCVVMostrar.setText(getString(R.string.label_cvv_display, pagoRespuesta.getCvv()));
        tvVencimientoMostrar.setText(getString(R.string.label_expiration_display, pagoRespuesta.getVencimiento()));
        tvMostrarTipoTarjeta.setText(getString(R.string.label_card_type_display, pagoRespuesta.getTipo_tarjeta()));
    }
    private void guardarUltimoPago(@NonNull Pago pagoRespuesta) {
        SharedPreferences.Editor editor = appPrefs.edit();
        editor.putString("ultimo_pago_numero_tarjeta", pagoRespuesta.getNumero_tarjeta());
        editor.putString("ultimo_pago_cvv", pagoRespuesta.getCvv());
        editor.putString("ultimo_pago_vencimiento", pagoRespuesta.getVencimiento());
        editor.putString("ultimo_pago_tipo_tarjeta", pagoRespuesta.getTipo_tarjeta());
        editor.apply(); // Guardar los cambios
    }
    private void cargarDatosUltimoPago() {
        String ultimoNumeroTarjeta = appPrefs.getString("ultimo_pago_numero_tarjeta", "");
        String ultimoCVV = appPrefs.getString("ultimo_pago_cvv", "");
        String ultimoVencimiento = appPrefs.getString("ultimo_pago_vencimiento", "");
        String ultimoTipoTarjeta = appPrefs.getString("ultimo_pago_tipo_tarjeta", "");

        etNumeroTarjeta.setText(ultimoNumeroTarjeta);
        etCVV.setText(ultimoCVV);
        etVencimiento.setText(ultimoVencimiento);
        if (!ultimoTipoTarjeta.isEmpty()) {
            tvMostrarTipoTarjeta.setText(getString(R.string.label_card_type_display, ultimoTipoTarjeta));
        } else {
            tvMostrarTipoTarjeta.setText(getString(R.string.label_card_type_display_placeholder));
        }
    }
    private boolean validarDatos(String numeroTarjeta, String cvv, String vencimiento, String tipoTarjeta) {
        if (numeroTarjeta.length() != 16 || !numeroTarjeta.matches("\\d+")) {
            etNumeroTarjeta.setError(getString(R.string.error_card_number_length));
            etNumeroTarjeta.requestFocus();
            return false;
        }
        if (cvv.length() != 3 || !cvv.matches("\\d+")) {
            etCVV.setError(getString(R.string.error_cvv_length));
            etCVV.requestFocus();
            return false;
        }
        if (!validarVencimiento(vencimiento)) {
            etVencimiento.requestFocus();
            return false;
        }
        if (!tipoTarjeta.equals("debito") && !tipoTarjeta.equals("credito")) {
            Toast.makeText(getContext(), getString(R.string.error_card_type_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validarVencimiento(String vencimiento) {
        if (vencimiento.length() != 5 || !vencimiento.matches("\\d{2}/\\d{2}")) {
            etVencimiento.setError(getString(R.string.error_expiration_format));
            return false;
        }

        // Separar el mes y el año
        String[] partes = vencimiento.split("/");
        int mes = Integer.parseInt(partes[0]);
        int anio = Integer.parseInt(partes[1]); // Obtener el año

        // Obtener el año actual (últimos dos dígitos)
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % 100;
        int currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1; // Meses de 0-11

        // Verificar que el mes esté entre 01 y 12
        if (mes < 1 || mes > 12) {
            etVencimiento.setError(getString(R.string.error_month_range));
            return false;
        }

        // Verificar que el año no sea pasado
        if (anio < currentYear || (anio == currentYear && mes < currentMonth)) {
            etVencimiento.setError(getString(R.string.error_expiration_past));
            return false;
        }
        return true;
    }
}

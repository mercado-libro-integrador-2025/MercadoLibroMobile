package com.example.mercadolibromobile.fragments;

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

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.api.PagoApi;
import com.example.mercadolibromobile.api.RetrofitClient;
import com.example.mercadolibromobile.models.Pago;
import com.example.mercadolibromobile.utils.AuthUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PagoFragment extends Fragment {

    private static final String TAG = "PagoFragment";

    private EditText etNumeroTarjeta, etCVV, etVencimiento;
    private Spinner spTipoTarjeta;
    private TextView tvNumeroTarjetaMostrar, tvCVVMostrar, tvVencimientoMostrar, tvMostrarTipoTarjeta;
    private Button btnPagar;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pagar, container, false);

        // Inicializar vistas
        etNumeroTarjeta = view.findViewById(R.id.etNumeroTarjeta);
        etCVV = view.findViewById(R.id.etCVV);
        etVencimiento = view.findViewById(R.id.etVencimiento);
        spTipoTarjeta = view.findViewById(R.id.spTipoTarjeta);

        tvNumeroTarjetaMostrar = view.findViewById(R.id.tvNumeroTarjetaMostrar);
        tvCVVMostrar = view.findViewById(R.id.tvCVVMostrar);
        tvVencimientoMostrar = view.findViewById(R.id.tvVencimientoMostrar);
        tvMostrarTipoTarjeta = view.findViewById(R.id.tvmostarTipoTarjeta);

        btnPagar = view.findViewById(R.id.btnPagar);

        // Inicializar SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);

        // Configurar el Spinner con los tipos de tarjeta
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
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

        // Validar los datos
        if (!validarDatos(numeroTarjeta, cvv, vencimiento, tipoTarjeta)) {
            return;
        }

        // Obtener el token de acceso de SharedPreferences
        String token = sharedPreferences.getString("access_token", "");

        // Extraer el usuario (ID) desde el token usando AuthUtils
        int usuario = AuthUtils.obtenerUsuarioIdDesdeToken(token);

        if (!token.isEmpty() && usuario != -1) {
            // Crear instancia del modelo Pago con el usuario
            Pago pago = new Pago(usuario, numeroTarjeta, cvv, vencimiento, tipoTarjeta);

            Retrofit retrofit = RetrofitClient.getInstance("https://backend-mercado-libro-mobile.onrender.com/api/");
            PagoApi pagoApi = retrofit.create(PagoApi.class);

            pagoApi.realizarPago("Bearer " + token, pago).enqueue(new Callback<Pago>() {
                @Override
                public void onResponse(Call<Pago> call, Response<Pago> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Pago pagoRespuesta = response.body();
                        // Mostrar detalles del pago
                        mostrarDetallesPago(pagoRespuesta);
                        // Guardar los detalles del último pago en SharedPreferences
                        guardarUltimoPago(pagoRespuesta);
                        Toast.makeText(getActivity(), "Pago realizado con éxito", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                            Log.e(TAG, "Error al realizar el pago: " + errorMessage);
                            Toast.makeText(getActivity(), "Error al realizar el pago: " + errorMessage, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar el cuerpo de error", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Pago> call, Throwable t) {
                    Toast.makeText(getActivity(), "Error de conexión", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Fallo al realizar el pago: ", t);
                }
            });
        } else {
            Toast.makeText(getActivity(), "Token o ID de usuario no válido. Por favor, inicie sesión.", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDetallesPago(Pago pagoRespuesta) {
        tvNumeroTarjetaMostrar.setText("Número de Tarjeta: " + pagoRespuesta.getNumero_tarjeta());
        tvCVVMostrar.setText("CVV: " + pagoRespuesta.getCvv());
        tvVencimientoMostrar.setText("Vencimiento: " + pagoRespuesta.getVencimiento());
        tvMostrarTipoTarjeta.setText("Tipo de Tarjeta: " + pagoRespuesta.getTipo_tarjeta());
    }

    private void guardarUltimoPago(Pago pagoRespuesta) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ultimo_pago_numero_tarjeta", pagoRespuesta.getNumero_tarjeta());
        editor.putString("ultimo_pago_cvv", pagoRespuesta.getCvv());
        editor.putString("ultimo_pago_vencimiento", pagoRespuesta.getVencimiento());
        editor.putString("ultimo_pago_tipo_tarjeta", pagoRespuesta.getTipo_tarjeta());
        editor.apply(); // Guardar los cambios
    }

    private void cargarDatosUltimoPago() {
        String ultimoNumeroTarjeta = sharedPreferences.getString("ultimo_pago_numero_tarjeta", "");
        String ultimoCVV = sharedPreferences.getString("ultimo_pago_cvv", "");
        String ultimoVencimiento = sharedPreferences.getString("ultimo_pago_vencimiento", "");
        String ultimoTipoTarjeta = sharedPreferences.getString("ultimo_pago_tipo_tarjeta", "");

        // Cargar los datos en los EditText y TextView
        etNumeroTarjeta.setText(ultimoNumeroTarjeta);
        etCVV.setText(ultimoCVV);
        etVencimiento.setText(ultimoVencimiento);
        tvMostrarTipoTarjeta.setText("Tipo de Tarjeta: " + ultimoTipoTarjeta);
    }

    private boolean validarDatos(String numeroTarjeta, String cvv, String vencimiento, String tipoTarjeta) {
        if (numeroTarjeta.length() != 16 || !numeroTarjeta.matches("\\d+")) {
            Toast.makeText(getActivity(), "El número de tarjeta debe tener 16 dígitos.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (cvv.length() != 3 || !cvv.matches("\\d+")) {
            Toast.makeText(getActivity(), "El CVV debe tener 3 dígitos.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!validarVencimiento(vencimiento)) {
            return false;
        }
        if (!tipoTarjeta.equals("debito") && !tipoTarjeta.equals("credito")) {
            Toast.makeText(getActivity(), "El tipo de tarjeta debe ser 'debito' o 'credito'.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validarVencimiento(String vencimiento) {
        if (vencimiento.length() != 5 || !vencimiento.matches("\\d{2}/\\d{2}")) {
            Toast.makeText(getActivity(), "El vencimiento debe estar en formato mm/aa y no puede tener más de 5 caracteres.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Separar el mes y el año
        String[] partes = vencimiento.split("/");
        int mes = Integer.parseInt(partes[0]);

        // Verificar que el mes esté entre 01 y 12
        if (mes < 1 || mes > 12) {
            Toast.makeText(getActivity(), "El mes debe estar entre 01 y 12.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}

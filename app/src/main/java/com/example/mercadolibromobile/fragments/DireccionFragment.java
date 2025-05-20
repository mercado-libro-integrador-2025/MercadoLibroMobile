package com.example.mercadolibromobile.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log; // Agregado para depuración
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.api.DireccionApi;
import com.example.mercadolibromobile.api.RetrofitClient;
import com.example.mercadolibromobile.models.Direccion;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DireccionFragment extends Fragment {

    private static final String TAG = "DireccionFragment"; // Etiqueta para los logs

    private EditText etCalle, etNumero, etCiudad, etProvincia;
    private TextView tvCalleIngresada, tvNumeroIngresado, tvCiudadIngresada, tvProvinciaIngresada;
    private Button btnIrAlPago;
    private SharedPreferences sharedPreferences;
    private boolean direccionGuardada = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_direccion, container, false);

        // Inicializar vistas
        etCalle = view.findViewById(R.id.etCalle);
        etNumero = view.findViewById(R.id.etNumero);
        etCiudad = view.findViewById(R.id.etCiudad);
        etProvincia = view.findViewById(R.id.etProvincia);

        tvCalleIngresada = view.findViewById(R.id.tvCalleIngresada);
        tvNumeroIngresado = view.findViewById(R.id.tvNumeroIngresado);
        tvCiudadIngresada = view.findViewById(R.id.tvCiudadIngresada);
        tvProvinciaIngresada = view.findViewById(R.id.tvProvinciaIngresada);

        btnIrAlPago = view.findViewById(R.id.btnIrAlPago);

        // Inicializar SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);

        // botón para guardar la dirección o ir al pago
        btnIrAlPago.setOnClickListener(v -> {
            if (!direccionGuardada) {
                guardarDireccion();  // Intercambio entre dirección y pago cuando se guarden los datos
            } else {
                irAPagoFragment();
            }
        });

        // Cargar las direcciones al iniciar el fragmento
        cargarDirecciones();

        return view;
    }

    private void guardarDireccion() {
        // Obtener el token de SharedPreferences
        String token = "Bearer " + sharedPreferences.getString("access_token", null);
        Log.d(TAG, "Guardando dirección con token: " + token);

        if (token != null) {
            Retrofit retrofit = RetrofitClient.getInstance("https://backend-mercado-libro-mobile.onrender.com/api/");
            DireccionApi direccionApi = retrofit.create(DireccionApi.class);

            Direccion nuevaDireccion = new Direccion(
                    0,
                    0,
                    etCalle.getText().toString(),
                    etNumero.getText().toString(),
                    etCiudad.getText().toString(),
                    etProvincia.getText().toString()
            );

            // Realizar el POST para guardar la dirección
            direccionApi.createDireccion(token, nuevaDireccion).enqueue(new Callback<Direccion>() {
                @Override
                public void onResponse(Call<Direccion> call, Response<Direccion> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Guardar la dirección en SharedPreferences
                        guardarDireccionEnSharedPreferences(response.body());

                        Toast.makeText(getActivity(), "Dirección guardada exitosamente", Toast.LENGTH_SHORT).show();
                        direccionGuardada = true;  // Cambia el estado
                        btnIrAlPago.setText("Ir al Pago");  // Cambia el texto del botón
                        cargarDirecciones();  // Llama al GET después de guardar
                    } else {
                        Toast.makeText(getActivity(), "Error al guardar dirección", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al guardar dirección: " + response.errorBody());
                    }
                }

                @Override
                public void onFailure(Call<Direccion> call, Throwable t) {
                    Toast.makeText(getActivity(), "Error de conexión al guardar dirección", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Fallo al guardar dirección: ", t);
                }
            });
        } else {
            Toast.makeText(getActivity(), "Token no encontrado, por favor inicie sesión.", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarDireccionEnSharedPreferences(Direccion direccion) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("direccion_calle", direccion.getCalle());
        editor.putString("direccion_numero", direccion.getNumero());
        editor.putString("direccion_ciudad", direccion.getCiudad());
        editor.putString("direccion_provincia", direccion.getProvincia());
        editor.apply();
    }

    private void cargarDirecciones() {
        // Obtener la dirección guardada de SharedPreferences
        String calle = sharedPreferences.getString("direccion_calle", "No hay dirección guardada");
        String numero = sharedPreferences.getString("direccion_numero", "No hay dirección guardada");
        String ciudad = sharedPreferences.getString("direccion_ciudad", "No hay dirección guardada");
        String provincia = sharedPreferences.getString("direccion_provincia", "No hay dirección guardada");

        // Mostrar los datos en los TextViews
        tvCalleIngresada.setText("Calle ingresada: " + calle);
        tvNumeroIngresado.setText("Número ingresado: " + numero);
        tvCiudadIngresada.setText("Ciudad ingresada: " + ciudad);
        tvProvinciaIngresada.setText("Provincia ingresada: " + provincia);

        Log.d(TAG, "Dirección cargada desde SharedPreferences: Calle=" + calle + ", Número=" + numero + ", Ciudad=" + ciudad + ", Provincia=" + provincia);
    }

    private void irAPagoFragment() {
        // PagoFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new PagoFragment());
        transaction.addToBackStack(null);  // Agregar a la pila de retroceso
        transaction.commit();
    }
}
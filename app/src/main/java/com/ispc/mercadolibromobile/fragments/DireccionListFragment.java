package com.ispc.mercadolibromobile.fragments;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentManager;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.adapters.DireccionAdapter;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.models.Direccion;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DireccionListFragment extends Fragment implements DireccionAdapter.OnDireccionInteractionListener, DireccionFormFragment.OnDireccionSavedListener {
    private static final String TAG = "DireccionListFragment";
    public static final String IS_SELECTION_MODE = "is_selection_mode";
    private RecyclerView recyclerViewDirecciones;
    private TextView tvNoDireccionesMessage;
    private Button btnAgregarNuevaDireccion;
    private List<Direccion> direcciones;
    private DireccionAdapter adapter;
    private ApiService apiService;
    private boolean isSelectionMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_direccion_list, container, false);

        recyclerViewDirecciones = view.findViewById(R.id.recyclerViewDirecciones);
        tvNoDireccionesMessage = view.findViewById(R.id.tvNoDireccionesMessage);
        btnAgregarNuevaDireccion = view.findViewById(R.id.btnAgregarNuevaDireccion);

        direcciones = new ArrayList<>();
        adapter = new DireccionAdapter(direcciones, this);
        recyclerViewDirecciones.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDirecciones.setAdapter(adapter);

        apiService = RetrofitClient.getApiService(getContext());

        if (getArguments() != null) {
            isSelectionMode = getArguments().getBoolean(IS_SELECTION_MODE, false);
            Log.d(TAG, "isSelectionMode: " + isSelectionMode);
        }

        btnAgregarNuevaDireccion.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, DireccionFormFragment.newInstance(null)) // Pasa null para crear una nueva
                    .addToBackStack(null)
                    .commit();
        });

        getParentFragmentManager().setFragmentResultListener("requestKeyDireccionSaved", this, (requestKey, bundle) -> {
            Direccion savedDireccion = (Direccion) bundle.getSerializable("saved_direccion");
            if (savedDireccion != null) {
                Toast.makeText(getContext(), "Dirección guardada/actualizada con éxito. Refrescando lista.", Toast.LENGTH_SHORT).show();
                obtenerDireccionesUsuario();
            }
        });


        obtenerDireccionesUsuario();

        return view;
    }

    private void obtenerDireccionesUsuario() {
        String token = SessionUtils.getAuthToken(getContext());
        if (token == null) {
            Toast.makeText(getContext(), "No hay sesión activa.", Toast.LENGTH_SHORT).show();
            tvNoDireccionesMessage.setText("Por favor, inicia sesión para ver tus direcciones.");
            tvNoDireccionesMessage.setVisibility(View.VISIBLE);
            recyclerViewDirecciones.setVisibility(View.GONE);
            return;
        }

        apiService.getDirecciones("Bearer " + token).enqueue(new Callback<List<Direccion>>() {
            @Override
            public void onResponse(@NonNull Call<List<Direccion>> call, @NonNull Response<List<Direccion>> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        direcciones.clear();
                        direcciones.addAll(response.body());
                        adapter.notifyDataSetChanged();
                        if (direcciones.isEmpty()) {
                            tvNoDireccionesMessage.setText("No tienes direcciones registradas. ¡Agrega una!");
                            tvNoDireccionesMessage.setVisibility(View.VISIBLE);
                            recyclerViewDirecciones.setVisibility(View.GONE);
                        } else {
                            tvNoDireccionesMessage.setVisibility(View.GONE);
                            recyclerViewDirecciones.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Error al cargar direcciones: " + response.code() + ", " + response.message());
                        tvNoDireccionesMessage.setText("Error al cargar direcciones. Intenta de nuevo.");
                        tvNoDireccionesMessage.setVisibility(View.VISIBLE);
                        recyclerViewDirecciones.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error al cargar direcciones: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Direccion>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "Fallo de conexión al cargar direcciones: " + t.getMessage(), t);
                    tvNoDireccionesMessage.setText("Error de conexión al cargar direcciones.");
                    tvNoDireccionesMessage.setVisibility(View.VISIBLE);
                    recyclerViewDirecciones.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error de conexión al cargar direcciones.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDireccionSaved(Direccion direccion) {
        Log.d(TAG, "Dirección guardada/actualizada en DireccionListFragment a través de OnDireccionSavedListener: " + direccion.getFullAddress());
    }

    @Override
    public void onDireccionClick(Direccion direccion) {
        if (isSelectionMode) {
            Bundle result = new Bundle();
            result.putSerializable(CarritoFragment.KEY_SELECTED_ADDRESS, direccion);
            getParentFragmentManager().setFragmentResult("requestKeyAddressSelection", result);
            getParentFragmentManager().popBackStack();
        } else {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, DireccionFormFragment.newInstance(direccion)) // Pasa la dirección para editar
                    .addToBackStack(null)
                    .commit();
        }
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
        String token = SessionUtils.getAuthToken(getContext());
        if (token == null) {
            Toast.makeText(getContext(), "No hay sesión activa para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar esta dirección?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    apiService.deleteDireccion(direccionId, "Bearer " + token).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            if (isAdded()) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(getContext(), "Dirección eliminada.", Toast.LENGTH_SHORT).show();
                                    obtenerDireccionesUsuario(); // Recargar la lista
                                } else {
                                    Log.e(TAG, "Error al eliminar dirección: " + response.code() + " - " + response.message());
                                    Toast.makeText(getContext(), "Error al eliminar dirección: " + response.message(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            if (isAdded()) {
                                Log.e(TAG, "Fallo de red al eliminar dirección: " + t.getMessage(), t);
                                Toast.makeText(getContext(), "Error de conexión al eliminar dirección.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}
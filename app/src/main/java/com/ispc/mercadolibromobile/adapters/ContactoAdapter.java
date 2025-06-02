package com.ispc.mercadolibromobile.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.fragments.ContactFragment;
import com.ispc.mercadolibromobile.models.Contacto;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ContactoViewHolder> {

    private final Context context;
    private final List<Contacto> contactos = new ArrayList<>();
    private final List<Contacto> contactosOriginales = new ArrayList<>();
    private final String emailUsuario;
    private final ApiService apiService;
    private final ProgressBar progressBar;

    public ContactoAdapter(Context context, View view) {
        this.context = context;
        this.emailUsuario = SessionUtils.getUserEmail(context);
        this.apiService = RetrofitClient.getApiService(context);
        this.progressBar = view.findViewById(R.id.progressBar);

        // Carga inicial
        loadConsultas();
    }

    @NonNull
    @Override
    public ContactoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false);
        return new ContactoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactoViewHolder holder, int position) {
        Contacto contacto = contactos.get(position);

        holder.tvAsunto.setText(contacto.getAsunto());
        holder.tvMensaje.setText(contacto.getMensaje());

        if (contacto.getEmail().equals(emailUsuario)) {
            holder.btnAcciones.setVisibility(View.VISIBLE);

            holder.btnVer.setOnClickListener(v -> abrirContactoFragment(contacto, "ver"));
            holder.btnEditar.setOnClickListener(v -> abrirContactoFragment(contacto, "editar"));

            holder.btnBorrar.setOnClickListener(v -> borrarContacto(contacto));
        } else {
            holder.btnAcciones.setVisibility(View.GONE);
        }
    }

    private void borrarContacto(Contacto contacto) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.borrarConsulta(contacto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                // Recargar lista desde servidor para mantener consistencia
                loadConsultas();
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error al eliminar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Fallo al eliminar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirContactoFragment(Contacto contacto, String modo) {
        Fragment fragment = new ContactFragment();

        Bundle args = new Bundle();
        args.putString("modo", modo);
        args.putString("asunto", contacto.getAsunto());
        args.putString("mensaje", contacto.getMensaje());
        fragment.setArguments(args);

        ((AppCompatActivity) context).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public int getItemCount() {
        return contactos.size();
    }

    public static class ContactoViewHolder extends RecyclerView.ViewHolder {
        TextView tvAsunto, tvMensaje;
        LinearLayout btnAcciones;
        Button btnVer, btnEditar, btnBorrar;

        public ContactoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAsunto = itemView.findViewById(R.id.tvAsunto);
            tvMensaje = itemView.findViewById(R.id.tvMensaje);
            btnAcciones = itemView.findViewById(R.id.btnAcciones);
            btnVer = itemView.findViewById(R.id.btnVer);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnBorrar = itemView.findViewById(R.id.btnBorrar);
        }
    }

    // 🔍 Filtro
    public void filtrar(String texto) {
        contactos.clear();
        if (texto.isEmpty()) {
            contactos.addAll(contactosOriginales);
        } else {
            String filtro = texto.toLowerCase();
            for (Contacto c : contactosOriginales) {
                if (c.getAsunto().toLowerCase().contains(filtro) || c.getMensaje().toLowerCase().contains(filtro)) {
                    contactos.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    // 🔄 Recarga de contactos desde API
    public void loadConsultas() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.obtenerConsultas().enqueue(new Callback<List<Contacto>>() {
            @Override
            public void onResponse(@NonNull Call<List<Contacto>> call, @NonNull Response<List<Contacto>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    contactos.clear();
                    contactos.addAll(response.body());

                    contactosOriginales.clear();
                    contactosOriginales.addAll(response.body());

                    notifyDataSetChanged();
                } else {
                    Log.e("API", "Error al obtener contactos: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Contacto>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show();
                Log.e("API", "Fallo: " + t.getMessage());
            }
        });
    }
}

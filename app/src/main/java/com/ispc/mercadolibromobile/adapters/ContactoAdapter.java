package com.ispc.mercadolibromobile.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.databinding.ItemContactoBinding;
import com.ispc.mercadolibromobile.fragments.ContactFragment;
import com.ispc.mercadolibromobile.models.Contacto;

import java.util.ArrayList;
import java.util.List;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ContactoViewHolder> {

    private final Context context;
    private final List<Contacto> contactos = new ArrayList<>();
    private final List<Contacto> contactosOriginales = new ArrayList<>(); // Lista para mantener todos los contactos sin filtrar
    private final ApiService apiService;
    private final ProgressBar progressBar; // El ProgressBar ahora es opcional si el DialogFragment lo maneja

    public static final String PREFS_NAME = "MyPrefs"; // Nombre para tus SharedPreferences
    public static final String LAST_FILTERED_ASUNTO_KEY = "lastFilteredAsunto"; // Clave para guardar el asunto

    public ContactoAdapter(Context context, View view) { // Se asume que 'view' es la vista raíz donde buscar el ProgressBar
        this.context = context;
        this.apiService = RetrofitClient.getApiService(context);
        this.progressBar = view.findViewById(R.id.progressBar);

        loadConsultas();
    }

    @NonNull
    @Override
    public ContactoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout del item de contacto
        View view = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false);
        return new ContactoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactoViewHolder holder, int position) {
        Contacto contacto = contactos.get(position);

        holder.binding.tvAsunto.setText(contacto.getAsunto());
        holder.binding.tvMensaje.setText(contacto.getMensaje());
        holder.binding.tvNombre.setText(contacto.getNombre()); // Asegúrate de mostrar el nombre también

        // Lógica para mostrar/ocultar botones de acción y alinear texto
        if (contacto.getNombre().equalsIgnoreCase(SessionUtils.getUserName(context))) {
            //holder.binding.btnAcciones.setVisibility(View.VISIBLE);

            //holder.binding.btnVer.setOnClickListener(v -> abrirContactoFragment(contacto, "ver"));
            //holder.binding.btnEditar.setOnClickListener(v -> abrirContactoFragment(contacto, "editar"));
            //holder.binding.btnBorrar.setOnClickListener(v -> borrarContacto(contacto));

            // Alineación para mensajes propios
            holder.binding.tvNombre.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            holder.binding.tvAsunto.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            holder.binding.tvMensaje.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        } else {
            //holder.binding.btnAcciones.setVisibility(View.GONE);

            // Alineación para mensajes recibidos
            holder.binding.tvNombre.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            holder.binding.tvAsunto.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            holder.binding.tvMensaje.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
    }

    @Override
    public int getItemCount() {
        return contactos.size();
    }

    // --- ViewHolder ---
    public static class ContactoViewHolder extends RecyclerView.ViewHolder {
        ItemContactoBinding binding;
        public ContactoViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemContactoBinding.bind(itemView);
        }
    }

    // --- Métodos del Adaptador ---

    public void updateContacts(List<Contacto> newContacts) {
        contactos.clear();
        contactos.addAll(newContacts);
        contactosOriginales.clear(); // Limpiamos también la lista original
        contactosOriginales.addAll(newContacts); // Y la llenamos con los nuevos contactos (ya filtrados si es el caso)
        notifyDataSetChanged(); // Notificamos al RecyclerView que los datos han cambiado
    }

    public void filtrar(String texto) {
        contactos.clear();
        if (texto.isEmpty()) {
            contactos.addAll(contactosOriginales);
        } else {
            String filtro = texto.toLowerCase();
            for (Contacto c : contactosOriginales) {
                // Se verifica que asunto y mensaje no sean nulos antes de convertirlos a minúsculas
                boolean asuntoMatch = c.getAsunto() != null && c.getAsunto().toLowerCase().contains(filtro);
                boolean mensajeMatch = c.getMensaje() != null && c.getMensaje().toLowerCase().contains(filtro);

                if (asuntoMatch || mensajeMatch) {
                    contactos.add(c);
                }
            }
        }
        notifyDataSetChanged();

        // 💾 Guarda el asunto del último contacto filtrado en SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!contactos.isEmpty()) {
            String lastAsunto = contactos.get(contactos.size() - 1).getAsunto();
            editor.putString(LAST_FILTERED_ASUNTO_KEY, lastAsunto);
            Log.d("ContactoAdapter", "Guardado en SharedPreferences: " + lastAsunto);
        } else {
            // Si la lista está vacía, guarda una cadena vacía o elimina la clave
            editor.putString(LAST_FILTERED_ASUNTO_KEY, ""); // O editor.remove(LAST_FILTERED_ASUNTO_KEY);
            Log.d("ContactoAdapter", "Lista de contactos filtrada vacía. Guardado: ''");
        }
        editor.apply(); // Aplica los cambios de forma asíncrona
    }

    public void loadConsultas() {
        if (progressBar != null) { // Verifica si el ProgressBar ha sido inicializado
            progressBar.setVisibility(View.VISIBLE);
        }
        apiService.obtenerConsultas().enqueue(new Callback<List<Contacto>>() {
            @Override
            public void onResponse(@NonNull Call<List<Contacto>> call, @NonNull Response<List<Contacto>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (response.isSuccessful() && response.body() != null) {
                    String userEmail = SessionUtils.getUserEmail(context);
                    List<Contacto> filteredContacts = new ArrayList<>();

                    for (Contacto contacto : response.body()) {
                        // Filtra solo los contactos cuyo email coincida con el del usuario logueado
                        // Usar equalsIgnoreCase para comparar emails sin importar mayúsculas/minúsculas
                        if (contacto.getEmail() != null && contacto.getEmail().equalsIgnoreCase(userEmail)) {
                            filteredContacts.add(contacto);
                        }
                    }

                    // 💾 Guarda el asunto del último contacto filtrado en SharedPreferences
                    SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (!filteredContacts.isEmpty()) {
                        String lastAsunto = filteredContacts.get(filteredContacts.size() - 1).getAsunto();
                        editor.putString(LAST_FILTERED_ASUNTO_KEY, lastAsunto);
                        Log.d("ContactoAdapter", "Guardado en SharedPreferences: " + lastAsunto);
                    } else {
                        // Si la lista está vacía, guarda una cadena vacía o elimina la clave
                        editor.putString(LAST_FILTERED_ASUNTO_KEY, ""); // O editor.remove(LAST_FILTERED_ASUNTO_KEY);
                        Log.d("ContactoAdapter", "Lista de contactos filtrada vacía. Guardado: ''");
                    }
                    editor.apply(); // Aplica los cambios de forma asíncrona

                    // Usa el nuevo método updateContacts para actualizar ambas listas
                    updateContacts(filteredContacts);
                } else {
                    Log.e("ContactoAdapter", "Error al obtener contactos: " + response.code() + " - " + response.message());
                    Toast.makeText(context, "Error al cargar contactos: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Contacto>> call, @NonNull Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Toast.makeText(context, "Error de conexión al cargar contactos", Toast.LENGTH_SHORT).show();
                Log.e("ContactoAdapter", "Fallo en la conexión: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Envía una solicitud a la API para borrar un contacto y recarga la lista.
     * @param contacto El objeto Contacto a borrar.
     */
    private void borrarContacto(Contacto contacto) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        apiService.borrarConsulta(contacto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Contacto eliminado correctamente", Toast.LENGTH_SHORT).show();
                    loadConsultas(); // Recargar la lista para reflejar el cambio
                } else {
                    Log.e("ContactoAdapter", "Error al eliminar contacto: " + response.code() + " - " + response.message());
                    Toast.makeText(context, "Error al eliminar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Toast.makeText(context, "Fallo en la conexión al eliminar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ContactoAdapter", "Fallo al eliminar contacto: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Abre el fragmento ContactFragment para ver o editar un contacto.
     * @param contacto El objeto Contacto a pasar.
     * @param modo El modo de operación ("ver" o "editar").
     */
    private void abrirContactoFragment(Contacto contacto, String modo) {
        Fragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putString("modo", modo);
        args.putString("asunto", contacto.getAsunto());
        args.putString("mensaje", contacto.getMensaje());
        // También podrías pasar el ID del contacto si lo necesitas para editar/borrar
        // args.putInt("idContacto", contacto.getId());
        fragment.setArguments(args);

        // Asegúrate de que el contexto es una AppCompatActivity para obtener el FragmentManager
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null) // Permite volver al fragmento anterior con el botón de atrás
                    .commit();
        } else {
            Log.e("ContactoAdapter", "Contexto no es una AppCompatActivity. No se puede abrir ContactFragment.");
            Toast.makeText(context, "Error interno al abrir contacto.", Toast.LENGTH_SHORT).show();
        }
    }
}
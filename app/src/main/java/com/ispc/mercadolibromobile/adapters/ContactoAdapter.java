package com.ispc.mercadolibromobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.models.Contacto;

import java.util.ArrayList;
import java.util.List;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ContactoViewHolder> {

    private final Context context;
    private final List<Contacto> contactos = new ArrayList<>();
    private final ApiService apiService;

    public ContactoAdapter(Context context, View view) {
        this.context = context;
        this.apiService = RetrofitClient.getApiService(context);
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
    }

    @Override
    public int getItemCount() {
        return contactos.size();
    }

    public void setContactos(List<Contacto> newContactos) {
        contactos.clear();
        contactos.addAll(newContactos);
        notifyDataSetChanged();
    }

    public static class ContactoViewHolder extends RecyclerView.ViewHolder {
        TextView tvAsunto, tvMensaje;

        public ContactoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAsunto = itemView.findViewById(R.id.tvAsunto);
            tvMensaje = itemView.findViewById(R.id.tvMensaje);
        }
    }
}
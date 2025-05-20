package com.example.mercadolibromobile.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.models.Resena;

import java.util.List;

public class ResenaAdapter extends RecyclerView.Adapter<ResenaAdapter.ResenaViewHolder> {

    private List<Resena> resenas;
    private OnResenaInteractionListener interactionListener; // Listener de interacción

    // Constructor que recibe la lista de reseñas y el listener de interacción
    public ResenaAdapter(List<Resena> resenas, OnResenaInteractionListener interactionListener) {
        this.resenas = resenas;
        this.interactionListener = interactionListener; // Inicializar el listener de interacción
    }

    @NonNull
    @Override
    public ResenaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resena, parent, false);
        return new ResenaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResenaViewHolder holder, int position) {
        Resena resena = resenas.get(position);
        holder.commentTextView.setText(resena.getComentario());
        holder.dateTextView.setText(resena.getFechaCreacion());
        holder.libroTextView.setText("Título: " + resena.getTituloLibro());
        holder.usuarioTextView.setText("Usuario: " + resena.getEmailUsuario());

        // Configurar el listener para el botón de eliminar con confirmación
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirmación de Eliminación")
                    .setMessage("¿Estás seguro de que deseas eliminar esta reseña?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        eliminarResena(position);  // Llamar al método de eliminación
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Configurar el listener para el botón de editar
        holder.editButton.setOnClickListener(v -> {
            if (interactionListener != null) {
                interactionListener.onResenaEdit(resena); // Llamar al método de edición en la actividad
            }
        });
    }

    @Override
    public int getItemCount() {
        return resenas.size();
    }

    // Método para eliminar la reseña y llamar al listener
    public void eliminarResena(int position) {
        Resena resenaEliminada = resenas.get(position);
        resenas.remove(position);  // Eliminar la reseña de la lista
        notifyItemRemoved(position);  // Notificar el cambio en el adaptador

        // Llamar al listener de eliminación para manejar la acción en la actividad
        if (interactionListener != null) {
            interactionListener.onResenaDelete(resenaEliminada);
        }
    }

    // Método para actualizar la lista de reseñas
    public void updateResenas(List<Resena> nuevasResenas) {
        this.resenas.clear();
        this.resenas.addAll(nuevasResenas);
        notifyDataSetChanged();
    }

    // Interfaz para notificar las acciones de eliminar y editar de una reseña
    public interface OnResenaInteractionListener {
        void onResenaDelete(Resena resena);
        void onResenaEdit(Resena resena);
    }

    static class ResenaViewHolder extends RecyclerView.ViewHolder {
        TextView commentTextView, dateTextView, libroTextView, usuarioTextView;
        ImageView deleteButton, editButton; // Botones de eliminar y editar

        ResenaViewHolder(@NonNull View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.comentarioTextView);
            dateTextView = itemView.findViewById(R.id.fechaTextView);
            libroTextView = itemView.findViewById(R.id.libroTextView);
            usuarioTextView = itemView.findViewById(R.id.usuarioTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton); // Inicializar el botón de eliminar
            editButton = itemView.findViewById(R.id.editButton); // Inicializar el botón de editar
        }
    }
}

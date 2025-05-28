package com.ispc.mercadolibromobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.models.Direccion;

import java.util.List;

public class DireccionAdapter extends RecyclerView.Adapter<DireccionAdapter.DireccionViewHolder> {

    private List<Direccion> direcciones;
    private OnDireccionInteractionListener listener;

    public interface OnDireccionInteractionListener {
        void onDireccionClick(Direccion direccion);
        void onEditDireccion(Direccion direccion);
        void onDeleteDireccion(int direccionId);
    }

    public DireccionAdapter(List<Direccion> direcciones, OnDireccionInteractionListener listener) {
        this.direcciones = direcciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DireccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_direccion, parent, false);
        return new DireccionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DireccionViewHolder holder, int position) {
        Direccion direccion = direcciones.get(position);
        holder.bind(direccion, listener);
    }

    @Override
    public int getItemCount() {
        return direcciones.size();
    }

    public static class DireccionViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullAddress;
        Button btnEdit;
        Button btnDelete;

        public DireccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullAddress = itemView.findViewById(R.id.tvAddressDetails);
            btnEdit = itemView.findViewById(R.id.btnEditAddress);
            btnDelete = itemView.findViewById(R.id.btnDeleteAddress);
        }

        public void bind(final Direccion direccion, final OnDireccionInteractionListener listener) {
            tvFullAddress.setText(direccion.getFullAddress());

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onDireccionClick(direccion));

                if (btnEdit != null) {
                    btnEdit.setOnClickListener(v -> listener.onEditDireccion(direccion));
                    btnEdit.setVisibility(View.VISIBLE);
                } else {

                }

                if (btnDelete != null) {
                    btnDelete.setOnClickListener(v -> listener.onDeleteDireccion(direccion.getId()));
                    btnDelete.setVisibility(View.VISIBLE);
                } else {
                }
            } else {
                itemView.setOnClickListener(null);
                if (btnEdit != null) btnEdit.setVisibility(View.GONE);
                if (btnDelete != null) btnDelete.setVisibility(View.GONE);
            }
        }
    }
}
package com.ispc.mercadolibromobile.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.models.Pedido;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {
    private final List<Pedido> pedidoList;

    public PedidoAdapter(List<Pedido> pedidoList) {
        this.pedidoList = pedidoList;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidoList.get(position);

        if (holder.idPedidoTextView != null) {
            holder.idPedidoTextView.setText("Número de pedido: " + pedido.getId());
        } else {
            Log.e("PedidoAdapter", "idPedidoTextView is null for item at position: " + position);
        }

        if (holder.direccionTextView != null) {
            if (pedido.getDireccion() != null) {
                holder.direccionTextView.setText("Dirección: " + pedido.getDireccion().getCalle() + ", " +
                        pedido.getDireccion().getCiudad() + ", " +
                        pedido.getDireccion().getProvincia());
            } else {
                holder.direccionTextView.setText("Dirección: No especificada");
            }
        }

        if (holder.fechaPedidoTextView != null) {
            String fechaOriginal = pedido.getFecha_pedido();
            if (fechaOriginal != null && !fechaOriginal.isEmpty()) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date date = inputFormat.parse(fechaOriginal.substring(0, 19));

                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String formattedDate = outputFormat.format(date);
                    holder.fechaPedidoTextView.setText("Fecha: " + formattedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    holder.fechaPedidoTextView.setText(R.string.review_item_date_placeholder);
                }
            } else {
                holder.fechaPedidoTextView.setText(R.string.review_item_date_placeholder);
            }
        }


        if (holder.totalTextView != null) {
            holder.totalTextView.setText(String.format(Locale.getDefault(), "Total: $%.2f", pedido.getTotal()));
        }

        if (holder.estadoTextView != null) {
            String estado = pedido.getEstado();
            if (estado != null && !estado.isEmpty()) {
                holder.estadoTextView.setText("Estado: " + estado);
            } else {
                holder.estadoTextView.setText("Estado: Desconocido");
            }
        } else {
            Log.e("PedidoAdapter", "estadoTextView is null for item at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return pedidoList.size();
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        public TextView idPedidoTextView;
        public TextView direccionTextView;
        public TextView estadoTextView;
        public TextView fechaPedidoTextView;
        public TextView totalTextView;

        public PedidoViewHolder(View view) {
            super(view);
            idPedidoTextView = view.findViewById(R.id.idPedido);
            direccionTextView = view.findViewById(R.id.direccion);
            estadoTextView = view.findViewById(R.id.estado);
            fechaPedidoTextView = view.findViewById(R.id.fechaPedido);
            totalTextView = view.findViewById(R.id.total);
        }
    }
}
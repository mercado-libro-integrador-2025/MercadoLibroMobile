package com.example.mercadolibromobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.api.PedidoApi;
import com.example.mercadolibromobile.models.Pedido;
import java.util.List;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {
    private List<Pedido> pedidoList;

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
        holder.idPedidoTextView.setText(String.valueOf(pedido.getId_pedido()));
        holder.usuarioTextView.setText(pedido.getUsuario());
        holder.direccionTextView.setText(pedido.getDireccion().getCalle() + ", " + pedido.getDireccion().getCiudad());
        holder.metodoPagoTextView.setText(pedido.getMetodo_pago());
        holder.estadoTextView.setText(pedido.getEstado());
        holder.fechaPedidoTextView.setText(pedido.getFecha_pedido());
        holder.totalTextView.setText(String.valueOf(pedido.getTotal()));
    }

    @Override
    public int getItemCount() {
        return pedidoList.size();
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        public TextView idPedidoTextView;
        public TextView usuarioTextView;
        public TextView direccionTextView;
        public TextView metodoPagoTextView;
        public TextView estadoTextView;
        public TextView fechaPedidoTextView;
        public TextView totalTextView;

        public PedidoViewHolder(View view) {
            super(view);
            idPedidoTextView = view.findViewById(R.id.idPedido);
            usuarioTextView = view.findViewById(R.id.usuario);
            direccionTextView = view.findViewById(R.id.direccion);
            metodoPagoTextView = view.findViewById(R.id.metodoPago);
            estadoTextView = view.findViewById(R.id.estado);
            fechaPedidoTextView = view.findViewById(R.id.fechaPedido);
            totalTextView = view.findViewById(R.id.total);
        }
    }
}
package com.example.mercadolibromobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mercadolibromobile.R;
import com.example.mercadolibromobile.fragments.CarritoFragment;
import com.example.mercadolibromobile.models.ItemCarrito;

import java.util.List;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

    private List<ItemCarrito> itemsCarrito;
    private Context context;
    private CarritoListener carritoListener;

    public CarritoAdapter(List<ItemCarrito> itemsCarrito, Context context, CarritoListener carritoListener) {
        this.itemsCarrito = itemsCarrito;
        this.context = context;
        this.carritoListener = carritoListener;
    }

    @NonNull
    @Override
    public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carrito, parent, false);
        return new CarritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
        ItemCarrito item = itemsCarrito.get(position);

        holder.tvTituloLibro.setText(item.getTituloLibro());
        holder.tvCantidad.setText("Cantidad: " + item.getCantidad());
        holder.tvPrecio.setText("Precio: $" + item.getPrecioUnitario());

        holder.btnAumentarCantidad.setOnClickListener(v -> {
            carritoListener.aumentarCantidad(item);
        });

        holder.btnDisminuirCantidad.setOnClickListener(v -> {
            carritoListener.disminuirCantidad(item);
        });

        holder.btnEliminar.setOnClickListener(v -> {
            carritoListener.eliminarItem(item);
        });
    }

    @Override
    public int getItemCount() {
        return itemsCarrito.size();
    }

    public static class CarritoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloLibro, tvCantidad, tvPrecio;
        ImageButton btnAumentarCantidad, btnDisminuirCantidad, btnEliminar;

        public CarritoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloLibro = itemView.findViewById(R.id.tvTituloLibro);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            btnAumentarCantidad = itemView.findViewById(R.id.btnAumentarCantidad);
            btnDisminuirCantidad = itemView.findViewById(R.id.btnDisminuirCantidad);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }

    public interface CarritoListener {
        void aumentarCantidad(ItemCarrito item);
        void disminuirCantidad(ItemCarrito item);
        void eliminarItem(ItemCarrito item);
    }
}

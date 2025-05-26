package com.ispc.mercadolibromobile.adapters;

import android.content.Context;
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

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private final List<Direccion> addressList;
    private final Context context;
    private final OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onEditAddress(Direccion direccion);
        void onDeleteAddress(int direccionId);
    }

    public AddressAdapter(List<Direccion> addressList, Context context, OnAddressActionListener listener) {
        this.addressList = addressList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_direccion, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Direccion direccion = addressList.get(position);

        String fullAddress = context.getString(R.string.address_placeholder_format,
                direccion.getCalle(),
                direccion.getNumero(),
                direccion.getCiudad(),
                direccion.getProvincia());
        holder.tvAddressDetails.setText(fullAddress);

        holder.btnEditAddress.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditAddress(direccion);
            }
        });

        holder.btnDeleteAddress.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteAddress(direccion.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddressDetails;
        Button btnEditAddress, btnDeleteAddress;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddressDetails = itemView.findViewById(R.id.tvAddressDetails);
            btnEditAddress = itemView.findViewById(R.id.btnEditAddress);
            btnDeleteAddress = itemView.findViewById(R.id.btnDeleteAddress);
        }
    }
}

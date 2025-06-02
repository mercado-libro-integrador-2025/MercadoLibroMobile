package com.ispc.mercadolibromobile.dtos;

import com.google.gson.annotations.SerializedName;
import com.ispc.mercadolibromobile.models.Direccion;
import com.ispc.mercadolibromobile.models.ItemCarrito;

import java.util.List;

public class PedidoCreationRequest {
    @SerializedName("id_transaccion_mp")
    private String idTransaccionMp;
    @SerializedName("estado")
    private String estado;
    @SerializedName("total")
    private double total;
    @SerializedName("direccion_id")
    private int direccionId;
    @SerializedName("items_carrito_ids")
    private List<Integer> itemsCarritoIds;


    public PedidoCreationRequest(String idTransaccionMp, String estado, double total, int direccionId) {
        this.idTransaccionMp = idTransaccionMp;
        this.estado = estado;
        this.total = total;
        this.direccionId = direccionId;
    }

    // Getters
    public String getIdTransaccionMp() { return idTransaccionMp; }
    public String getEstado() { return estado; }
    public double getTotal() { return total; }
    public int getDireccionId() { return direccionId; }

}
package com.ispc.mercadolibromobile.models;

import com.google.gson.annotations.SerializedName;

public class ItemCarritoUpdatedTo {
    @SerializedName("cantidad")
    private int cantidad;

    public ItemCarritoUpdatedTo(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
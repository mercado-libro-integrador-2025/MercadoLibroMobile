package com.ispc.mercadolibromobile.dtos;

import com.google.gson.annotations.SerializedName;

public class ItemCarritoUpdateDto {
    @SerializedName("cantidad")
    private int cantidad;

    public ItemCarritoUpdateDto(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
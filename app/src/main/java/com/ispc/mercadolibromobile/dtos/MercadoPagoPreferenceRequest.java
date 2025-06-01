package com.ispc.mercadolibromobile.dtos;

import com.google.gson.annotations.SerializedName;
import com.ispc.mercadolibromobile.models.ProductoParaMP;

import java.util.List;

public class MercadoPagoPreferenceRequest {
    @SerializedName("productos")
    private List<ProductoParaMP> productos;
    @SerializedName("direccion_id")
    private int direccionId;

    public MercadoPagoPreferenceRequest(List<ProductoParaMP> productos, int direccionId) {
        this.productos = productos;
        this.direccionId = direccionId;
    }

    public List<ProductoParaMP> getProductos() {
        return productos;
    }

    public int getDireccionId() {
        return direccionId;
    }
}
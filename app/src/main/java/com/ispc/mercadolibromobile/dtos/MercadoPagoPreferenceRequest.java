package com.ispc.mercadolibromobile.dtos;

import com.google.gson.annotations.SerializedName;
import com.ispc.mercadolibromobile.models.ProductoParaMP;

import java.util.List;

public class MercadoPagoPreferenceRequest {
    @SerializedName("productos")
    private List<ProductoParaMP> productos;
    @SerializedName("direccion_id")
    private int direccionId;

    @SerializedName("is_mobile_app")
    private boolean isMobileApp;

    public MercadoPagoPreferenceRequest(List<ProductoParaMP> productos, int direccionId, boolean isMobileApp) {
        this.productos = productos;
        this.direccionId = direccionId;
        this.isMobileApp = isMobileApp;
    }

    public List<ProductoParaMP> getProductos() {
        return productos;
    }

    public int getDireccionId() {
        return direccionId;
    }

    public boolean getIsMobileApp() {
        return isMobileApp;
    }
}
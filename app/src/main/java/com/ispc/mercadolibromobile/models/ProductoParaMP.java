package com.ispc.mercadolibromobile.models;
import com.google.gson.annotations.SerializedName;

public class ProductoParaMP {
    @SerializedName("id_libro")
    private int idLibro;
    @SerializedName("cantidad")
    private int cantidad;

    public ProductoParaMP(int idLibro, int cantidad) {
        this.idLibro = idLibro;
        this.cantidad = cantidad;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
package com.example.mercadolibromobile.models;

import com.google.gson.annotations.SerializedName;

public class Categoria {
    @SerializedName("id_categoria")
    private int idCategoria;

    @SerializedName("nombre_categoria")
    private String nombreCategoria;

    // Constructor
    public Categoria(int idCategoria, String nombreCategoria) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }

    // Getters y Setters
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }
}

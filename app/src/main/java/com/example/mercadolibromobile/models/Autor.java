package com.example.mercadolibromobile.models;

import com.google.gson.annotations.SerializedName;

public class Autor {
    @SerializedName("id_autor")
    private int idAutor;

    @SerializedName("nombre_autor")
    private String nombreAutor;

    // Constructor
    public Autor(int idAutor, String nombreAutor) {
        this.idAutor = idAutor;
        this.nombreAutor = nombreAutor;
    }

    // Getters y Setters
    public int getIdAutor() {
        return idAutor;
    }

    public void setIdAutor(int idAutor) {
        this.idAutor = idAutor;
    }

    public String getNombreAutor() {
        return nombreAutor;
    }

    public void setNombreAutor(String nombreAutor) {
        this.nombreAutor = nombreAutor;
    }
}

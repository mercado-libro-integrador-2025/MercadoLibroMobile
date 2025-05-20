package com.example.mercadolibromobile.models;

import com.google.gson.annotations.SerializedName;

public class ItemCarrito {
    @SerializedName("id")
    private int id;

    @SerializedName("libro")
    private int libro;

    @SerializedName("usuario")
    private int usuario;

    @SerializedName("cantidad")
    private int cantidad;

    @SerializedName("precio_unitario")
    private double precioUnitario;

    @SerializedName("titulo_libro")
    private String tituloLibro;

    // Constructor sin id para crear un nuevo item desde la app
    public ItemCarrito(int libro, int usuario, int cantidad, double precioUnitario) {
        this.libro = libro;
        this.usuario = usuario;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Constructor con id para deserialización desde el backend
    public ItemCarrito(int id, int libro, int usuario, int cantidad, double precioUnitario) {
        this.id = id;
        this.libro = libro;
        this.usuario = usuario;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public int getLibro() {
        return libro;
    }

    public int getUsuario() {
        return usuario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public double getTotal() {
        return cantidad * precioUnitario;
    }

    public void aumentarCantidad() {
        cantidad++;
    }

    public void disminuirCantidad() {
        if (cantidad > 1) cantidad--;
    }
}

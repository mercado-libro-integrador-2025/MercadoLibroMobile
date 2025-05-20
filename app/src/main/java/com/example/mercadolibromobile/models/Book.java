package com.example.mercadolibromobile.models;

import com.google.gson.annotations.SerializedName;

public class Book {
    @SerializedName("id_libro")
    private int idLibro;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("autor")
    private Autor autor;

    @SerializedName("categoria")
    private Categoria categoria;

    @SerializedName("precio")
    private double precio;

    @SerializedName("stock")
    private int stock;

    @SerializedName("portada")
    private String portada;

    @SerializedName("descripcion")
    private String descripcion;

    // Constructor
    public Book(int idLibro, String titulo, Autor autor, Categoria categoria, double precio, int stock, String portada, String descripcion) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.portada = portada;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getPortada() {
        return portada;
    }

    public void setPortada(String portada) {
        this.portada = portada;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

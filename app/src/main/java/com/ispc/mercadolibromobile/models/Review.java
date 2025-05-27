package com.ispc.mercadolibromobile.models;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("id")
    private int idResena;

    @SerializedName("comentario")
    private String contenido;

    @SerializedName("libro")
    private int idLibro;

    @SerializedName("titulo_libro")
    private String tituloLibroAsociado;

    @SerializedName("fecha_creacion")
    private String fecha;

    @SerializedName("email_usuario")
    private String emailUsuario;

    public Review(String contenido, int idLibro, String emailUsuario) {
        this.contenido = contenido;
        this.idLibro = idLibro;
        this.emailUsuario = emailUsuario;
    }

    public Review(int idResena, String contenido, int idLibro, String tituloLibroAsociado, String fecha, String emailUsuario) {
        this.idResena = idResena;
        this.contenido = contenido;
        this.idLibro = idLibro;
        this.tituloLibroAsociado = tituloLibroAsociado;
        this.fecha = fecha;
        this.emailUsuario = emailUsuario;
    }

    // Getters
    public int getIdResena() {
        return idResena;
    }

    public String getContenido() {
        return contenido;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public String getTituloLibroAsociado() {
        return tituloLibroAsociado;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    // Setters
    public void setIdResena(int idResena) {
        this.idResena = idResena;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }


    public void setTituloLibroAsociado(String tituloLibroAsociado) {
        this.tituloLibroAsociado = tituloLibroAsociado;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }
}

package com.ispc.mercadolibromobile.models;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("id")
    private int idResena;

    @SerializedName("comentario")
    private String contenido;

    @SerializedName("libro")
    private int idLibro;

    @SerializedName("id_usuario")
    private int idUsuario;

    @SerializedName("titulo_libro")
    private String tituloLibroAsociado;

    @SerializedName("fecha_creacion")
    private String fecha;

    @SerializedName("email_usuario")
    private String emailUsuario;

    public Review(String contenido, int idLibro, int idUsuario) {
        this.contenido = contenido;
        this.idLibro = idLibro;
        this.idUsuario = idUsuario;
    }

    public Review(int idResena, String contenido, int idLibro, int idUsuario, String tituloLibroAsociado, String fecha, String emailUsuario) {
        this.idResena = idResena;
        this.contenido = contenido;
        this.idLibro = idLibro;
        this.idUsuario = idUsuario;
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

    public int getIdUsuario() {
        return idUsuario;
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

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
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

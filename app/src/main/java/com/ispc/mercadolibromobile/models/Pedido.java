package com.ispc.mercadolibromobile.models;

import com.google.gson.annotations.SerializedName;
public class Pedido {
    private int id;
    private int usuario;
    private Direccion direccion;
    @SerializedName("id_transaccion_mp")

    private String id_transaccion_mp;

    private String estado;

    @SerializedName("fecha_pedido")

    private String fecha_pedido;
    private double total;
    public Pedido() {}
    public Pedido(int id, int usuario, Direccion direccion, String id_transaccion_mp, String estado, String fecha_pedido, double total) {

        this.id = id;

        this.usuario = usuario;

        this.direccion = direccion;

        this.id_transaccion_mp = id_transaccion_mp;

        this.estado = estado;

        this.fecha_pedido = fecha_pedido;

        this.total = total;

    }

// Getters

    public int getId() { return id; }

    public int getUsuario() { return usuario; }

    public Direccion getDireccion() { return direccion; }

    public String getId_transaccion_mp() { return id_transaccion_mp; }

    public String getEstado() { return estado; }

    public String getFecha_pedido() { return fecha_pedido; }

    public double getTotal() { return total; }



// Setters

    public void setId(int id) { this.id = id; }

    public void setUsuario(int usuario) { this.usuario = usuario; }

    public void setDireccion(Direccion direccion) { this.direccion = direccion; }

    public void setId_transaccion_mp(String id_transaccion_mp) { this.id_transaccion_mp = id_transaccion_mp; }

    public void setEstado(String estado) { this.estado = estado; }

    public void setFecha_pedido(String fecha_pedido) { this.fecha_pedido = fecha_pedido; }

    public void setTotal(double total) { this.total = total; }

}
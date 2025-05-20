package com.example.mercadolibromobile.models;

public class Pago {
    private int usuario;
    private String numero_tarjeta;
    private String cvv;
    private String vencimiento;
    private String tipo_tarjeta;

    public Pago(int usuario, String numero_tarjeta, String cvv, String vencimiento, String tipo_tarjeta) {
        this.usuario = usuario;
        this.numero_tarjeta = numero_tarjeta;
        this.cvv = cvv;
        this.vencimiento = vencimiento;
        this.tipo_tarjeta = tipo_tarjeta;
    }

    // Getters y Setters
    public int getUsuario() {
        return usuario;
    }

    public String getNumero_tarjeta() {
        return numero_tarjeta;
    }

    public String getCvv() {
        return cvv;
    }

    public String getVencimiento() {
        return vencimiento;
    }

    public String getTipo_tarjeta() {
        return tipo_tarjeta;
    }
}

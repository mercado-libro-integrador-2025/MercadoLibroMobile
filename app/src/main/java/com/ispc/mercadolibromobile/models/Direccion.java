package com.ispc.mercadolibromobile.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Direccion implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("usuario")
    private int usuario;
    @SerializedName("calle")
    private String calle;
    @SerializedName("numero")
    private String numero;
    @SerializedName("ciudad")
    private String ciudad;
    @SerializedName("provincia")
    private String provincia;

    public Direccion(int id, int usuario, String calle, String numero, String ciudad, String provincia) {
        this.id = id;
        this.usuario = usuario;
        this.calle = calle;
        this.numero = numero;
        this.ciudad = ciudad;
        this.provincia = provincia;

    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuario() {
        return usuario;
    }

    public void setUsuario(int usuario) {
        this.usuario = usuario;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        fullAddress.append(calle).append(" ").append(numero);
        fullAddress.append(", ").append(ciudad).append(", ").append(provincia);
        return fullAddress.toString();
    }

    @Override
    public String toString() {
        return getFullAddress();
    }

}

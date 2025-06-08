package com.ispc.mercadolibromobile.models;

import java.io.Serializable;

public class Contacto implements Serializable {
    private Integer id;
    private String nombre;
    private String email;
    private String asunto;
    private String mensaje;

    public Contacto(String nombre, String email, String asunto, String mensaje) {
        this.nombre = nombre;
        this.email = email;
        this.asunto = asunto;
        this.mensaje = mensaje;
    }

    public Contacto(Integer id, String nombre, String email, String asunto, String mensaje) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.asunto = asunto;
        this.mensaje = mensaje;
    }

    // --- GETTERS ---
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getAsunto() { return asunto; }
    public String getMensaje() { return mensaje; }

    public void setId(Integer id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
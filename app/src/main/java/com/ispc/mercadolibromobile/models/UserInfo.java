package com.ispc.mercadolibromobile.models;

import java.io.Serializable; // Implementar Serializable si lo necesitas para Bundles

public class UserInfo implements Serializable {
    private int id;
    private String email;
    private String username;

    public UserInfo() {
    }

    public UserInfo(int id, String email, String username) {
        this.id = id;
        this.email = email;
        this.username = username;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
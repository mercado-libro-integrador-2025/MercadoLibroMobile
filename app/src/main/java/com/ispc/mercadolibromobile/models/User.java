package com.ispc.mercadolibromobile.models;

public class User {
    private String refresh;
    private String access;
    private String name;
    private UserInfo user;

    public String getRefresh() {
        return refresh;
    }

    public String getAccess() {
        return access;
    }

    public UserInfo getUser() {
        return user;
    }

    public String getName() {return name;}
}


package com.example.mercadolibromobile.utils;

import com.auth0.android.jwt.JWT;

public class AuthUtils {
    public static int obtenerUsuarioIdDesdeToken(String token) {
        try {
            JWT jwt = new JWT(token);
            return jwt.getClaim("user_id").asInt();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}

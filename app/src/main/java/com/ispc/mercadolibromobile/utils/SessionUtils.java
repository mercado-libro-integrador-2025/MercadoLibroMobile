package com.ispc.mercadolibromobile.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class SessionUtils {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";

    private static final String TAG = "SessionUtils"; // Tag para logs

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveAuthToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public static void saveRefreshToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_REFRESH_TOKEN, token).apply();
    }

    public static void saveUserEmail(Context context, String email) {
        getPrefs(context).edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public static String getAuthToken(Context context) {
        return getPrefs(context).getString(KEY_ACCESS_TOKEN, null);
    }

    public static String getRefreshToken(Context context) {
        return getPrefs(context).getString(KEY_REFRESH_TOKEN, null);
    }

    public static String getUserEmail(Context context) {
        return getPrefs(context).getString(KEY_USER_EMAIL, null);
    }

    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public static void saveUserId(Context context, int userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.commit(); // *** CAMBIADO A COMMIT() PARA ESCRITURA SÍNCRONA ***
        Log.d(TAG, "User ID saved: " + userId + " (using commit)");
    }

    public static int getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        Log.d(TAG, "User ID retrieved from SharedPreferences: " + userId);
        return userId;
    }

    public static void saveUserName(Context context, String username) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public static String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USERNAME, null);
    }

    public static int getUserIdFromJwt(String token) {
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token JWT es nulo o vacío.");
            return -1;
        }

        try {
            String[] split = token.split("\\.");
            if (split.length < 2) {
                Log.e(TAG, "Token JWT inválido: no tiene suficientes partes.");
                return -1;
            }

            byte[] decodedBytes = Base64.decode(split[1], Base64.URL_SAFE);
            String payload = new String(decodedBytes, "UTF-8");
            Log.d(TAG, "JWT Payload decoded: " + payload);

            JSONObject jsonPayload = new JSONObject(payload);
            if (jsonPayload.has("user_id")) {
                int userId = jsonPayload.getInt("user_id");
                Log.d(TAG, "User ID found in JWT payload: " + userId);
                return userId;
            } else {
                Log.w(TAG, "El payload del JWT no contiene el campo 'user_id'. Payload: " + payload);
                return -1;
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error de codificación al decodificar JWT: " + e.getMessage());
            return -1;
        } catch (JSONException e) {
            Log.e(TAG, "Error al parsear JSON del payload JWT: " + e.getMessage());
            return -1;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error de Base64 al decodificar JWT (posiblemente token mal formado): " + e.getMessage());
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado al obtener user ID de JWT: " + e.getMessage());
            return -1;
        }
    }

    // verificacion expiracion token
    public static boolean isTokenExpired(String token) {
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token nulo o vacío. Se considera expirado.");
            return true;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                Log.e(TAG, "Token inválido. Se considera expirado.");
                return true;
            }

            byte[] decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE);
            String payload = new String(decodedBytes, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(payload);

            long exp = jsonObject.getLong("exp"); // Tiempo de expiración en segundos
            long now = System.currentTimeMillis() / 1000; // Tiempo actual en segundos

            Log.d(TAG, "Tiempo de expiración del token: " + exp + ", tiempo actual: " + now);

            return exp < now;
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar expiración del token: " + e.getMessage());
            return true; // Si hay algun error,se trata el token como expirado
        }
    }
}

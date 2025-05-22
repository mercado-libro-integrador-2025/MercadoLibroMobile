package com.example.mercadolibromobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionUtils {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";

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

    public static void saveUserId(Context context, int userId) {
        getPrefs(context).edit().putInt(KEY_USER_ID, userId).apply();
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

    public static int getUserId(Context context) {
        return getPrefs(context).getInt(KEY_USER_ID, -1);
    }

    public static void clearSession(Context context) {
        getPrefs(context).edit().clear().apply();
    }
}

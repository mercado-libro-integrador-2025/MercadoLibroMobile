package com.ispc.mercadolibromobile.models;

public class AuthModels {
    public static class LoginResponse {
        private String access;
        private String refresh;
        private int userId;

        public String getAccess() {
            return access;
        }

        public String getRefresh() {
            return refresh;
        }

        public int getUserId() {
            return userId;
    }}

    public static class SignupRequest {
        private final String email;
        private final String password;
        private final String username;

        public SignupRequest(String email, String password, String username) {
            this.email = email;
            this.password = password;
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class SignupResponse {
        private String access;
        private String refresh;

        public String getAccess() {
            return access;
        }

        public String getRefresh() {
            return refresh;
        }
    }

    public static class LoginRequest {
        private final String email;
        private final String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }
}
package com.example.project.models;

/**
 * Authentication response models for all auth endpoints
 */
public class AuthResponse {
    private boolean success;
    private String message;
    private User user;
    private String accessToken;
    private String refreshToken;
    private String error;
    private Session session; // For Supabase response structure
    
    // Default constructor for Gson
    public AuthResponse() {}
    
    // Constructor for success response
    public AuthResponse(boolean success, String message, User user, String accessToken, String refreshToken) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    
    // Constructor for error response
    public AuthResponse(String error, String message) {
        this.success = false;
        this.error = error;
        this.message = message;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public User getUser() { return user; }
    public String getAccessToken() {
        // Handle both direct accessToken and session.access_token
        if (accessToken != null) return accessToken;
        if (session != null) return session.getAccessToken();
        return null;
    }
    public String getRefreshToken() {
        // Handle both direct refreshToken and session.refresh_token
        if (refreshToken != null) return refreshToken;
        if (session != null) return session.getRefreshToken();
        return null;
    }
    public String getError() { return error; }
    public Session getSession() { return session; }
    
    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setUser(User user) { this.user = user; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setError(String error) { this.error = error; }
    public void setSession(Session session) { this.session = session; }
    
    /**
     * Validates the response structure
     */
    public boolean isValidResponse() {
        if (success) {
            // Check if we have user and access token (either direct or via session)
            String token = getAccessToken();
            return user != null && token != null && !token.trim().isEmpty();
        } else {
            return error != null && !error.trim().isEmpty();
        }
    }
    
    @Override
    public String toString() {
        if (success) {
            return "AuthResponse{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", user=" + user +
                    ", accessToken='" + (accessToken != null ? "[REDACTED]" : "null") + '\'' +
                    ", refreshToken='" + (refreshToken != null ? "[REDACTED]" : "null") + '\'' +
                    '}';
        } else {
            return "AuthResponse{" +
                    "success=" + success +
                    ", error='" + error + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
    
    /**
     * User model for authentication responses
     */
    public static class User {
        private String id;
        private String email;
        private String name;
        private String picture;
        private String type; // "google" or "guest"

        // Supabase-specific fields
        private UserMetadata user_metadata;
        private AppMetadata app_metadata;
        
        public User() {}
        
        public User(String id, String email, String name, String picture, String type) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.picture = picture;
            this.type = type;
        }
        
        // Getters
        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getName() {
            // Handle both direct name and user_metadata.full_name
            if (name != null) return name;
            if (user_metadata != null) return user_metadata.getFull_name();
            return null;
        }
        public String getPicture() {
            // Handle both direct picture and user_metadata.picture
            if (picture != null) return picture;
            if (user_metadata != null) return user_metadata.getPicture();
            return null;
        }
        public String getType() {
            // Handle both direct type and app_metadata.provider
            if (type != null) return type;
            if (app_metadata != null && app_metadata.getProvider() != null) return app_metadata.getProvider();
            return "guest"; // default
        }
        
        // Setters
        public void setId(String id) { this.id = id; }
        public void setEmail(String email) { this.email = email; }
        public void setName(String name) { this.name = name; }
        public void setPicture(String picture) { this.picture = picture; }
        public void setType(String type) { this.type = type; }
        
        public boolean isGuest() {
            return "guest".equals(type);
        }
        
        public boolean isGoogleUser() {
            return "google".equals(type);
        }
        
        @Override
        public String toString() {
            return "User{" +
                    "id='" + id + '\'' +
                    ", email='" + email + '\'' +
                    ", name='" + name + '\'' +
                    ", picture='" + picture + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    /**
     * Session model for Supabase authentication responses
     */
    public static class Session {
        private String access_token;
        private String refresh_token;
        private String token_type;
        private int expires_in;
        private long expires_at;

        public Session() {}

        // Getters
        public String getAccessToken() { return access_token; }
        public String getRefreshToken() { return refresh_token; }
        public String getTokenType() { return token_type; }
        public int getExpiresIn() { return expires_in; }
        public long getExpiresAt() { return expires_at; }

        // Setters
        public void setAccessToken(String access_token) { this.access_token = access_token; }
        public void setRefreshToken(String refresh_token) { this.refresh_token = refresh_token; }
        public void setTokenType(String token_type) { this.token_type = token_type; }
        public void setExpiresIn(int expires_in) { this.expires_in = expires_in; }
        public void setExpiresAt(long expires_at) { this.expires_at = expires_at; }

        @Override
        public String toString() {
            return "Session{" +
                    "access_token='" + (access_token != null ? "[REDACTED]" : "null") + '\'' +
                    ", refresh_token='" + (refresh_token != null ? "[REDACTED]" : "null") + '\'' +
                    ", token_type='" + token_type + '\'' +
                    ", expires_in=" + expires_in +
                    ", expires_at=" + expires_at +
                    '}';
        }
    }

    /**
     * User metadata from Supabase
     */
    public static class UserMetadata {
        private String full_name;
        private String picture;
        private String avatar_url;

        public String getFull_name() { return full_name; }
        public String getPicture() {
            if (picture != null) return picture;
            return avatar_url; // fallback to avatar_url
        }
        public String getAvatar_url() { return avatar_url; }
    }

    /**
     * App metadata from Supabase
     */
    public static class AppMetadata {
        private String provider;

        public String getProvider() { return provider; }
    }
}

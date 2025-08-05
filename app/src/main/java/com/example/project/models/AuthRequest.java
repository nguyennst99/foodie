package com.example.project.models;

/**
 * Authentication request models for Google OAuth and Guest login
 */
public class AuthRequest {
    
    /**
     * Google OAuth authentication request
     */
    public static class GoogleAuthRequest {
        private String idToken;
        
        public GoogleAuthRequest() {}
        
        public GoogleAuthRequest(String idToken) {
            this.idToken = idToken;
        }
        
        public String getIdToken() { return idToken; }
        public void setIdToken(String idToken) { this.idToken = idToken; }
        
        @Override
        public String toString() {
            return "GoogleAuthRequest{" +
                    "idToken='" + (idToken != null ? "[REDACTED]" : "null") + '\'' +
                    '}';
        }
    }
    
    /**
     * Guest authentication request
     */
    public static class GuestAuthRequest {
        private String deviceId;
        
        public GuestAuthRequest() {}
        
        public GuestAuthRequest(String deviceId) {
            this.deviceId = deviceId;
        }
        
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        @Override
        public String toString() {
            return "GuestAuthRequest{" +
                    "deviceId='" + deviceId + '\'' +
                    '}';
        }
    }
    
    /**
     * Token refresh request
     */
    public static class RefreshTokenRequest {
        private String refreshToken;
        
        public RefreshTokenRequest() {}
        
        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        
        @Override
        public String toString() {
            return "RefreshTokenRequest{" +
                    "refreshToken='" + (refreshToken != null ? "[REDACTED]" : "null") + '\'' +
                    '}';
        }
    }
}

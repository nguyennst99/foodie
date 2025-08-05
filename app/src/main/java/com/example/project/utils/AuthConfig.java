package com.example.project.utils;

import android.content.Context;
import android.util.Log;
import com.example.project.R;

/**
 * Authentication configuration helper
 * Handles both development and production Google OAuth setup
 */
public class AuthConfig {
    private static final String TAG = "AuthConfig";
    
    // Development mode flag - set to false when you have real Google OAuth configured
    public static final boolean DEVELOPMENT_MODE = false; // Production mode - real Google Sign-In popup will appear
    
    /**
     * Check if Google OAuth is properly configured
     */
    public static boolean isGoogleOAuthConfigured(Context context) {
        try {
            String clientId = context.getString(R.string.google_oauth_client_id);
            boolean isConfigured = !clientId.equals("YOUR_GOOGLE_OAUTH_CLIENT_ID_HERE") && 
                                 !clientId.trim().isEmpty();
            
            Log.d(TAG, "Google OAuth configured: " + isConfigured);
            return isConfigured;
        } catch (Exception e) {
            Log.e(TAG, "Error checking Google OAuth configuration", e);
            return false;
        }
    }
    
    /**
     * Get development mode status
     */
    public static boolean isDevelopmentMode() {
        return DEVELOPMENT_MODE;
    }
    
    /**
     * Log configuration status
     */
    public static void logConfigurationStatus(Context context) {
        Log.d(TAG, "=== Authentication Configuration ===");
        Log.d(TAG, "Development Mode: " + DEVELOPMENT_MODE);
        Log.d(TAG, "Google OAuth Configured: " + isGoogleOAuthConfigured(context));
        
        if (DEVELOPMENT_MODE) {
            Log.d(TAG, "Running in development mode");
            Log.d(TAG, " To enable real Google OAuth:");
            Log.d(TAG, "   1. Get Google OAuth Client ID from Google Cloud Console");
            Log.d(TAG, "   2. Replace YOUR_GOOGLE_OAUTH_CLIENT_ID_HERE in google_oauth_config.xml");
            Log.d(TAG, "   3. Set DEVELOPMENT_MODE = false in AuthConfig.java");
        } else if (!isGoogleOAuthConfigured(context)) {
            Log.w(TAG, " Production mode but Google OAuth not configured!");
            Log.w(TAG, " Please configure Google OAuth Client ID");
        } else {
            Log.d(TAG, "Production mode with Google OAuth configured");
        }
        Log.d(TAG, "=====================================");
    }
}

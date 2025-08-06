package com.example.project.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager to handle app settings and environment variables
 * Reads from config.properties file in assets folder
 */
public class ConfigManager {
    private static final String TAG = "ConfigManager";
    private static final String CONFIG_FILE = "config.properties";
    
    // Default values
    private static final String DEFAULT_BASE_URL = "http://10.0.2.2:3000";
    private static final int DEFAULT_TIMEOUT = 30;
    
    private static ConfigManager instance;
    private Properties properties;
    
    private ConfigManager(Context context) {
        loadConfiguration(context);
    }
    
    public static synchronized ConfigManager getInstance(Context context) {
        if (instance == null) {
            instance = new ConfigManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private void loadConfiguration(Context context) {
        properties = new Properties();
        
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(CONFIG_FILE);
            properties.load(inputStream);
            inputStream.close();
            Log.d(TAG, "Configuration loaded successfully");
        } catch (IOException e) {
            Log.w(TAG, "Could not load " + CONFIG_FILE + ", using defaults: " + e.getMessage());
            loadDefaults();
        }
    }
    
    private void loadDefaults() {
        properties.setProperty("api.base.url", DEFAULT_BASE_URL);
        properties.setProperty("api.timeout.seconds", String.valueOf(DEFAULT_TIMEOUT));
        properties.setProperty("development.mode", "true");
        properties.setProperty("debug.logging", "true");
    }
    
    public String getApiBaseUrl() {
        return properties.getProperty("api.base.url", DEFAULT_BASE_URL);
    }
    
    public int getApiTimeoutSeconds() {
        try {
            return Integer.parseInt(properties.getProperty("api.timeout.seconds", String.valueOf(DEFAULT_TIMEOUT)));
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid timeout value, using default: " + DEFAULT_TIMEOUT);
            return DEFAULT_TIMEOUT;
        }
    }
    
    public boolean isDevelopmentMode() {
        return Boolean.parseBoolean(properties.getProperty("development.mode", "true"));
    }
    
    public boolean isDebugLoggingEnabled() {
        return Boolean.parseBoolean(properties.getProperty("debug.logging", "true"));
    }

    public String getGoogleMapsApiKey() {
        return properties.getProperty("google.maps.api.key", "");
    }
    
    public void logConfigurationStatus() {
        Log.d(TAG, "=== Configuration Status ===");
        Log.d(TAG, "API Base URL: " + getApiBaseUrl());
        Log.d(TAG, "API Timeout: " + getApiTimeoutSeconds() + "s");
        Log.d(TAG, "Development Mode: " + isDevelopmentMode());
        Log.d(TAG, "Debug Logging: " + isDebugLoggingEnabled());
        Log.d(TAG, "Google Maps API Key: " + (getGoogleMapsApiKey().isEmpty() ? "Not configured" : "Configured"));
        Log.d(TAG, "============================");
    }
}
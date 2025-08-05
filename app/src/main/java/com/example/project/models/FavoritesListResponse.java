package com.example.project.models;

import java.util.List;

/**
 * Response model for the get favorites API endpoint
 * Based on the testing guide expected response structure
 */
public class FavoritesListResponse {
    private boolean success;
    private List<FavoriteItem> favorites;
    private int count;
    private String error;
    private String message;

    // Default constructor for Gson
    public FavoritesListResponse() {}

    // Constructor for success response
    public FavoritesListResponse(boolean success, List<FavoriteItem> favorites, int count) {
        this.success = success;
        this.favorites = favorites;
        this.count = count;
    }

    // Constructor for error response
    public FavoritesListResponse(String error, String message) {
        this.success = false;
        this.error = error;
        this.message = message;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public List<FavoriteItem> getFavorites() { return favorites; }
    public int getCount() { return count; }
    public String getError() { return error; }
    public String getMessage() { return message; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setFavorites(List<FavoriteItem> favorites) { this.favorites = favorites; }
    public void setCount(int count) { this.count = count; }
    public void setError(String error) { this.error = error; }
    public void setMessage(String message) { this.message = message; }

    /**
     * Validates the response structure according to testing guide
     */
    public boolean isValidResponse() {
        if (success) {
            return favorites != null && count >= 0;
        } else {
            return error != null && !error.trim().isEmpty();
        }
    }

    @Override
    public String toString() {
        if (success) {
            return "FavoritesListResponse{" +
                    "success=" + success +
                    ", favorites=" + (favorites != null ? favorites.size() : 0) + " items" +
                    ", count=" + count +
                    '}';
        } else {
            return "FavoritesListResponse{" +
                    "success=" + success +
                    ", error='" + error + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}

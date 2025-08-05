package com.example.project.models;

import java.util.List;

/**
 * Response model for the restaurant search API
 * Based on the testing guide expected response structure
 */
public class SearchResponse {
    private boolean success;
    private List<Restaurant> restaurants;
    private int count;
    private String error;
    private String message;

    // Default constructor for Gson
    public SearchResponse() {}

    // Constructor for success response
    public SearchResponse(boolean success, List<Restaurant> restaurants, int count) {
        this.success = success;
        this.restaurants = restaurants;
        this.count = count;
    }

    // Constructor for error response
    public SearchResponse(String error, String message) {
        this.success = false;
        this.error = error;
        this.message = message;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public List<Restaurant> getRestaurants() { return restaurants; }
    public int getCount() { return count; }
    public String getError() { return error; }
    public String getMessage() { return message; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setRestaurants(List<Restaurant> restaurants) { this.restaurants = restaurants; }
    public void setCount(int count) { this.count = count; }
    public void setError(String error) { this.error = error; }
    public void setMessage(String message) { this.message = message; }

    /**
     * Validates the response structure according to testing guide
     */
    public boolean isValidResponse() {
        if (success) {
            return restaurants != null && count >= 0;
        } else {
            return error != null && !error.trim().isEmpty();
        }
    }

    @Override
    public String toString() {
        if (success) {
            return "SearchResponse{" +
                    "success=" + success +
                    ", restaurants=" + (restaurants != null ? restaurants.size() : 0) + " items" +
                    ", count=" + count +
                    '}';
        } else {
            return "SearchResponse{" +
                    "success=" + success +
                    ", error='" + error + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}

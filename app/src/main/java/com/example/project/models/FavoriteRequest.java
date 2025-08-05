package com.example.project.models;

/**
 * Request model for adding a restaurant to favorites
 * Based on the testing guide API specification
 */
public class FavoriteRequest {
    private Restaurant restaurant_data;

    // Default constructor for Gson
    public FavoriteRequest() {}

    // Constructor
    public FavoriteRequest(Restaurant restaurant_data) {
        this.restaurant_data = restaurant_data;
    }

    // Getters and Setters
    public Restaurant getRestaurantData() { return restaurant_data; }
    public void setRestaurantData(Restaurant restaurant_data) { this.restaurant_data = restaurant_data; }

    /**
     * Validates the request data
     */
    public boolean isValid() {
        return restaurant_data != null && restaurant_data.isValid();
    }

    @Override
    public String toString() {
        return "FavoriteRequest{" +
                "restaurant_data=" + restaurant_data +
                '}';
    }
}

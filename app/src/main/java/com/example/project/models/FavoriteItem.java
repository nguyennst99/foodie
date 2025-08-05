package com.example.project.models;

/**
 * Model class representing a favorite item from the get favorites API
 * Based on the testing guide expected response structure
 */
public class FavoriteItem {
    private String id;
    private Restaurant restaurant;
    private String created_at;

    // Default constructor for Gson
    public FavoriteItem() {}

    // Constructor
    public FavoriteItem(String id, Restaurant restaurant, String created_at) {
        this.id = id;
        this.restaurant = restaurant;
        this.created_at = created_at;
    }

    // Getters
    public String getId() { return id; }
    public Restaurant getRestaurantData() { return restaurant; }
    public String getCreatedAt() { return created_at; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setRestaurantData(Restaurant restaurant) { this.restaurant = restaurant; }
    public void setCreatedAt(String created_at) { this.created_at = created_at; }

    /**
     * Validates the favorite item data
     */
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
               restaurant != null && restaurant.isValid() &&
               created_at != null && !created_at.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "FavoriteItem{" +
                "id='" + id + '\'' +
                ", restaurant=" + restaurant +
                ", created_at='" + created_at + '\'' +
                '}';
    }
}

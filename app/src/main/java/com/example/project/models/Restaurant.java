package com.example.project.models;

import java.util.Map;

/**
 * Restaurant model class representing the data structure from the backend API
 * Based on the testing guide response format
 */
public class Restaurant {
    private String name;
    private String cuisine_type;
    private double rating;
    private String address;
    private String phone;
    private String description;
    private Map<String, String> hours;
    private Double latitude;  // Using Double to allow null values
    private Double longitude; // Using Double to allow null values

    // Default constructor for Gson
    public Restaurant() {}

    // Constructor for testing (backward compatibility)
    public Restaurant(String name, String cuisine_type, double rating, String address,
                     String phone, String description, Map<String, String> hours) {
        this.name = name;
        this.cuisine_type = cuisine_type;
        this.rating = rating;
        this.address = address;
        this.phone = phone;
        this.description = description;
        this.hours = hours;
        this.latitude = null;
        this.longitude = null;
    }

    // Constructor with location data
    public Restaurant(String name, String cuisine_type, double rating, String address,
                     String phone, String description, Map<String, String> hours,
                     Double latitude, Double longitude) {
        this.name = name;
        this.cuisine_type = cuisine_type;
        this.rating = rating;
        this.address = address;
        this.phone = phone;
        this.description = description;
        this.hours = hours;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getName() { return name; }
    public String getCuisineType() { return cuisine_type; }
    public double getRating() { return rating; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getDescription() { return description; }
    public Map<String, String> getHours() { return hours; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setCuisineType(String cuisine_type) { this.cuisine_type = cuisine_type; }
    public void setRating(double rating) { this.rating = rating; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDescription(String description) { this.description = description; }
    public void setHours(Map<String, String> hours) { this.hours = hours; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    /**
     * Validates the restaurant data according to testing guide requirements
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               cuisine_type != null && !cuisine_type.trim().isEmpty() &&
               rating >= 3.0 && rating <= 5.0 &&
               address != null && !address.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty() &&
               description != null && description.length() >= 50 &&
               hours != null && !hours.isEmpty();
    }

    /**
     * Gets formatted hours for today (simplified for testing)
     */
    public String getTodayHours() {
        if (hours == null) return "Hours not available";
        
        // For testing, just return Monday hours or first available
        String todayHours = hours.get("monday");
        if (todayHours == null && !hours.isEmpty()) {
            todayHours = hours.values().iterator().next();
        }
        return todayHours != null ? "Open today: " + todayHours : "Hours not available";
    }

    /**
     * Check if restaurant has valid location coordinates
     */
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    /**
     * Get location as a formatted string for debugging
     */
    public String getLocationString() {
        if (hasLocation()) {
            return String.format("%.6f, %.6f", latitude, longitude);
        }
        return "No coordinates available";
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "name='" + name + '\'' +
                ", cuisine_type='" + cuisine_type + '\'' +
                ", rating=" + rating +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", description='" + description + '\'' +
                ", hours=" + hours +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}

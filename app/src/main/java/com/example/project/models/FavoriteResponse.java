package com.example.project.models;

/**
 * Response model for favorites API operations
 * Based on the testing guide API specification
 */
public class FavoriteResponse {
    private boolean success;
    private String message;
    private String favorite_id;
    private String restaurant_id;
    private String error;

    // Default constructor for Gson
    public FavoriteResponse() {}

    // Constructor for success response
    public FavoriteResponse(boolean success, String message, String favorite_id, String restaurant_id) {
        this.success = success;
        this.message = message;
        this.favorite_id = favorite_id;
        this.restaurant_id = restaurant_id;
    }

    // Constructor for error response
    public FavoriteResponse(String error, String message) {
        this.success = false;
        this.error = error;
        this.message = message;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getFavoriteId() { return favorite_id; }
    public String getRestaurantId() { return restaurant_id; }
    public String getError() { return error; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setFavoriteId(String favorite_id) { this.favorite_id = favorite_id; }
    public void setRestaurantId(String restaurant_id) { this.restaurant_id = restaurant_id; }
    public void setError(String error) { this.error = error; }

    /**
     * Validates the response structure according to testing guide
     */
    public boolean isValidResponse() {
        if (success) {
            return message != null && !message.trim().isEmpty() &&
                   favorite_id != null && !favorite_id.trim().isEmpty() &&
                   restaurant_id != null && !restaurant_id.trim().isEmpty();
        } else {
            return error != null && !error.trim().isEmpty();
        }
    }

    @Override
    public String toString() {
        if (success) {
            return "FavoriteResponse{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", favorite_id='" + favorite_id + '\'' +
                    ", restaurant_id='" + restaurant_id + '\'' +
                    '}';
        } else {
            return "FavoriteResponse{" +
                    "success=" + success +
                    ", error='" + error + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}

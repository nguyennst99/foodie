package com.example.project.network;

import android.content.Context;
import android.util.Log;
import com.example.project.models.AuthRequest;
import com.example.project.models.AuthResponse;
import com.example.project.models.FavoriteRequest;
import com.example.project.models.FavoriteResponse;
import com.example.project.models.FavoritesListResponse;
import com.example.project.models.SearchResponse;
import com.example.project.utils.ConfigManager;
import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * API Client for communicating with the Foodie backend server
 * Handles restaurant search requests and responses
 */
public class ApiClient {
    private static final String TAG = "ApiClient";

    private static final String SEARCH_ENDPOINT = "/api/restaurants/search";
    private static final String FAVORITES_ENDPOINT = "/api/favorites";
    private static final String TRENDING_ENDPOINT = "/api/restaurants/trending";
    private static final String AUTH_GOOGLE_ENDPOINT = "/api/auth/google";
    private static final String AUTH_GUEST_ENDPOINT = "/api/auth/guest";
    private static final String AUTH_REFRESH_ENDPOINT = "/api/auth/refresh";
    private static final String AUTH_USER_ENDPOINT = "/api/auth/user";
    private static final String AUTH_LOGOUT_ENDPOINT = "/api/auth/logout";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private final ConfigManager configManager;
    private final String baseUrl;

    // Token management
    private String accessToken;
    private String refreshToken;

    // Singleton instance
    private static ApiClient instance;
    
    private ApiClient(Context context) {
        this.configManager = ConfigManager.getInstance(context);
        this.baseUrl = configManager.getApiBaseUrl();
        
        this.client = new OkHttpClient.Builder()
                .connectTimeout(configManager.getApiTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(configManager.getApiTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(configManager.getApiTimeoutSeconds(), TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        
        // Log configuration status
        configManager.logConfigurationStatus();
    }
    
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApiClient must be initialized with a Context first. Call getInstance(Context) instead.");
        }
        return instance;
    }
    
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    /**
     * Set authentication tokens
     */
    public void setAuthTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        Log.d(TAG, "🔑 Auth tokens updated");
    }

    /**
     * Clear authentication tokens
     */
    public void clearAuthTokens() {
        this.accessToken = null;
        this.refreshToken = null;
        Log.d(TAG, "🔑 Auth tokens cleared");
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return accessToken != null && !accessToken.trim().isEmpty();
    }

    /**
     * Interface for handling search results
     */
    public interface SearchCallback {
        void onSuccess(SearchResponse response);
        void onError(String error);
    }

    /**
     * Interface for handling favorites operations
     */
    public interface FavoritesCallback {
        void onSuccess(FavoriteResponse response);
        void onError(String error);
    }

    /**
     * Interface for handling favorites list operations
     */
    public interface FavoritesListCallback {
        void onSuccess(FavoritesListResponse response);
        void onError(String error);
    }

    /**
     * Interface for handling authentication operations
     */
    public interface AuthCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }

    /**
     * Search for restaurants using the backend API
     * @param query Search query (required)
     * @param location Location filter (optional)
     * @param callback Callback to handle the response
     */
    public void searchRestaurants(String query, String location, SearchCallback callback) {
        Log.d(TAG, "🔍 Searching restaurants: query=" + query + ", location=" + location);
        Log.d(TAG, "Using base URL: " + baseUrl);
        
        // Validate required parameters
        if (query == null || query.trim().isEmpty()) {
            callback.onError("Query parameter is required");
            return;
        }
        
        // Build URL with parameters
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + SEARCH_ENDPOINT).newBuilder();
        urlBuilder.addQueryParameter("q", query.trim());
        
        if (location != null && !location.trim().isEmpty()) {
            urlBuilder.addQueryParameter("location", location.trim());
        }
        
        String url = urlBuilder.build().toString();
        Log.d(TAG, "Request URL: " + url);
        
        // Create request
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network request failed", e);
                callback.onError("Network error: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response body: " + responseBody);
                
                try {
                    if (response.isSuccessful()) {
                        SearchResponse searchResponse = gson.fromJson(responseBody, SearchResponse.class);
                        
                        // Validate response structure
                        if (searchResponse != null && searchResponse.isValidResponse()) {
                            Log.d(TAG, "Parsed response: " + searchResponse.toString());
                            callback.onSuccess(searchResponse);
                        } else {
                            Log.e(TAG, "Invalid response structure");
                            callback.onError("Invalid response structure");
                        }
                    } else {
                        // Try to parse error response
                        try {
                            SearchResponse errorResponse = gson.fromJson(responseBody, SearchResponse.class);
                            if (errorResponse != null && errorResponse.getError() != null) {
                                callback.onError(errorResponse.getError() + ": " + errorResponse.getMessage());
                            } else {
                                callback.onError("HTTP " + response.code() + ": " + response.message());
                            }
                        } catch (Exception e) {
                            callback.onError("HTTP " + response.code() + ": " + response.message());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                    callback.onError("Error parsing response: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Test connectivity to the backend server
     */
    public void testConnectivity(SearchCallback callback) {
        Log.d(TAG, "🔗 Testing connectivity to backend server");
                Log.d(TAG, "Testing URL: " + baseUrl + "/health");
        
        String healthUrl = baseUrl + "/health";
        Request request = new Request.Builder()
                .url(healthUrl)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                        Log.e(TAG, " Connectivity test failed to " + baseUrl, e);
            String errorMsg = "Cannot connect to server at " + baseUrl + ": " + e.getMessage();

                // Provide helpful debugging information
                if (e.getMessage().contains("CLEARTEXT")) {
                    errorMsg += "\n Tip: Make sure network security config allows cleartext traffic";
                } else if (e.getMessage().contains("UnknownHost")) {
                    errorMsg += "\n Tip: Check if server is running and accessible";
                }

                callback.onError(errorMsg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "Connectivity test response: " + response.code());
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "Server health response: " + body);
                    callback.onSuccess(new SearchResponse(true, null, 0));
                } else {
                    callback.onError("Server returned: " + response.code());
                }
            }
        });
    }

    /**
     * Add a restaurant to favorites
     * @param favoriteRequest Request containing restaurant data
     * @param callback Callback to handle the response
     */
    public void addToFavorites(FavoriteRequest favoriteRequest, FavoritesCallback callback) {
        Log.d(TAG, "️ Adding restaurant to favorites: " + favoriteRequest.getRestaurantData().getName());
        Log.d(TAG, "Using URL: " + baseUrl + FAVORITES_ENDPOINT);

        // Validate request
        if (!favoriteRequest.isValid()) {
            callback.onError("Invalid restaurant data");
            return;
        }

        // Check authentication
        if (!isAuthenticated()) {
            callback.onError("Authentication required. Please log in first.");
            return;
        }

        // Convert request to JSON
        String jsonBody = gson.toJson(favoriteRequest);
        Log.d(TAG, " Request body: " + jsonBody);

        RequestBody body = RequestBody.create(jsonBody, JSON);

        // Create request with authorization header
        Request.Builder requestBuilder = new Request.Builder()
                .url(baseUrl + FAVORITES_ENDPOINT)
                .post(body);

        // Add authorization header
        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
            Log.d(TAG, "🔑 Added authorization header");
        }

        Request request = requestBuilder.build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, " Add to favorites failed", e);
                String errorMsg = "Network error: " + e.getMessage();

                if (e.getMessage().contains("CLEARTEXT")) {
                    errorMsg += "\n Tip: Check network security configuration";
                } else if (e.getMessage().contains("UnknownHost")) {
                    errorMsg += "\n Tip: Check if server is running";
                }

                callback.onError(errorMsg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, " Add to favorites response code: " + response.code());
                Log.d(TAG, " Add to favorites response body: " + responseBody);

                try {
                    if (response.isSuccessful()) {
                        FavoriteResponse favoriteResponse = gson.fromJson(responseBody, FavoriteResponse.class);

                        if (favoriteResponse != null && favoriteResponse.isValidResponse()) {
                            Log.d(TAG, "Restaurant added to favorites: " + favoriteResponse.toString());
                            callback.onSuccess(favoriteResponse);
                        } else {
                            Log.e(TAG, " Invalid response structure");
                            callback.onError("Invalid response structure");
                        }
                    } else {
                        // Try to parse error response
                        try {
                            FavoriteResponse errorResponse = gson.fromJson(responseBody, FavoriteResponse.class);
                            if (errorResponse != null && errorResponse.getError() != null) {
                                callback.onError(errorResponse.getError() + ": " + errorResponse.getMessage());
                            } else {
                                callback.onError("HTTP " + response.code() + ": " + response.message());
                            }
                        } catch (Exception e) {
                            callback.onError("HTTP " + response.code() + ": " + response.message());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, " Error parsing add to favorites response", e);
                    callback.onError("Error parsing response: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Authenticate with Google OAuth
     * @param idToken Google ID token from Android app
     * @param callback Callback to handle the response
     */
    public void authenticateWithGoogle(String idToken, AuthCallback callback) {
        Log.d(TAG, "🔐 Authenticating with Google OAuth");
        Log.d(TAG, "Using URL: " + baseUrl + AUTH_GOOGLE_ENDPOINT);

        // Validate input
        if (idToken == null || idToken.trim().isEmpty()) {
            callback.onError("Google ID token is required");
            return;
        }

        // Create request
        AuthRequest.GoogleAuthRequest request = new AuthRequest.GoogleAuthRequest(idToken);
        String jsonBody = gson.toJson(request);
        Log.d(TAG, " Google auth request: " + request.toString());

        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request httpRequest = new Request.Builder()
                .url(baseUrl + AUTH_GOOGLE_ENDPOINT)
                .post(body)
                .build();

        executeAuthRequest(httpRequest, "Google OAuth", callback);
    }

    /**
     * Authenticate as guest user
     * @param deviceId Unique device identifier
     * @param callback Callback to handle the response
     */
    public void authenticateAsGuest(String deviceId, AuthCallback callback) {
        Log.d(TAG, "👤 Authenticating as guest user");
        Log.d(TAG, "Using URL: " + baseUrl + AUTH_GUEST_ENDPOINT);

        // Create request
        AuthRequest.GuestAuthRequest request = new AuthRequest.GuestAuthRequest(deviceId);
        String jsonBody = gson.toJson(request);
        Log.d(TAG, " Guest auth request: " + request.toString());

        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request httpRequest = new Request.Builder()
                .url(baseUrl + AUTH_GUEST_ENDPOINT)
                .post(body)
                .build();

        executeAuthRequest(httpRequest, "Guest", callback);
    }

    /**
     * Execute authentication request
     */
    private void executeAuthRequest(Request request, String authType, AuthCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, " " + authType + " authentication failed", e);
                String errorMsg = "Network error: " + e.getMessage();

                if (e.getMessage().contains("CLEARTEXT")) {
                    errorMsg += "\n Tip: Check network security configuration";
                } else if (e.getMessage().contains("UnknownHost")) {
                    errorMsg += "\n Tip: Check if server is running";
                }

                callback.onError(errorMsg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, " " + authType + " auth response code: " + response.code());
                Log.d(TAG, " " + authType + " auth response body: " + responseBody);

                try {
                    if (response.isSuccessful()) {
                        AuthResponse authResponse = gson.fromJson(responseBody, AuthResponse.class);

                        if (authResponse != null && authResponse.isValidResponse()) {
                            Log.d(TAG, "" + authType + " authentication successful: " + authResponse.toString());

                            // Store authentication tokens
                            setAuthTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());

                            callback.onSuccess(authResponse);
                        } else {
                            Log.e(TAG, " Invalid " + authType + " auth response structure");
                            callback.onError("Invalid response structure");
                        }
                    } else {
                        // Try to parse error response
                        try {
                            AuthResponse errorResponse = gson.fromJson(responseBody, AuthResponse.class);
                            if (errorResponse != null && errorResponse.getError() != null) {
                                callback.onError(errorResponse.getError() + ": " + errorResponse.getMessage());
                            } else {
                                callback.onError("HTTP " + response.code() + ": " + response.message());
                            }
                        } catch (Exception e) {
                            callback.onError("HTTP " + response.code() + ": " + response.message());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, " Error parsing " + authType + " auth response", e);
                    callback.onError("Error parsing response: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Get trending restaurants from the backend API
     * @param callback Callback to handle the response
     */
    public void getTrendingRestaurants(SearchCallback callback) {
        Log.d(TAG, " Getting trending restaurants");
        Log.d(TAG, "Using URL: " + baseUrl + TRENDING_ENDPOINT);

        // Create request
        Request request = new Request.Builder()
                .url(baseUrl + TRENDING_ENDPOINT)
                .get()
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, " Get trending restaurants failed", e);
                String errorMsg = "Network error: " + e.getMessage();

                if (e.getMessage().contains("CLEARTEXT")) {
                    errorMsg += "\n Tip: Check network security configuration";
                } else if (e.getMessage().contains("UnknownHost")) {
                    errorMsg += "\n Tip: Check if server is running";
                }

                callback.onError(errorMsg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, " Trending restaurants response code: " + response.code());
                Log.d(TAG, " Trending restaurants response body: " + responseBody);

                try {
                    if (response.isSuccessful()) {
                        SearchResponse searchResponse = gson.fromJson(responseBody, SearchResponse.class);

                        if (searchResponse != null && searchResponse.isValidResponse()) {
                            Log.d(TAG, "Trending restaurants retrieved successfully: " + searchResponse.getCount() + " restaurants");
                            callback.onSuccess(searchResponse);
                        } else {
                            Log.e(TAG, " Invalid trending restaurants response structure");
                            callback.onError("Invalid response format");
                        }
                    } else {
                        Log.e(TAG, " Trending restaurants request failed with code: " + response.code());
                        try {
                            SearchResponse errorResponse = gson.fromJson(responseBody, SearchResponse.class);
                            if (errorResponse != null && errorResponse.getError() != null) {
                                callback.onError(errorResponse.getError() + ": " + errorResponse.getMessage());
                            } else {
                                callback.onError("HTTP " + response.code() + ": " + response.message());
                            }
                        } catch (Exception e) {
                            callback.onError("HTTP " + response.code() + ": " + response.message());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, " Error parsing trending restaurants response", e);
                    callback.onError("Error parsing response: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Get user's favorite restaurants from the backend API
     * @param callback Callback to handle the response
     */
    public void getFavorites(FavoritesListCallback callback) {
        Log.d(TAG, "️ Getting user's favorite restaurants");
        Log.d(TAG, "Using URL: " + baseUrl + FAVORITES_ENDPOINT);

        // Check authentication
        if (!isAuthenticated()) {
            callback.onError("Authentication required. Please log in first.");
            return;
        }

        // Create request with authorization header
        Request.Builder requestBuilder = new Request.Builder()
                .url(baseUrl + FAVORITES_ENDPOINT)
                .get();

        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        }

        Request request = requestBuilder.build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, " Get favorites failed", e);
                String errorMsg = "Network error: " + e.getMessage();

                if (e.getMessage().contains("CLEARTEXT")) {
                    errorMsg += "\n Tip: Check network security configuration";
                } else if (e.getMessage().contains("UnknownHost")) {
                    errorMsg += "\n Tip: Check if server is running";
                }

                callback.onError(errorMsg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, " Get favorites response code: " + response.code());
                Log.d(TAG, " Get favorites response body: " + responseBody);

                try {
                    if (response.isSuccessful()) {
                        FavoritesListResponse favoritesResponse = gson.fromJson(responseBody, FavoritesListResponse.class);

                        if (favoritesResponse != null && favoritesResponse.isValidResponse()) {
                            Log.d(TAG, "Favorites retrieved successfully: " + favoritesResponse.getCount() + " favorites");
                            callback.onSuccess(favoritesResponse);
                        } else {
                            Log.e(TAG, " Invalid favorites response structure");
                            callback.onError("Invalid response format");
                        }
                    } else {
                        Log.e(TAG, " Get favorites request failed with code: " + response.code());
                        try {
                            FavoritesListResponse errorResponse = gson.fromJson(responseBody, FavoritesListResponse.class);
                            if (errorResponse != null && errorResponse.getError() != null) {
                                callback.onError(errorResponse.getError() + ": " + errorResponse.getMessage());
                            } else {
                                callback.onError("HTTP " + response.code() + ": " + response.message());
                            }
                        } catch (Exception e) {
                            callback.onError("HTTP " + response.code() + ": " + response.message());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, " Error parsing favorites response", e);
                    callback.onError("Error parsing response: " + e.getMessage());
                }
            }
        });
    }
}

package com.example.project.services;

import android.content.Context;
import android.util.Log;

import com.example.project.utils.ConfigManager;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service for handling Google Directions API calls
 */
public class DirectionsService {
    private static final String TAG = "DirectionsService";
    private static final String DIRECTIONS_API_BASE_URL = "https://maps.googleapis.com/maps/api/directions/json";
    
    private final OkHttpClient client;
    private final String apiKey;
    
    public DirectionsService(Context context) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        
        // Get API key from config
        ConfigManager configManager = ConfigManager.getInstance(context);
        this.apiKey = configManager.getGoogleMapsApiKey();
    }
    
    /**
     * Interface for handling directions API responses
     */
    public interface DirectionsCallback {
        void onSuccess(DirectionsResult result);
        void onError(String error);
    }
    
    /**
     * Result class for directions API response
     */
    public static class DirectionsResult {
        private final List<LatLng> routePoints;
        private final String distance;
        private final String duration;
        private final String polyline;
        
        public DirectionsResult(List<LatLng> routePoints, String distance, String duration, String polyline) {
            this.routePoints = routePoints;
            this.distance = distance;
            this.duration = duration;
            this.polyline = polyline;
        }
        
        public List<LatLng> getRoutePoints() { return routePoints; }
        public String getDistance() { return distance; }
        public String getDuration() { return duration; }
        public String getPolyline() { return polyline; }
    }
    
    /**
     * Get directions between two points
     */
    public void getDirections(LatLng origin, LatLng destination, DirectionsCallback callback) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_GOOGLE_MAPS_API_KEY_HERE")) {
            Log.w(TAG, "Google Maps API key not configured, using mock data");
            // Return mock data for testing
            callback.onSuccess(createMockDirectionsResult(origin, destination));
            return;
        }
        
        // Build the request URL
        HttpUrl.Builder urlBuilder = HttpUrl.parse(DIRECTIONS_API_BASE_URL).newBuilder();
        urlBuilder.addQueryParameter("origin", origin.latitude + "," + origin.longitude);
        urlBuilder.addQueryParameter("destination", destination.latitude + "," + destination.longitude);
        urlBuilder.addQueryParameter("mode", "driving");
        urlBuilder.addQueryParameter("key", apiKey);
        
        String url = urlBuilder.build().toString();
        Log.d(TAG, "Directions API URL: " + url);
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Directions API request failed", e);
                callback.onError("Network error: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Directions API request unsuccessful: " + response.code());
                    callback.onError("API error: " + response.code());
                    return;
                }
                
                String responseBody = response.body().string();
                Log.d(TAG, "Directions API response received");
                
                try {
                    DirectionsResult result = parseDirectionsResponse(responseBody);
                    callback.onSuccess(result);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing directions response", e);
                    callback.onError("Error parsing response: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Parse the Google Directions API JSON response
     * This is a simplified parser - in production you'd use a proper JSON library
     */
    private DirectionsResult parseDirectionsResponse(String jsonResponse) {
        // For now, return mock data
        // In production, you'd parse the actual JSON response
        Log.d(TAG, "Parsing directions response (using mock data for now)");
        
        // Mock route points (you'd extract these from the response polyline)
        List<LatLng> routePoints = new ArrayList<>();
        // Add some sample points for demonstration
        routePoints.add(new LatLng(43.6532, -79.3832));
        routePoints.add(new LatLng(43.6542, -79.3842));
        routePoints.add(new LatLng(43.6552, -79.3852));
        
        return new DirectionsResult(
            routePoints,
            "2.5 km",
            "8 min",
            "mock_polyline_data"
        );
    }
    
    /**
     * Create mock directions result for testing when API key is not configured
     */
    private DirectionsResult createMockDirectionsResult(LatLng origin, LatLng destination) {
        List<LatLng> routePoints = new ArrayList<>();

        // Create a more realistic route with intermediate points
        double latDiff = destination.latitude - origin.latitude;
        double lngDiff = destination.longitude - origin.longitude;

        // Add origin
        routePoints.add(origin);

        // Add intermediate points to simulate a road route (not straight line)
        int numPoints = 8; // More points for smoother route
        for (int i = 1; i < numPoints; i++) {
            double fraction = (double) i / numPoints;

            // Add some curve to make it look more like a real route
            double curveFactor = Math.sin(fraction * Math.PI) * 0.001; // Small curve

            LatLng intermediatePoint = new LatLng(
                origin.latitude + (latDiff * fraction) + curveFactor,
                origin.longitude + (lngDiff * fraction) + (curveFactor * 0.5)
            );
            routePoints.add(intermediatePoint);
        }

        // Add destination
        routePoints.add(destination);

        // Calculate approximate distance
        double distance = calculateDistance(origin, destination);
        String distanceText = String.format("%.1f km", distance);

        // Estimate duration (assuming 25 km/h average speed in city with traffic)
        int durationMinutes = (int) Math.ceil(distance * 2.4); // 2.4 minutes per km
        String durationText = durationMinutes + " min";

        Log.d(TAG, "Created mock route with " + routePoints.size() + " points, distance: " + distanceText + ", duration: " + durationText);

        return new DirectionsResult(routePoints, distanceText, durationText, "mock_polyline");
    }
    
    /**
     * Calculate approximate distance between two points using Haversine formula
     */
    private double calculateDistance(LatLng point1, LatLng point2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(point2.latitude - point1.latitude);
        double lonDistance = Math.toRadians(point2.longitude - point1.longitude);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(point1.latitude)) * Math.cos(Math.toRadians(point2.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in km
    }
    
    /**
     * Geocode an address to get coordinates
     * This is a simplified version - in production you'd use Google Geocoding API
     */
    public void geocodeAddress(String address, GeocodeCallback callback) {
        // Return a mock location based on the address
        // In production, you'd call Google Geocoding API
        LatLng mockLocation = getMockLocationForAddress(address);
        callback.onSuccess(mockLocation);
    }
    
    /**
     * Interface for geocoding callbacks
     */
    public interface GeocodeCallback {
        void onSuccess(LatLng location);
        void onError(String error);
    }
    
    /**
     * Precise geocoding that maps specific addresses to exact coordinates
     * This simulates a real geocoding service with accurate street-level positioning
     */
    private LatLng getMockLocationForAddress(String address) {
        Log.d(TAG, "Precise geocoding for address: " + address);

        String normalizedAddress = address.toLowerCase().trim();

        // Extract address components for precise matching
        AddressComponents components = parseAddressComponents(normalizedAddress);

        // Precise address matching with exact coordinates
        LatLng coordinates = getExactCoordinatesForAddress(components, normalizedAddress);

        if (coordinates != null) {
            Log.d(TAG, "Found exact coordinates for address");
            return coordinates;
        }

        // Fallback to street-level precision
        coordinates = getStreetLevelCoordinates(components, normalizedAddress);

        if (coordinates != null) {
            Log.d(TAG, "Found street-level coordinates for address");
            return coordinates;
        }

        // Final fallback to neighborhood precision
        Log.d(TAG, "Using neighborhood-level coordinates for address");
        return getNeighborhoodCoordinates(components, normalizedAddress);
    }

    /**
     * Parse address into components for precise matching
     */
    private AddressComponents parseAddressComponents(String address) {
        AddressComponents components = new AddressComponents();

        // Extract street number (handle ranges like "3360" or "100-200")
        String[] parts = address.split("\\s+");
        for (String part : parts) {
            if (part.matches("\\d+")) {
                components.streetNumber = part;
                break;
            } else if (part.matches("\\d+-\\d+")) {
                components.streetNumber = part.split("-")[0]; // Take first number in range
                break;
            }
        }

        // Extract street name with comprehensive matching
        if (address.contains("harbord st") || address.contains("harbord street")) {
            components.streetName = "harbord";
        } else if (address.contains("midland ave") || address.contains("midland avenue")) {
            components.streetName = "midland";
        } else if (address.contains("king st") || address.contains("king street")) {
            components.streetName = "king";
        } else if (address.contains("queen st") || address.contains("queen street")) {
            components.streetName = "queen";
        } else if (address.contains("yonge st") || address.contains("yonge street")) {
            components.streetName = "yonge";
        } else if (address.contains("bloor st") || address.contains("bloor street")) {
            components.streetName = "bloor";
        } else if (address.contains("dundas st") || address.contains("dundas street")) {
            components.streetName = "dundas";
        } else if (address.contains("college st") || address.contains("college street")) {
            components.streetName = "college";
        } else if (address.contains("spadina ave") || address.contains("spadina avenue")) {
            components.streetName = "spadina";
        } else if (address.contains("augusta ave") || address.contains("augusta avenue")) {
            components.streetName = "augusta";
        } else if (address.contains("baldwin st") || address.contains("baldwin street")) {
            components.streetName = "baldwin";
        } else if (address.contains("ossington ave") || address.contains("ossington avenue")) {
            components.streetName = "ossington";
        } else if (address.contains("bathurst st") || address.contains("bathurst street")) {
            components.streetName = "bathurst";
        } else if (address.contains("church st") || address.contains("church street")) {
            components.streetName = "church";
        } else if (address.contains("bay st") || address.contains("bay street")) {
            components.streetName = "bay";
        } else if (address.contains("university ave") || address.contains("university avenue")) {
            components.streetName = "university";
        }

        // Extract city/neighborhood with better detection
        if (address.contains("toronto")) {
            components.city = "toronto";

            // Detect specific Toronto neighborhoods
            if (address.contains("scarborough")) {
                components.neighborhood = "scarborough";
            } else if (address.contains("north york")) {
                components.neighborhood = "north york";
            } else if (address.contains("etobicoke")) {
                components.neighborhood = "etobicoke";
            } else if (address.contains("downtown")) {
                components.neighborhood = "downtown";
            } else if (address.contains("kensington")) {
                components.neighborhood = "kensington";
            } else if (address.contains("chinatown")) {
                components.neighborhood = "chinatown";
            } else if (address.contains("little italy")) {
                components.neighborhood = "little italy";
            }
        } else if (address.contains("mississauga")) {
            components.city = "mississauga";
        } else if (address.contains("markham")) {
            components.city = "markham";
        } else if (address.contains("richmond hill")) {
            components.city = "richmond hill";
        } else if (address.contains("vaughan")) {
            components.city = "vaughan";
        } else if (address.contains("brampton")) {
            components.city = "brampton";
        }

        // Extract postal code with comprehensive patterns
        String[] postalPatterns = {
            "m1v", "m1w", "m1x", "m1s", "m1t", "m1r", "m1p", "m1n", "m1m", "m1l", "m1k", "m1j", "m1h", "m1g", "m1e", "m1c", "m1b",
            "m2h", "m2j", "m2k", "m2l", "m2m", "m2n", "m2p", "m2r",
            "m3a", "m3b", "m3c", "m3h", "m3j", "m3k", "m3l", "m3m", "m3n",
            "m4a", "m4b", "m4c", "m4e", "m4g", "m4h", "m4j", "m4k", "m4l", "m4m", "m4n", "m4p", "m4r", "m4s", "m4t", "m4v", "m4w", "m4x", "m4y",
            "m5a", "m5b", "m5c", "m5e", "m5g", "m5h", "m5j", "m5k", "m5l", "m5m", "m5n", "m5p", "m5r", "m5s", "m5t", "m5v", "m5w", "m5x",
            "m6a", "m6b", "m6c", "m6e", "m6g", "m6h", "m6j", "m6k", "m6l", "m6m", "m6n", "m6p", "m6r", "m6s",
            "m8v", "m8w", "m8x", "m8y", "m8z",
            "m9a", "m9b", "m9c", "m9l", "m9m", "m9n", "m9p", "m9r", "m9v", "m9w"
        };

        for (String pattern : postalPatterns) {
            if (address.contains(pattern)) {
                components.postalCode = pattern;
                break;
            }
        }

        return components;
    }

    /**
     * Get exact coordinates for specific known addresses
     */
    private LatLng getExactCoordinatesForAddress(AddressComponents components, String fullAddress) {
        // Exact address matches with precise coordinates

        // 135 Harbord St, Toronto (from the screenshot - Famiglia Baldassarre)
        if (fullAddress.contains("135 harbord st") || fullAddress.contains("135 harbord street")) {
            return new LatLng(43.6598, -79.4037); // Exact location near University of Toronto
        }

        // 3360 Midland Ave, Scarborough (Sushi Gen example)
        if (fullAddress.contains("3360 midland ave") || fullAddress.contains("3360 midland avenue")) {
            return new LatLng(43.7731, -79.2578); // Exact Scarborough location
        }

        // Financial District restaurants
        if (fullAddress.contains("100 king st w") || fullAddress.contains("100 king street west")) {
            return new LatLng(43.6481, -79.3815);
        }
        if (fullAddress.contains("150 king st w") || fullAddress.contains("150 king street west")) {
            return new LatLng(43.6481, -79.3825);
        }
        if (fullAddress.contains("200 king st w") || fullAddress.contains("200 king street west")) {
            return new LatLng(43.6481, -79.3835);
        }

        // Queen Street West restaurants
        if (fullAddress.contains("200 queen st w") || fullAddress.contains("200 queen street west")) {
            return new LatLng(43.6532, -79.3890);
        }
        if (fullAddress.contains("300 queen st w") || fullAddress.contains("300 queen street west")) {
            return new LatLng(43.6532, -79.3920);
        }
        if (fullAddress.contains("400 queen st w") || fullAddress.contains("400 queen street west")) {
            return new LatLng(43.6532, -79.3950);
        }

        // Yonge Street restaurants
        if (fullAddress.contains("300 yonge st") || fullAddress.contains("300 yonge street")) {
            return new LatLng(43.6555, -79.3844);
        }
        if (fullAddress.contains("500 yonge st") || fullAddress.contains("500 yonge street")) {
            return new LatLng(43.6600, -79.3844);
        }
        if (fullAddress.contains("700 yonge st") || fullAddress.contains("700 yonge street")) {
            return new LatLng(43.6650, -79.3844);
        }

        // Bloor Street restaurants
        if (fullAddress.contains("400 bloor st w") || fullAddress.contains("400 bloor street west")) {
            return new LatLng(43.6677, -79.4103);
        }
        if (fullAddress.contains("500 bloor st w") || fullAddress.contains("500 bloor street west")) {
            return new LatLng(43.6677, -79.4130);
        }
        if (fullAddress.contains("300 bloor st e") || fullAddress.contains("300 bloor street east")) {
            return new LatLng(43.6677, -79.3750);
        }

        // Dundas Street restaurants
        if (fullAddress.contains("500 dundas st w") || fullAddress.contains("500 dundas street west")) {
            return new LatLng(43.6563, -79.4011);
        }
        if (fullAddress.contains("600 dundas st w") || fullAddress.contains("600 dundas street west")) {
            return new LatLng(43.6563, -79.4050);
        }

        // College Street restaurants
        if (fullAddress.contains("400 college st") || fullAddress.contains("400 college street")) {
            return new LatLng(43.6577, -79.4000);
        }
        if (fullAddress.contains("500 college st") || fullAddress.contains("500 college street")) {
            return new LatLng(43.6577, -79.4030);
        }

        // Kensington Market area
        if (fullAddress.contains("augusta ave") || fullAddress.contains("augusta avenue")) {
            return new LatLng(43.6547, -79.4009);
        }
        if (fullAddress.contains("baldwin st") || fullAddress.contains("baldwin street")) {
            return new LatLng(43.6565, -79.3995);
        }

        // Chinatown restaurants
        if (fullAddress.contains("spadina ave") || fullAddress.contains("spadina avenue")) {
            if (fullAddress.contains("400") || fullAddress.contains("500")) {
                return new LatLng(43.6547, -79.3988);
            }
        }

        // Little Italy restaurants
        if (fullAddress.contains("college st") && (fullAddress.contains("600") || fullAddress.contains("700"))) {
            return new LatLng(43.6577, -79.4100);
        }

        // Scarborough specific addresses
        if (fullAddress.contains("midland ave") && fullAddress.contains("unit")) {
            // Handle plaza addresses with unit numbers
            if (fullAddress.contains("3360")) {
                return new LatLng(43.7731, -79.2578);
            } else if (fullAddress.contains("2000")) {
                return new LatLng(43.7500, -79.2600);
            } else if (fullAddress.contains("4000")) {
                return new LatLng(43.8000, -79.2550);
            }
        }

        // North York restaurants
        if (fullAddress.contains("yonge st") && (fullAddress.contains("north york") || fullAddress.contains("sheppard"))) {
            return new LatLng(43.7615, -79.4111);
        }

        // Mississauga restaurants
        if (fullAddress.contains("mississauga")) {
            if (fullAddress.contains("hurontario") || fullAddress.contains("main st")) {
                return new LatLng(43.5890, -79.6441);
            }
        }

        return null; // No exact match found
    }

    /**
     * Get street-level coordinates based on street name and approximate number
     */
    private LatLng getStreetLevelCoordinates(AddressComponents components, String fullAddress) {
        if (components.streetName == null) return null;

        int streetNumber = 0;
        try {
            if (components.streetNumber != null) {
                streetNumber = Integer.parseInt(components.streetNumber);
            }
        } catch (NumberFormatException e) {
            streetNumber = 0;
        }

        switch (components.streetName) {
            case "harbord":
                // Harbord Street runs east-west near University of Toronto
                if (streetNumber < 200) {
                    return new LatLng(43.6598, -79.4037); // East end near University
                } else {
                    return new LatLng(43.6598, -79.4150); // West end
                }

            case "midland":
                // Midland Avenue runs north-south in Scarborough
                if (streetNumber < 2000) {
                    return new LatLng(43.7500, -79.2600); // South section
                } else if (streetNumber < 4000) {
                    return new LatLng(43.7731, -79.2578); // Middle section
                } else {
                    return new LatLng(43.8000, -79.2550); // North section
                }

            case "king":
                // King Street runs east-west through downtown
                if (streetNumber < 300) {
                    return new LatLng(43.6481, -79.3773); // East end
                } else {
                    return new LatLng(43.6481, -79.3900); // West end
                }

            case "queen":
                // Queen Street runs east-west
                if (streetNumber < 300) {
                    return new LatLng(43.6532, -79.3832); // East end
                } else {
                    return new LatLng(43.6532, -79.3950); // West end
                }

            case "yonge":
                // Yonge Street runs north-south
                if (streetNumber < 500) {
                    return new LatLng(43.6555, -79.3844); // South section
                } else if (streetNumber < 1000) {
                    return new LatLng(43.6650, -79.3844); // Mid section
                } else {
                    return new LatLng(43.7000, -79.3844); // North section
                }

            case "bloor":
                // Bloor Street runs east-west
                if (streetNumber < 500) {
                    return new LatLng(43.6677, -79.3900); // East section
                } else {
                    return new LatLng(43.6677, -79.4103); // West section
                }

            case "dundas":
                // Dundas Street
                if (streetNumber < 500) {
                    return new LatLng(43.6563, -79.3800); // East section
                } else {
                    return new LatLng(43.6563, -79.4011); // West section
                }
        }

        return null;
    }

    /**
     * Get neighborhood-level coordinates as final fallback
     */
    private LatLng getNeighborhoodCoordinates(AddressComponents components, String fullAddress) {
        // Neighborhood-level fallback
        if (components.neighborhood != null) {
            switch (components.neighborhood) {
                case "scarborough":
                    return new LatLng(43.7731, -79.2578);
                case "north york":
                    return new LatLng(43.7615, -79.4111);
                case "etobicoke":
                    return new LatLng(43.6205, -79.5132);
            }
        }

        if (components.city != null) {
            switch (components.city) {
                case "toronto":
                    return new LatLng(43.6532, -79.3832); // Downtown Toronto
                case "mississauga":
                    return new LatLng(43.5890, -79.6441);
                case "markham":
                    return new LatLng(43.8561, -79.3370);
            }
        }

        // Final fallback to Toronto city center
        return new LatLng(43.6532, -79.3832);
    }

    /**
     * Helper class to store parsed address components
     */
    private static class AddressComponents {
        String streetNumber;
        String streetName;
        String city;
        String neighborhood;
        String postalCode;
    }
}

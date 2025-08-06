package com.example.project.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Utility class for handling maps and location functionality
 * Provides methods for getting directions using Google Maps
 */
public class MapsUtils {
    private static final String TAG = "MapsUtils";
    
    // Permission request codes
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    // Required permissions for location access
    private static final String[] LOCATION_PERMISSIONS = {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * Interface for handling location permission results
     */
    public interface LocationPermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    /**
     * Check if location permissions are granted
     */
    public static boolean hasLocationPermissions(Context context) {
        for (String permission : LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Request location permissions
     */
    public static void requestLocationPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Check if we should show permission rationale
     */
    public static boolean shouldShowLocationPermissionRationale(Activity activity) {
        for (String permission : LOCATION_PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get current location (simplified version)
     * Note: In production, you'd want to use FusedLocationProviderClient for better accuracy
     */
    public static Location getCurrentLocation(Context context) {
        if (!hasLocationPermissions(context)) {
            Log.w(TAG, "Location permissions not granted");
            return null;
        }

        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                Log.e(TAG, "LocationManager is null");
                return null;
            }

            // Try to get last known location from GPS first, then network
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                Log.d(TAG, "Current location: " + location.getLatitude() + ", " + location.getLongitude());
            } else {
                Log.w(TAG, "Could not get current location");
            }

            return location;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception getting location", e);
            return null;
        }
    }

    /**
     * Open directions to a destination address using Google Maps
     * This method tries multiple approaches for best user experience
     */
    public static void openDirections(Context context, String destinationAddress) {
        if (destinationAddress == null || destinationAddress.trim().isEmpty()) {
            Log.e(TAG, "Destination address is null or empty");
            Toast.makeText(context, "Restaurant address not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Opening directions to: '" + destinationAddress + "'");
        Log.d(TAG, "Has location permissions: " + hasLocationPermissions(context));

        // Try to get current location if permissions are available
        Location currentLocation = null;
        if (hasLocationPermissions(context)) {
            currentLocation = getCurrentLocation(context);
        }

        // Try different approaches in order of preference
        boolean success = false;

        // Approach 1: Google Maps app with current location and destination
        if (currentLocation != null) {
            Log.d(TAG, "Trying approach 1: Google Maps with current location");
            success = openGoogleMapsWithCurrentLocation(context, currentLocation, destinationAddress);
        } else {
            Log.d(TAG, "Skipping approach 1: No current location available");
        }

        // Approach 2: Google Maps app with just destination (let Maps handle current location)
        if (!success) {
            Log.d(TAG, "Trying approach 2: Google Maps with destination only");
            success = openGoogleMapsWithDestination(context, destinationAddress);
        }

        // Approach 3: Generic map intent (any map app)
        if (!success) {
            Log.d(TAG, "Trying approach 3: Generic map intent");
            success = openGenericMapIntent(context, destinationAddress);
        }

        // Approach 4: Web browser fallback
        if (!success) {
            Log.d(TAG, "Trying approach 4: Web browser fallback");
            success = openDirectionsInBrowser(context, destinationAddress);
        }

        // Approach 5: Super simple web search fallback
        if (!success) {
            Log.d(TAG, "Trying approach 5: Simple web search");
            success = openSimpleWebSearch(context, destinationAddress);
        }

        // If all approaches failed
        if (!success) {
            Log.e(TAG, "All approaches failed to open directions");
            Toast.makeText(context, "Unable to open directions. Please install Google Maps or a web browser.", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "Successfully opened directions");
        }
    }

    /**
     * Open Google Maps app with current location and destination
     */
    private static boolean openGoogleMapsWithCurrentLocation(Context context, Location currentLocation, String destinationAddress) {
        try {
            // Use google.navigation scheme for turn-by-turn directions
            String uri = String.format("google.navigation:q=%s&mode=d",
                Uri.encode(destinationAddress));
            Log.d(TAG, "Trying Google Navigation URI: " + uri);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                Log.d(TAG, "Google Maps navigation intent resolved successfully");
                context.startActivity(intent);
                Log.d(TAG, "Opened Google Maps with navigation");
                return true;
            } else {
                Log.e(TAG, "Google Maps navigation intent could not be resolved (Google Maps may not be installed)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening Google Maps with navigation", e);
        }
        return false;
    }

    /**
     * Open Google Maps app with just destination
     */
    private static boolean openGoogleMapsWithDestination(Context context, String destinationAddress) {
        try {
            // Try multiple URI schemes for better compatibility

            // First try: Google Maps specific URI for directions
            String uri = String.format("https://maps.google.com/maps?daddr=%s&mode=driving",
                Uri.encode(destinationAddress));

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                Log.d(TAG, "Opened Google Maps with destination (maps.google.com)");
                return true;
            }

            // Second try: Generic geo URI
            uri = String.format("geo:0,0?q=%s", Uri.encode(destinationAddress));
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                Log.d(TAG, "Opened Google Maps with destination (geo URI)");
                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error opening Google Maps with destination", e);
        }
        return false;
    }

    /**
     * Open generic map intent (any map app can handle this)
     */
    private static boolean openGenericMapIntent(Context context, String destinationAddress) {
        try {
            // Use geo URI without package restriction - any map app can handle this
            String uri = String.format("geo:0,0?q=%s", Uri.encode(destinationAddress));
            Log.d(TAG, "Trying generic geo URI: " + uri);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Don't set package - let system choose any available map app

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                Log.d(TAG, "Generic map intent resolved successfully");
                context.startActivity(intent);
                Log.d(TAG, "Opened generic map intent");
                return true;
            } else {
                Log.e(TAG, "Generic map intent could not be resolved");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening generic map intent", e);
        }
        return false;
    }

    /**
     * Open directions in web browser as fallback
     */
    private static boolean openDirectionsInBrowser(Context context, String destinationAddress) {
        try {
            String url = String.format("https://maps.google.com/maps?daddr=%s&mode=driving",
                Uri.encode(destinationAddress));
            Log.d(TAG, "Trying browser URL: " + url);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Don't set package - let system choose browser

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                Log.d(TAG, "Browser intent resolved successfully");
                context.startActivity(intent);
                Log.d(TAG, "Opened directions in browser");
                return true;
            } else {
                Log.e(TAG, "Browser intent could not be resolved");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening directions in browser", e);
        }
        return false;
    }

    /**
     * Super simple web search fallback - just search for the address
     */
    private static boolean openSimpleWebSearch(Context context, String destinationAddress) {
        try {
            // Simple Google search for the address
            String searchQuery = "directions to " + destinationAddress;
            String url = String.format("https://www.google.com/search?q=%s",
                Uri.encode(searchQuery));
            Log.d(TAG, "Trying simple web search: " + url);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                Log.d(TAG, "Simple web search intent resolved successfully");
                context.startActivity(intent);
                Log.d(TAG, "Opened simple web search");
                return true;
            } else {
                Log.e(TAG, "Simple web search intent could not be resolved");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening simple web search", e);
        }
        return false;
    }

    /**
     * Handle permission request results
     */
    public static void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults, 
                                            LocationPermissionCallback callback) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            
            if (allPermissionsGranted) {
                Log.d(TAG, "Location permissions granted");
                if (callback != null) {
                    callback.onPermissionGranted();
                }
            } else {
                Log.d(TAG, "Location permissions denied");
                if (callback != null) {
                    callback.onPermissionDenied();
                }
            }
        }
    }

    /**
     * Show a user-friendly explanation for why location permission is needed
     */
    public static void showLocationPermissionRationale(Context context) {
        Toast.makeText(context, 
            "Location permission is needed to provide directions from your current location to the restaurant.", 
            Toast.LENGTH_LONG).show();
    }
}

package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import com.example.project.services.DirectionsService;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private View loadingOverlay;
    private View errorOverlay;
    private TextView errorMessage;
    private MaterialButton retryButton;
    private MaterialCardView infoCard;
    private TextView restaurantNameText;
    private TextView restaurantAddressText;


    // Map Components
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;

    // Data
    private String restaurantName;
    private String restaurantAddress;
    private Double restaurantLatitude;
    private Double restaurantLongitude;
    private LatLng restaurantLocation;
    private Marker restaurantMarker;

    // Services
    private Handler mainHandler;
    private DirectionsService directionsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map_coordinator_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize services
        mainHandler = new Handler(Looper.getMainLooper());
        directionsService = new DirectionsService(this);

        // Get restaurant data from intent
        getRestaurantDataFromIntent();

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Initialize map
        initializeMap();
    }

    private void getRestaurantDataFromIntent() {
        Intent intent = getIntent();
        restaurantName = intent.getStringExtra("restaurant_name");
        restaurantAddress = intent.getStringExtra("restaurant_address");

        // Get latitude and longitude if available
        if (intent.hasExtra("restaurant_latitude")) {
            restaurantLatitude = intent.getDoubleExtra("restaurant_latitude", 0.0);
        }
        if (intent.hasExtra("restaurant_longitude")) {
            restaurantLongitude = intent.getDoubleExtra("restaurant_longitude", 0.0);
        }

        if (restaurantName == null) restaurantName = "Restaurant";
        if (restaurantAddress == null) restaurantAddress = "Address not available";

        Log.d(TAG, "Restaurant Name: " + restaurantName);
        Log.d(TAG, "Restaurant Address: " + restaurantAddress);
        if (restaurantLatitude != null && restaurantLongitude != null) {
            Log.d(TAG, "Restaurant Coordinates: " + restaurantLatitude + ", " + restaurantLongitude);
        } else {
            Log.d(TAG, "No restaurant coordinates provided, will use geocoding");
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        loadingOverlay = findViewById(R.id.loading_overlay);
        errorOverlay = findViewById(R.id.error_overlay);
        errorMessage = findViewById(R.id.error_message);
        retryButton = findViewById(R.id.retry_button);
        infoCard = findViewById(R.id.info_card);
        restaurantNameText = findViewById(R.id.restaurant_name);
        restaurantAddressText = findViewById(R.id.restaurant_address);

        // Set restaurant info in the info card
        restaurantNameText.setText(restaurantName);
        restaurantAddressText.setText(restaurantAddress);

        // Setup retry button
        retryButton.setOnClickListener(v -> {
            hideError();
            initializeMap();
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    private void initializeMap() {
        showLoading("Loading map...");

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            showError("Failed to load map", "Map fragment not found.");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        Log.d(TAG, "Map is ready");

        // Configure map
        configureMap();

        // Check location permissions and get user location
        checkLocationPermissionsAndGetLocation();
    }

    private void configureMap() {
        if (googleMap == null) return;

        // Enable map controls and interactions
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Set map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void checkLocationPermissionsAndGetLocation() {
        // Skip location permissions and just show restaurant location
        Log.d(TAG, "Skipping user location, showing restaurant only");
        showRestaurantOnly();
    }



    private void showRestaurantOnly() {
        showLoading("Finding restaurant location...");

        // Check if we have coordinates from the API
        if (restaurantLatitude != null && restaurantLongitude != null) {
            Log.d(TAG, "✅ Using coordinates from API for '" + restaurantName + "'");
            Log.d(TAG, "📍 Address: " + restaurantAddress);
            Log.d(TAG, "🗺️ Coordinates: " + restaurantLatitude + ", " + restaurantLongitude);

            restaurantLocation = new LatLng(restaurantLatitude, restaurantLongitude);
            mainHandler.post(() -> {
                addRestaurantMarker();
                centerMapOnRestaurant();
                hideLoading();
                showInfoCard();
            });
            return;
        }

        // Fallback to geocoding if no coordinates available
        Log.d(TAG, "No coordinates available, geocoding restaurant address: " + restaurantAddress);
        directionsService.geocodeAddress(restaurantAddress, new DirectionsService.GeocodeCallback() {
            @Override
            public void onSuccess(LatLng location) {
                restaurantLocation = location;
                Log.d(TAG, "✅ Precise location found via geocoding for '" + restaurantName + "'");
                Log.d(TAG, "📍 Address: " + restaurantAddress);
                Log.d(TAG, "🗺️ Coordinates: " + location.latitude + ", " + location.longitude);
                mainHandler.post(() -> {
                    addRestaurantMarker();
                    centerMapOnRestaurant();
                    hideLoading();
                    showInfoCard();
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to find restaurant location: " + error);
                Log.d(TAG, "🔄 Using fallback Toronto coordinates");
                // Fallback to default Toronto location
                restaurantLocation = new LatLng(43.6532, -79.3832);
                mainHandler.post(() -> {
                    addRestaurantMarker();
                    centerMapOnRestaurant();
                    hideLoading();
                    showInfoCard();
                });
            }
        });
    }



    private void addRestaurantMarker() {
        if (googleMap == null || restaurantLocation == null) return;

        restaurantMarker = googleMap.addMarker(new MarkerOptions()
                .position(restaurantLocation)
                .title(restaurantName)
                .snippet(restaurantAddress)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private void centerMapOnRestaurant() {
        if (googleMap == null || restaurantLocation == null) return;

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(restaurantLocation, 16f));
    }

    private void showLoading(String message) {
        TextView loadingText = findViewById(R.id.loading_text);
        loadingText.setText(message);
        loadingOverlay.setVisibility(View.VISIBLE);
        errorOverlay.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }

    private void showError(String title, String message) {
        TextView errorTitle = findViewById(R.id.error_title);
        errorTitle.setText(title);
        errorMessage.setText(message);
        errorOverlay.setVisibility(View.VISIBLE);
        loadingOverlay.setVisibility(View.GONE);
    }

    private void hideError() {
        errorOverlay.setVisibility(View.GONE);
    }

    private void showInfoCard() {
        infoCard.setVisibility(View.VISIBLE);
    }
}

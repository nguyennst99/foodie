package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project.models.FavoriteRequest;
import com.example.project.models.FavoriteResponse;
import com.example.project.models.Restaurant;
import com.example.project.network.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class RestaurantDetailActivity extends AppCompatActivity {

    private static final String TAG = "RestaurantDetailActivity";

    private ImageView restaurantHeroImage;
    private TextView restaurantName, restaurantDescription, restaurantInfo, restaurantHours, restaurantRating, restaurantPhone;
    private MaterialCardView shareButton;
    private MaterialButton directionsButton, favoriteButton;

    private ApiClient apiClient;
    private Handler mainHandler;
    private Restaurant currentRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.restaurant_detail_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize API client and handler
        apiClient = ApiClient.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());

        initializeViews();
        loadRestaurantData();
        setupClickListeners();
    }
    
    private void initializeViews() {
        restaurantHeroImage = findViewById(R.id.restaurant_hero_image);
        restaurantName = findViewById(R.id.restaurant_name);
        restaurantDescription = findViewById(R.id.restaurant_description);
        restaurantInfo = findViewById(R.id.restaurant_info);
        restaurantHours = findViewById(R.id.restaurant_hours);
        restaurantRating = findViewById(R.id.restaurant_rating);
        restaurantPhone = findViewById(R.id.restaurant_phone);
        shareButton = findViewById(R.id.share_button);
        directionsButton = findViewById(R.id.directions_button);
        favoriteButton = findViewById(R.id.favorite_button);
    }
    
    private void loadRestaurantData() {
        // Get restaurant data from Intent extras
        Intent intent = getIntent();
        String name = intent.getStringExtra("restaurant_name");
        String description = intent.getStringExtra("restaurant_description");
        String info = intent.getStringExtra("restaurant_info");
        String hours = intent.getStringExtra("restaurant_hours");
        String rating = intent.getStringExtra("restaurant_rating");
        String phone = intent.getStringExtra("restaurant_phone");
        int imageResource = intent.getIntExtra("restaurant_image", R.drawable.restaurant_golden_spoon);

        // Get latitude and longitude if available
        Double latitude = null;
        Double longitude = null;
        if (intent.hasExtra("restaurant_latitude")) {
            latitude = intent.getDoubleExtra("restaurant_latitude", 0.0);
        }
        if (intent.hasExtra("restaurant_longitude")) {
            longitude = intent.getDoubleExtra("restaurant_longitude", 0.0);
        }

        // Set default values if no data is passed
        if (name == null) name = "The Golden Spoon";
        if (description == null) description = "The Golden Spoon offers a delightful Italian dining experience with a menu featuring classic pasta dishes, wood-fired pizzas, and fresh seafood. Enjoy a cozy atmosphere and attentive service.";
        if (info == null) info = "123 Main Street, Anytown | Italian | $$";
        if (hours == null) hours = "Open today: 11:00 AM - 10:00 PM";
        if (rating == null) rating = "4.5";
        if (phone == null) phone = "Phone: (555) 123-4567";

        // Create Restaurant object for favorites functionality
        try {
            currentRestaurant = new Restaurant();
            currentRestaurant.setName(name);
            currentRestaurant.setDescription(description);
            currentRestaurant.setRating(Double.parseDouble(rating));

            // Extract address from info (format: "address | cuisine | price")
            Log.d(TAG, "Raw restaurant_info: '" + info + "'");
            String[] infoParts = info.split(" \\| ");
            Log.d(TAG, "Split into " + infoParts.length + " parts");

            if (infoParts.length > 0) {
                String address = infoParts[0].trim();
                currentRestaurant.setAddress(address);
                Log.d(TAG, "Extracted address: '" + address + "'");
            }
            if (infoParts.length > 1) {
                currentRestaurant.setCuisineType(infoParts[1]);
                Log.d(TAG, "Extracted cuisine: '" + infoParts[1] + "'");
            }

            // Extract phone number (remove "Phone: " prefix if present)
            String phoneNumber = phone.replace("Phone: ", "");
            currentRestaurant.setPhone(phoneNumber);

            // Set default hours map
            java.util.Map<String, String> hoursMap = new java.util.HashMap<>();
            hoursMap.put("monday", "11:00 AM - 10:00 PM");
            currentRestaurant.setHours(hoursMap);

            // Set coordinates if available
            if (latitude != null && longitude != null) {
                currentRestaurant.setLatitude(latitude);
                currentRestaurant.setLongitude(longitude);
                Log.d(TAG, "Set restaurant coordinates: " + latitude + ", " + longitude);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creating Restaurant object: " + e.getMessage());
        }

        // Update UI with restaurant data
        restaurantName.setText(name);
        restaurantDescription.setText(description);
        restaurantInfo.setText(info);
        restaurantHours.setText(hours);
        restaurantRating.setText(rating);
        restaurantPhone.setText(phone);
        restaurantHeroImage.setImageResource(imageResource);
    }
    
    private void setupClickListeners() {
        // Back arrow click - return to previous activity
        ImageView backArrow = findViewById(R.id.back_arrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> {
                finish(); // Close this activity and return to previous
            });
        }
        
        // Share button click
        if (shareButton != null) {
            shareButton.setOnClickListener(v -> {
                shareRestaurant();
            });
        }
        
        // Directions button click
        if (directionsButton != null) {
            directionsButton.setOnClickListener(v -> {
                openDirectionsToRestaurant();
            });
        }
        
        // Favorite button click
        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(v -> {
                addRestaurantToFavorites();
            });
        }
    }
    
    private void shareRestaurant() {
        String restaurantNameText = restaurantName.getText().toString();
        String descriptionText = restaurantDescription.getText().toString();
        String infoText = restaurantInfo.getText().toString();

        String shareText = "Check out " + restaurantNameText + "!\n\n" +
                          descriptionText + "\n\n" +
                          infoText + "\n\n" +
                          "Shared from Foodie App";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Restaurant Recommendation");

        startActivity(Intent.createChooser(shareIntent, "Share Restaurant"));
    }

    /**
     * Add the current restaurant to favorites using the same API as SearchActivity
     */
    private void addRestaurantToFavorites() {
        if (currentRestaurant == null) {
            Toast.makeText(this, " Restaurant data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "️ Adding restaurant to favorites: " + currentRestaurant.getName());
        Toast.makeText(this, "Adding " + currentRestaurant.getName() + " to favorites...", Toast.LENGTH_SHORT).show();

        // Create favorite request
        FavoriteRequest favoriteRequest = new FavoriteRequest(currentRestaurant);

        // Call API to add to favorites
        apiClient.addToFavorites(favoriteRequest, new ApiClient.FavoritesCallback() {
            @Override
            public void onSuccess(FavoriteResponse response) {
                mainHandler.post(() -> handleFavoriteSuccess(response));
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> handleFavoriteError(error));
            }
        });
    }

    /**
     * Handle successful favorite addition
     */
    private void handleFavoriteSuccess(FavoriteResponse response) {
        Log.d(TAG, "Restaurant added to favorites successfully: " + response.toString());
        Toast.makeText(this, "" + currentRestaurant.getName() + " added to favorites!", Toast.LENGTH_SHORT).show();

        // Optional: Update UI to show restaurant is favorited
        // You could change the button text or color to indicate it's been favorited
    }

    /**
     * Handle favorite addition error
     */
    private void handleFavoriteError(String error) {
        Log.e(TAG, " Failed to add restaurant to favorites: " + error);
        Toast.makeText(this, " Failed to add " + currentRestaurant.getName() + " to favorites: " + error, Toast.LENGTH_LONG).show();
    }

    /**
     * Open directions to the restaurant using Google Maps
     */
    private void openDirectionsToRestaurant() {
        if (currentRestaurant == null || currentRestaurant.getAddress() == null) {
            Toast.makeText(this, "Restaurant address not available", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Restaurant or address is null");
            return;
        }

        String address = currentRestaurant.getAddress().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Restaurant address is empty", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Restaurant address is empty");
            return;
        }

        Log.d(TAG, "Opening integrated map for: '" + address + "'");

        // Launch the integrated MapActivity
        Intent mapIntent = new Intent(this, MapActivity.class);
        mapIntent.putExtra("restaurant_name", currentRestaurant.getName());
        mapIntent.putExtra("restaurant_address", address);

        // Pass coordinates if available
        if (currentRestaurant.hasLocation()) {
            mapIntent.putExtra("restaurant_latitude", currentRestaurant.getLatitude());
            mapIntent.putExtra("restaurant_longitude", currentRestaurant.getLongitude());
            Log.d(TAG, "Passing coordinates to MapActivity: " + currentRestaurant.getLocationString());
        }

        startActivity(mapIntent);
    }


}

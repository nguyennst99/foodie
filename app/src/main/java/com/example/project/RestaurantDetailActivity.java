package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class RestaurantDetailActivity extends AppCompatActivity {

    private ImageView restaurantHeroImage;
    private TextView restaurantName, restaurantDescription, restaurantInfo, restaurantHours;
    private MaterialCardView shareButton;
    private MaterialButton directionsButton, favoriteButton;

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
        int imageResource = intent.getIntExtra("restaurant_image", R.drawable.restaurant_golden_spoon);

        // Set default values if no data is passed
        if (name == null) name = "The Golden Spoon";
        if (description == null) description = "The Golden Spoon offers a delightful Italian dining experience with a menu featuring classic pasta dishes, wood-fired pizzas, and fresh seafood. Enjoy a cozy atmosphere and attentive service.";
        if (info == null) info = "123 Main Street, Anytown | Italian | $$";
        if (hours == null) hours = "Open today: 11:00 AM - 10:00 PM";

        // Update UI with restaurant data
        restaurantName.setText(name);
        restaurantDescription.setText(description);
        restaurantInfo.setText(info);
        restaurantHours.setText(hours);
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
                Toast.makeText(this, "Opening directions...", Toast.LENGTH_SHORT).show();
                // TODO: Implement directions functionality (Google Maps integration)
            });
        }
        
        // Favorite button click
        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(v -> {
                Toast.makeText(this, "Added to favorites!", Toast.LENGTH_SHORT).show();
                // TODO: Implement favorite functionality (save to database/preferences)
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
}

package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FavoritesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.favorites_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        setupClickListeners();
        setupRestaurantClickListeners();
    }
    
    private void setupClickListeners() {
        // Back arrow click - return to MainActivity
        ImageView backArrow = findViewById(R.id.back_arrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> {
                Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            });
        }
        
        // Home tab click - navigate to MainActivity
        LinearLayout homeTab = findViewById(R.id.home_tab);
        if (homeTab != null) {
            homeTab.setOnClickListener(v -> {
                Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            });
        }
        
        // Profile tab click - navigate to ProfileActivity
        LinearLayout profileTab = findViewById(R.id.profile_tab);
        if (profileTab != null) {
            profileTab.setOnClickListener(v -> {
                Intent intent = new Intent(FavoritesActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            });
        }
    }

    private void setupRestaurantClickListeners() {
        // Bella Trattoria click
        LinearLayout bellaTrattoria = findViewById(R.id.bella_trattoria_card);
        if (bellaTrattoria != null) {
            bellaTrattoria.setOnClickListener(v -> {
                Intent intent = new Intent(FavoritesActivity.this, RestaurantDetailActivity.class);
                intent.putExtra("restaurant_name", "Bella Trattoria");
                intent.putExtra("restaurant_description", "Bella Trattoria offers authentic Italian cuisine with a modern twist. Experience traditional recipes passed down through generations, featuring fresh pasta, wood-fired pizzas, and seasonal ingredients.");
                intent.putExtra("restaurant_info", "123 Main Street, Anytown | Italian | $$");
                intent.putExtra("restaurant_hours", "Open today: 11:00 AM - 10:00 PM");
                intent.putExtra("restaurant_image", R.drawable.restaurant_bella_trattoria);
                startActivity(intent);
            });
        }

        // TODO: Add IDs and click listeners for other restaurant cards:
        // - Sakura Sushi
        // - El Taco Loco
        // - Le Petit Bistro
        // - Spice Route
        // This would be better implemented with RecyclerView for dynamic content
    }
}

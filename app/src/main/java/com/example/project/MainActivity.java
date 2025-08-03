package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Menu icon click
        ImageView menuIcon = findViewById(R.id.menu_icon);
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v ->
                Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
            );
        }

        // Search bar click - navigate to SearchActivity
        MaterialCardView searchCard = findViewById(R.id.search_card);
        if (searchCard != null) {
            searchCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            });
        }

        // Bottom navigation - Favorites tab click
        setupBottomNavigation();

        // Restaurant card click listeners
        setupRestaurantClickListeners();
    }

    private void setupBottomNavigation() {
        // Favorites tab click - navigate to FavoritesActivity
        LinearLayout favoritesTab = findViewById(R.id.favorites_tab);
        if (favoritesTab != null) {
            favoritesTab.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
                // Note: We don't call finish() here so user can return with back button
            });
        }

        // Profile tab click - navigate to ProfileActivity
        LinearLayout profileTab = findViewById(R.id.profile_tab);
        if (profileTab != null) {
            profileTab.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                // Note: We don't call finish() here so user can return with back button
            });
        }
    }

    private void setupRestaurantClickListeners() {
        // Italian Restaurant click
        View italianRestaurant = findViewById(R.id.italian_restaurant);
        if (italianRestaurant != null) {
            italianRestaurant.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RestaurantDetailActivity.class);
                intent.putExtra("restaurant_name", "The Italian Place");
                intent.putExtra("restaurant_description", "The Italian Place offers authentic Italian cuisine with a modern twist. Enjoy handmade pasta, wood-fired pizzas, and fresh ingredients in a warm, welcoming atmosphere.");
                intent.putExtra("restaurant_info", "123 Main Street, Anytown | Italian | $$");
                intent.putExtra("restaurant_hours", "Open today: 11:00 AM - 10:00 PM");
                intent.putExtra("restaurant_image", R.drawable.restaurant_italian);
                startActivity(intent);
            });
        }

        // Sushi Restaurant click
        LinearLayout sushiRestaurant = findViewById(R.id.sushi_restaurant);
        if (sushiRestaurant != null) {
            sushiRestaurant.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RestaurantDetailActivity.class);
                intent.putExtra("restaurant_name", "Sushi Central");
                intent.putExtra("restaurant_description", "Sushi Central brings you the finest fresh sushi and sashimi in a sleek, modern setting. Experience traditional Japanese flavors with contemporary presentation.");
                intent.putExtra("restaurant_info", "456 Ocean Ave, Anytown | Japanese | $$$");
                intent.putExtra("restaurant_hours", "Open today: 5:00 PM - 11:00 PM");
                intent.putExtra("restaurant_image", R.drawable.restaurant_sushi);
                startActivity(intent);
            });
        }

        // Burger Restaurant click
        LinearLayout burgerRestaurant = findViewById(R.id.burger_restaurant);
        if (burgerRestaurant != null) {
            burgerRestaurant.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RestaurantDetailActivity.class);
                intent.putExtra("restaurant_name", "Burger Haven");
                intent.putExtra("restaurant_description", "Burger Haven serves gourmet burgers made with premium ingredients and house-made sauces. From classic beef to creative vegetarian options, there's something for everyone.");
                intent.putExtra("restaurant_info", "789 Burger St, Anytown | American | $$");
                intent.putExtra("restaurant_hours", "Open today: 11:00 AM - 12:00 AM");
                intent.putExtra("restaurant_image", R.drawable.restaurant_burger);
                startActivity(intent);
            });
        }

        // Taco Restaurant click
        View tacoRestaurant = findViewById(R.id.taco_restaurant);
        if (tacoRestaurant != null) {
            tacoRestaurant.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RestaurantDetailActivity.class);
                intent.putExtra("restaurant_name", "Taco Fiesta");
                intent.putExtra("restaurant_description", "Taco Fiesta brings vibrant Mexican flavors to life with authentic street tacos, fresh guacamole, and handcrafted cocktails. Experience the true taste of Mexico in a lively atmosphere.");
                intent.putExtra("restaurant_info", "321 Taco Blvd, Anytown | Mexican | $$");
                intent.putExtra("restaurant_hours", "Open today: 10:00 AM - 11:00 PM");
                intent.putExtra("restaurant_image", R.drawable.restaurant_taco);
                startActivity(intent);
            });
        }

        // Noodle Restaurant click
        MaterialCardView noodleRestaurant = findViewById(R.id.noodle_restaurant);
        if (noodleRestaurant != null) {
            noodleRestaurant.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RestaurantDetailActivity.class);
                intent.putExtra("restaurant_name", "Noodle Nirvana");
                intent.putExtra("restaurant_description", "Noodle Nirvana offers an exquisite Asian fusion experience with handmade noodles, aromatic broths, and fresh ingredients. Discover a perfect blend of traditional and modern Asian flavors.");
                intent.putExtra("restaurant_info", "654 Noodle Way, Anytown | Asian Fusion | $$");
                intent.putExtra("restaurant_hours", "Open today: 12:00 PM - 10:00 PM");
                intent.putExtra("restaurant_image", R.drawable.restaurant_noodle);
                startActivity(intent);
            });
        }
    }
}
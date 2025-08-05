package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapters.FavoritesAdapter;
import com.example.project.models.FavoritesListResponse;
import com.example.project.models.Restaurant;
import com.example.project.network.ApiClient;

public class FavoritesActivity extends AppCompatActivity {
    private static final String TAG = "FavoritesActivity";

    private ApiClient apiClient;
    private Handler mainHandler;
    private RecyclerView favoritesRecyclerView;
    private FavoritesAdapter favoritesAdapter;
    private TextView emptyStateText;

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

        // Initialize API client and handler
        apiClient = ApiClient.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());

        // Debug: Show toast to confirm new code is running
        Toast.makeText(this, "NEW FavoritesActivity loaded!", Toast.LENGTH_SHORT).show();

        setupViews();
        setupClickListeners();
        loadFavorites();
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

    /**
     * Setup views and RecyclerView
     */
    private void setupViews() {
        favoritesRecyclerView = findViewById(R.id.favorites_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);

        Log.d(TAG, "Setting up views - RecyclerView: " + (favoritesRecyclerView != null ? "found" : "null"));
        Log.d(TAG, "Setting up views - EmptyStateText: " + (emptyStateText != null ? "found" : "null"));

        if (favoritesRecyclerView == null) {
            Log.e(TAG, " RecyclerView not found! Check layout ID.");
            return;
        }

        // Setup RecyclerView
        favoritesAdapter = new FavoritesAdapter(restaurant -> {
            // Handle restaurant click - navigate to detail activity
            Intent intent = new Intent(FavoritesActivity.this, RestaurantDetailActivity.class);
            intent.putExtra("restaurant_name", restaurant.getName());
            intent.putExtra("restaurant_description", restaurant.getDescription());
            intent.putExtra("restaurant_info", restaurant.getAddress() + " | " + restaurant.getCuisineType());
            intent.putExtra("restaurant_hours", restaurant.getTodayHours());
            intent.putExtra("restaurant_rating", String.format("%.1f", restaurant.getRating()));
            intent.putExtra("restaurant_phone", "Phone: " + restaurant.getPhone());
            intent.putExtra("restaurant_image", R.drawable.restaurant_bella_trattoria); // Default image
            startActivity(intent);
        });

        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setAdapter(favoritesAdapter);
        Log.d(TAG, "RecyclerView setup complete");
    }

    /**
     * Load user's favorites from the API
     */
    private void loadFavorites() {
        Log.d(TAG, "️ Loading user's favorites");

        // Check if user is authenticated
        if (!apiClient.isAuthenticated()) {
            showEmptyState("Please log in to view your favorites");
            return;
        }

        apiClient.getFavorites(new ApiClient.FavoritesListCallback() {
            @Override
            public void onSuccess(FavoritesListResponse response) {
                mainHandler.post(() -> handleFavoritesSuccess(response));
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> handleFavoritesError(error));
            }
        });
    }

    /**
     * Handle successful favorites response
     */
    private void handleFavoritesSuccess(FavoritesListResponse response) {
        Log.d(TAG, "Favorites loaded successfully: " + response.getCount() + " favorites");
        Log.d(TAG, "📊 Response details: " + response.toString());

        if (response.getFavorites() != null && !response.getFavorites().isEmpty()) {
            Log.d(TAG, "📋 Found " + response.getFavorites().size() + " favorites, showing list");

            // Log first favorite for debugging
            if (!response.getFavorites().isEmpty()) {
                var firstFavorite = response.getFavorites().get(0);
                Log.d(TAG, "🍽️ First favorite: " + (firstFavorite.getRestaurantData() != null ?
                    firstFavorite.getRestaurantData().getName() : "null restaurant data"));
            }

            showFavoritesList();
            favoritesAdapter.updateFavorites(response.getFavorites());
            Log.d(TAG, "Adapter updated with favorites data");
        } else {
            Log.d(TAG, "📭 No favorites found, showing empty state");
            showEmptyState("No favorites yet. Start exploring restaurants!");
        }
    }

    /**
     * Handle favorites error
     */
    private void handleFavoritesError(String error) {
        Log.e(TAG, " Failed to load favorites: " + error);
        Toast.makeText(this, "Unable to load favorites", Toast.LENGTH_SHORT).show();
        showEmptyState("Unable to load favorites. Please try again.");
    }

    /**
     * Show the favorites list and hide empty state
     */
    private void showFavoritesList() {
        Log.d(TAG, "👁️ Showing favorites list");
        if (favoritesRecyclerView != null) {
            favoritesRecyclerView.setVisibility(View.VISIBLE);
            Log.d(TAG, "RecyclerView visibility set to VISIBLE");
        } else {
            Log.e(TAG, " RecyclerView is null, cannot show list");
        }
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.GONE);
            Log.d(TAG, "Empty state hidden");
        } else {
            Log.e(TAG, " EmptyStateText is null");
        }
    }

    /**
     * Show empty state and hide favorites list
     */
    private void showEmptyState(String message) {
        if (emptyStateText != null) {
            emptyStateText.setText(message);
            emptyStateText.setVisibility(View.VISIBLE);
        }
        if (favoritesRecyclerView != null) {
            favoritesRecyclerView.setVisibility(View.GONE);
        }
    }
}

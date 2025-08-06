package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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

import com.example.project.adapters.RestaurantSearchAdapter;
import com.example.project.models.FavoriteRequest;
import com.example.project.models.FavoriteResponse;
import com.example.project.models.Restaurant;
import com.example.project.models.SearchResponse;
import com.example.project.network.ApiClient;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    private EditText searchEditText;
    private ImageView clearSearch;
    private RecyclerView searchResultsRecycler;
    private TextView searchResultsHeader;
    private TextView noResultsMessage;
    private TextView defaultMessage;
    private LinearLayout loadingContainer;

    private ApiClient apiClient;
    private Handler mainHandler;
    private RestaurantSearchAdapter searchAdapter;
    
    // Search management
    private Handler searchHandler;
    private Runnable searchRunnable;
    private boolean isSearchInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();
        setupSearchFunctionality();
        setupRestaurantClickListeners();
        
        // Initialize search button state
        updateSearchButtonState();

        // Initialize API client and test connectivity
        apiClient = ApiClient.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());
        searchHandler = new Handler(Looper.getMainLooper());
        testBackendConnectivity();
    }

    private void initializeViews() {
        searchEditText = findViewById(R.id.search_edit_text);
        clearSearch = findViewById(R.id.clear_search);
        ImageView searchButton = findViewById(R.id.search_button);

        // Initialize search results UI components
        searchResultsRecycler = findViewById(R.id.search_results_recycler);
        searchResultsHeader = findViewById(R.id.search_results_header);
        noResultsMessage = findViewById(R.id.no_results_message);
        defaultMessage = findViewById(R.id.default_message);
        loadingContainer = findViewById(R.id.loading_container);

        // Setup RecyclerView
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        searchAdapter = new RestaurantSearchAdapter(
                // Restaurant click listener
                restaurant -> {
                    // Handle restaurant click - navigate to detail activity
                    Intent intent = new Intent(SearchActivity.this, RestaurantDetailActivity.class);
                    intent.putExtra("restaurant_name", restaurant.getName());
                    intent.putExtra("restaurant_rating", String.format("%.1f", restaurant.getRating()));
                    intent.putExtra("restaurant_reviews", "Reviews available");
                    intent.putExtra("restaurant_info", restaurant.getAddress() + " | " + restaurant.getCuisineType());
                    intent.putExtra("restaurant_hours", restaurant.getTodayHours());
                    intent.putExtra("restaurant_description", restaurant.getDescription());

                    // Pass coordinates if available
                    if (restaurant.hasLocation()) {
                        intent.putExtra("restaurant_latitude", restaurant.getLatitude());
                        intent.putExtra("restaurant_longitude", restaurant.getLongitude());
                        Log.d(TAG, "Passing coordinates for " + restaurant.getName() + ": " + restaurant.getLocationString());
                    }
                    intent.putExtra("restaurant_phone", "Phone: " + restaurant.getPhone());
                    intent.putExtra("restaurant_image", R.drawable.search_bella_trattoria); // Default image
                    startActivity(intent);
                },
                // Favorite click listener
                restaurant -> {
                    addRestaurantToFavorites(restaurant);
                }
        );

        searchResultsRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecycler.setAdapter(searchAdapter);
    }

    private void setupClickListeners() {
        // Back arrow click - return to MainActivity
        ImageView backArrow = findViewById(R.id.back_arrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> {
                finish(); // Close this activity and return to previous
            });
        }

        // Search button click
        ImageView searchButton = findViewById(R.id.search_button);
        if (searchButton != null) {
            searchButton.setOnClickListener(v -> {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    // Check if search is already in progress
                    if (isSearchInProgress) {
                        Toast.makeText(this, "Search in progress, please wait...", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Cancel any pending search
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                    performSearch(query);
                } else {
                    Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Clear search button
        if (clearSearch != null) {
            clearSearch.setOnClickListener(v -> {
                // Cancel any pending search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchEditText.setText("");
                searchEditText.requestFocus();
                showDefaultState();
            });
        }
    }

    private void setupSearchFunctionality() {
        if (searchEditText != null) {
            // Handle search text changes
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Show/hide clear button based on text content
                    if (clearSearch != null) {
                        clearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Cancel any pending search when text changes
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                    
                    // If search field is empty, show default state immediately
                    if (s.toString().trim().isEmpty()) {
                        showDefaultState();
                        return;
                    }
                    
                    // Don't perform automatic search - wait for user to press Enter or search button
                    // This prevents excessive API calls while user is typing
                }
            });

            // Handle search action from keyboard
            searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        String query = v.getText().toString().trim();
                        
                        // Check if search is already in progress
                        if (isSearchInProgress) {
                            Toast.makeText(SearchActivity.this, "Search in progress, please wait...", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        
                        if (!query.isEmpty()) {
                            // Cancel any pending delayed search
                            if (searchRunnable != null) {
                                searchHandler.removeCallbacks(searchRunnable);
                            }
                            // Perform immediate search
                            performSearch(query);
                        } else {
                            Toast.makeText(SearchActivity.this, "Please enter a search term", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                    return false;
                }
            });

            // Set initial clear button visibility
            clearSearch.setVisibility(searchEditText.getText().length() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            Log.d(TAG, "Empty search query, skipping search");
            hideLoadingState();
            return;
        }

        // Check if search is already in progress
        if (isSearchInProgress) {
            Log.d(TAG, "Search already in progress, ignoring new request");
            Toast.makeText(this, "Search in progress, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Performing search for: " + query);
        Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();

        // Set search state to in progress
        isSearchInProgress = true;
        updateSearchButtonState();

        // Show loading state
        showLoadingState();

        // Call the backend API
        apiClient.searchRestaurants(query.trim(), "toronto", new ApiClient.SearchCallback() {
            @Override
            public void onSuccess(SearchResponse response) {
                mainHandler.post(() -> handleSearchSuccess(response));
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> handleSearchError(error));
            }
        });
    }

    private void setupRestaurantClickListeners() {
        // TODO: Add IDs to search result cards in activity_search.xml and implement click listeners
        // Similar to MainActivity implementation

        // For now, search results are hardcoded LinearLayouts
        // This would be better implemented with RecyclerView for dynamic search results

        // Example implementation when IDs are added:
        /*
        LinearLayout bellaTrattoriaResult = findViewById(R.id.bella_trattoria_result);
        if (bellaTrattoriaResult != null) {
            bellaTrattoriaResult.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this, RestaurantDetailActivity.class);
                intent.putExtra("restaurant_name", "Bella Trattoria");
                intent.putExtra("restaurant_rating", "4.5");
                intent.putExtra("restaurant_reviews", "1200+ reviews");
                intent.putExtra("restaurant_info", "123 Main Street, Anytown | Italian | $$");
                intent.putExtra("restaurant_hours", "Open today: 11:00 AM - 10:00 PM");
                intent.putExtra("restaurant_image", R.drawable.search_bella_trattoria);
                startActivity(intent);
            });
        }
        */
    }

    /**
     * Test connectivity to the backend server
     */
    private void testBackendConnectivity() {
        Log.d(TAG, "🔗 Testing backend connectivity...");
        Toast.makeText(this, "🔗 Testing server connection...", Toast.LENGTH_SHORT).show();

        // First try the default configuration
        apiClient.testConnectivity(new ApiClient.SearchCallback() {
            @Override
            public void onSuccess(SearchResponse response) {
                mainHandler.post(() -> {
                    Log.d(TAG, "Backend connectivity test successful");
                    Toast.makeText(SearchActivity.this, "Connected to backend server", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    Log.e(TAG, " Backend connectivity test failed: " + error);
                    Toast.makeText(SearchActivity.this, " Failed to connect to backend server", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Handle successful search response
     */
    private void handleSearchSuccess(SearchResponse response) {
        Log.d(TAG, "Search successful: " + response.toString());

        // Reset search state
        isSearchInProgress = false;
        updateSearchButtonState();

        // Hide loading state
        hideLoadingState();

        if (response.getRestaurants() != null && !response.getRestaurants().isEmpty()) {
            // Log detailed information about each restaurant for testing
            for (int i = 0; i < response.getRestaurants().size(); i++) {
                Restaurant restaurant = response.getRestaurants().get(i);
                Log.d(TAG, "Restaurant " + (i + 1) + ": " + restaurant.toString());

                // Validate restaurant data according to testing guide
                if (restaurant.isValid()) {
                    Log.d(TAG, "Restaurant " + restaurant.getName() + " passed validation");
                } else {
                    Log.w(TAG, " Restaurant " + restaurant.getName() + " failed validation");
                }
            }

            // Update UI with search results
            showSearchResults(response.getRestaurants(), response.getCount());
            Toast.makeText(this, "Found " + response.getCount() + " restaurants", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "No restaurants found in response");
            showNoResults();
            Toast.makeText(this, "No restaurants found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show search results in the UI
     */
    private void showSearchResults(java.util.List<Restaurant> restaurants, int count) {
        // Hide other views
        defaultMessage.setVisibility(View.GONE);
        noResultsMessage.setVisibility(View.GONE);
        loadingContainer.setVisibility(View.GONE);

        // Show search results
        searchResultsHeader.setText("Found " + count + " restaurants");
        searchResultsHeader.setVisibility(View.VISIBLE);
        searchResultsRecycler.setVisibility(View.VISIBLE);

        // Update adapter with new data
        searchAdapter.updateRestaurants(restaurants);
    }

    /**
     * Show no results message
     */
    private void showNoResults() {
        // Hide other views
        defaultMessage.setVisibility(View.GONE);
        searchResultsHeader.setVisibility(View.GONE);
        searchResultsRecycler.setVisibility(View.GONE);
        loadingContainer.setVisibility(View.GONE);

        // Show no results message
        noResultsMessage.setVisibility(View.VISIBLE);
        searchAdapter.clearRestaurants();
    }

    /**
     * Show default state (no search performed)
     */
    private void showDefaultState() {
        // Hide search results
        searchResultsHeader.setVisibility(View.GONE);
        searchResultsRecycler.setVisibility(View.GONE);
        noResultsMessage.setVisibility(View.GONE);

        // Show default message
        defaultMessage.setVisibility(View.VISIBLE);
        searchAdapter.clearRestaurants();
    }

    /**
     * Handle search error
     */
    private void handleSearchError(String error) {
        Log.e(TAG, " Search failed: " + error);

        // Reset search state
        isSearchInProgress = false;
        updateSearchButtonState();

        // Hide loading state
        hideLoadingState();

        showNoResults();
        Toast.makeText(this, "Search failed: " + error, Toast.LENGTH_LONG).show();
    }

    /**
     * Update search button visual state based on search progress
     */
    private void updateSearchButtonState() {
        ImageView searchButton = findViewById(R.id.search_button);
        if (searchButton != null) {
            if (isSearchInProgress) {
                // Disable visual state - make it look inactive
                searchButton.setAlpha(0.5f);
                searchButton.setEnabled(false);
            } else {
                // Enable visual state - restore normal appearance
                searchButton.setAlpha(1.0f);
                searchButton.setEnabled(true);
            }
        }
    }

    /**
     * Add a restaurant to favorites
     */
    private void addRestaurantToFavorites(Restaurant restaurant) {
        Log.d(TAG, "️ Adding restaurant to favorites: " + restaurant.getName());
        Toast.makeText(this, "Adding " + restaurant.getName() + " to favorites...", Toast.LENGTH_SHORT).show();

        // Create favorite request
        FavoriteRequest favoriteRequest = new FavoriteRequest(restaurant);

        // Call API to add to favorites
        apiClient.addToFavorites(favoriteRequest, new ApiClient.FavoritesCallback() {
            @Override
            public void onSuccess(FavoriteResponse response) {
                mainHandler.post(() -> handleFavoriteSuccess(response, restaurant));
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> handleFavoriteError(error, restaurant));
            }
        });
    }

    /**
     * Handle successful favorite addition
     */
    private void handleFavoriteSuccess(FavoriteResponse response, Restaurant restaurant) {
        Log.d(TAG, "Restaurant added to favorites successfully: " + response.toString());
        Toast.makeText(this, "" + restaurant.getName() + " added to favorites!", Toast.LENGTH_SHORT).show();

        // Optional: Update UI to show restaurant is favorited
        // You could change the heart icon color or show a different state
    }

    /**
     * Handle favorite addition error
     */
    private void handleFavoriteError(String error, Restaurant restaurant) {
        Log.e(TAG, " Failed to add restaurant to favorites: " + error);
        Toast.makeText(this, " Failed to add " + restaurant.getName() + " to favorites: " + error, Toast.LENGTH_LONG).show();
    }

    /**
     * Show loading state during search
     */
    private void showLoadingState() {
        // Hide other views
        defaultMessage.setVisibility(View.GONE);
        searchResultsHeader.setVisibility(View.GONE);
        searchResultsRecycler.setVisibility(View.GONE);
        noResultsMessage.setVisibility(View.GONE);

        // Show loading container
        loadingContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Hide loading state
     */
    private void hideLoadingState() {
        loadingContainer.setVisibility(View.GONE);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending search to prevent memory leaks
        if (searchRunnable != null && searchHandler != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        // Reset search state
        isSearchInProgress = false;
    }
}

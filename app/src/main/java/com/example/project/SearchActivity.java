package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private ImageView clearSearch;
    private MaterialCardView priceFilter, ratingFilter, distanceFilter;
    private FloatingActionButton fabAction;

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
    }
    
    private void initializeViews() {
        searchEditText = findViewById(R.id.search_edit_text);
        clearSearch = findViewById(R.id.clear_search);
        priceFilter = findViewById(R.id.price_filter);
        ratingFilter = findViewById(R.id.rating_filter);
        distanceFilter = findViewById(R.id.distance_filter);
        fabAction = findViewById(R.id.fab_action);
    }
    
    private void setupClickListeners() {
        // Back arrow click - return to MainActivity
        ImageView backArrow = findViewById(R.id.back_arrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> {
                finish(); // Close this activity and return to previous
            });
        }
        
        // Clear search button
        if (clearSearch != null) {
            clearSearch.setOnClickListener(v -> {
                searchEditText.setText("");
                searchEditText.requestFocus();
            });
        }
        
        // Filter buttons (placeholder functionality)
        if (priceFilter != null) {
            priceFilter.setOnClickListener(v -> 
                Toast.makeText(this, "Price filter clicked", Toast.LENGTH_SHORT).show()
            );
        }
        
        if (ratingFilter != null) {
            ratingFilter.setOnClickListener(v -> 
                Toast.makeText(this, "Rating filter clicked", Toast.LENGTH_SHORT).show()
            );
        }
        
        if (distanceFilter != null) {
            distanceFilter.setOnClickListener(v -> 
                Toast.makeText(this, "Distance filter clicked", Toast.LENGTH_SHORT).show()
            );
        }
        
        // Floating Action Button
        if (fabAction != null) {
            fabAction.setOnClickListener(v -> 
                Toast.makeText(this, "Map view or additional actions", Toast.LENGTH_SHORT).show()
            );
        }
    }
    
    private void setupSearchFunctionality() {
        if (searchEditText != null) {
            // Handle search text changes
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Show/hide clear button based on text content
                    if (clearSearch != null) {
                        clearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Perform search when text changes
                    performSearch(s.toString());
                }
            });
            
            // Handle search action from keyboard
            searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        performSearch(v.getText().toString());
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
        // TODO: Implement actual search functionality
        // For now, just show a toast with the search query
        if (!query.trim().isEmpty()) {
            Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
            // Here you would typically:
            // 1. Call an API or search local database
            // 2. Filter the restaurant results
            // 3. Update the UI with new results
        }
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
}

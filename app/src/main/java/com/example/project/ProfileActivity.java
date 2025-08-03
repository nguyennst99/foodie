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

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        // Back arrow click - return to previous activity
        ImageView backArrow = findViewById(R.id.back_arrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> {
                finish(); // Close this activity and return to previous
            });
        }
        
        // Home tab click - navigate to MainActivity
        LinearLayout homeTab = findViewById(R.id.home_tab);
        if (homeTab != null) {
            homeTab.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            });
        }
        
        // Favorites tab click - navigate to FavoritesActivity
        LinearLayout favoritesTab = findViewById(R.id.favorites_tab);
        if (favoritesTab != null) {
            favoritesTab.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, FavoritesActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            });
        }
        
        // Edit Profile option click
        LinearLayout editProfileOption = findViewById(R.id.edit_profile_option);
        if (editProfileOption != null) {
            editProfileOption.setOnClickListener(v -> {
                Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to EditProfileActivity when implemented
            });
        }
        
        // Settings option click
        LinearLayout settingsOption = findViewById(R.id.settings_option);
        if (settingsOption != null) {
            settingsOption.setOnClickListener(v -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to SettingsActivity when implemented
            });
        }
    }
}

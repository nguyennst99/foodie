package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project.models.AuthResponse;
import com.example.project.network.ApiClient;
import com.example.project.utils.AuthConfig;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

/**
 * Login Activity with Google OAuth and Guest login options
 * Follows the Figma design with clean, minimal UI
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private MaterialButton googleLoginButton;
    private MaterialButton guestLoginButton;

    private ApiClient apiClient;
    private Handler mainHandler;

    // Google Sign-In components
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initializeViews();
        setupGoogleSignIn();
        setupClickListeners();

        // Initialize API client
        apiClient = ApiClient.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());

        // Log authentication configuration
        AuthConfig.logConfigurationStatus(this);
    }
    
    private void initializeViews() {
        googleLoginButton = findViewById(R.id.google_login_button);
        guestLoginButton = findViewById(R.id.guest_login_button);
    }

    private void setupGoogleSignIn() {
        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build Google Sign-In client
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Setup activity result launcher for Google Sign-In
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Google Sign-In result received");
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleGoogleSignInResult(task);
            }
        );
    }
    
    private void setupClickListeners() {
        // Google Login Button
        googleLoginButton.setOnClickListener(v -> {
            Log.d(TAG, "Google login button clicked");
            handleGoogleLogin();
        });
        
        // Guest Login Button
        guestLoginButton.setOnClickListener(v -> {
            Log.d(TAG, "Guest login button clicked");
            handleGuestLogin();
        });
    }
    
    /**
     * Handle Google OAuth login
     */
    private void handleGoogleLogin() {
        Log.d(TAG, "Starting Google OAuth login process");

        // Check if we're in development mode or if Google OAuth is properly configured
        if (AuthConfig.isDevelopmentMode()) {
            Log.d(TAG, "Development mode: Using test token");
            Toast.makeText(this, "Development mode: Testing with fake token", Toast.LENGTH_SHORT).show();
            handleDevelopmentGoogleLogin();
        } else if (!AuthConfig.isGoogleOAuthConfigured(this)) {
            Log.e(TAG, " Google OAuth not configured in production mode");
            Toast.makeText(this, " Google OAuth not configured. Please check configuration.", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "Production mode: Using real Google Sign-In");
            Toast.makeText(this, "Opening Google Sign-In...", Toast.LENGTH_SHORT).show();

            // Sign out any existing account first to ensure fresh login
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // Launch Google Sign-In intent
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        }
    }

    /**
     * Handle Google login in development mode (for testing without real Google OAuth setup)
     */
    private void handleDevelopmentGoogleLogin() {
        Log.d(TAG, "Development Google login - simulating successful Google Sign-In");

        // Create a realistic test ID token for development
        String testIdToken = createDevelopmentIdToken();

        Toast.makeText(this, "Testing with development token...", Toast.LENGTH_SHORT).show();

        apiClient.authenticateWithGoogle(testIdToken, new ApiClient.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                mainHandler.post(() -> handleAuthSuccess(response, "Google OAuth (Dev)"));
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> handleLoginError("Google OAuth (Dev)", error));
            }
        });
    }

    /**
     * Create a development ID token for testing
     */
    private String createDevelopmentIdToken() {
        // This creates a more realistic looking test token
        // Your backend should handle this appropriately in development mode
        return "dev_google_id_token_" + System.currentTimeMillis() + "_test_user@example.com";
    }

    /**
     * Handle Google Sign-In result
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Google Sign-In successful for: " + account.getEmail());

            // Get the ID token
            String idToken = account.getIdToken();
            if (idToken != null) {
                Log.d(TAG, "Got Google ID token from Supabase OAuth, sending to backend");
                Log.d(TAG, "User info: " + account.getDisplayName() + " (" + account.getEmail() + ")");
                Toast.makeText(this, "Verifying with backend...", Toast.LENGTH_SHORT).show();

                // Send ID token to your Node.js backend which will validate with Supabase
                apiClient.authenticateWithGoogle(idToken, new ApiClient.AuthCallback() {
                    @Override
                    public void onSuccess(AuthResponse response) {
                        mainHandler.post(() -> handleAuthSuccess(response, "Google OAuth"));
                    }

                    @Override
                    public void onError(String error) {
                        mainHandler.post(() -> handleLoginError("Google OAuth", error));
                    }
                });
            } else {
                Log.e(TAG, " Google Sign-In succeeded but no ID token received");
                handleLoginError("Google OAuth", "No ID token received from Google");
            }

        } catch (ApiException e) {
            Log.e(TAG, " Google Sign-In failed with code: " + e.getStatusCode(), e);
            String errorMessage = "Google Sign-In failed";

            switch (e.getStatusCode()) {
                case 12501: // User cancelled
                    errorMessage = "Sign-in was cancelled";
                    break;
                case 7: // Network error
                    errorMessage = "Network error during sign-in";
                    break;
                case 10: // Developer error (wrong configuration)
                    errorMessage = "Google Sign-In configuration error";
                    break;
                default:
                    errorMessage = "Google Sign-In failed (code: " + e.getStatusCode() + ")";
            }

            handleLoginError("Google OAuth", errorMessage);
        }
    }
    
    /**
     * Handle guest login
     */
    private void handleGuestLogin() {
        Log.d(TAG, "Starting guest login process");
        Toast.makeText(this, "Logging in as guest...", Toast.LENGTH_SHORT).show();

        // Get device ID for guest authentication
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        apiClient.authenticateAsGuest(deviceId, new ApiClient.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                mainHandler.post(() -> handleAuthSuccess(response, "Guest"));
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> handleLoginError("Guest", error));
            }
        });
    }
    
    /**
     * Handle successful authentication
     */
    private void handleAuthSuccess(AuthResponse response, String loginType) {
        Log.d(TAG, "" + loginType + " login successful: " + response.toString());
        Toast.makeText(this, "" + loginType + " login successful!", Toast.LENGTH_SHORT).show();

        // TODO: Store authentication tokens securely
        // SharedPreferences or encrypted storage would be used here
        // For now, just log the user info
        if (response.getUser() != null) {
            Log.d(TAG, "User info: " + response.getUser().toString());
        }

        // Navigate to MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Handle login error
     */
    private void handleLoginError(String loginType, String error) {
        Log.e(TAG, " " + loginType + " login failed: " + error);
        Toast.makeText(this, " " + loginType + " login failed: " + error, Toast.LENGTH_LONG).show();
    }
}

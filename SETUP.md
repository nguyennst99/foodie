# Foodie App Setup Guide

## Configuration Setup

This app uses configuration files to manage sensitive data and environment-specific settings. Follow these steps to set up your development environment.

### 1. Configuration Files

The following configuration files need to be set up (sample templates are provided):

#### Required Files:
- `config.properties` (API configuration)
- `app/src/main/res/values/google_oauth_config.xml` (Google OAuth)
- `app/src/main/res/xml/network_security_config.xml` (Network security)
- `app/google-services.json` (Firebase configuration)

### 2. Setup Steps

#### Step 1: API Configuration
```bash
# Copy the sample config file
cp config.properties.sample config.properties

# Edit config.properties and update:
# - api.base.url=http://YOUR_SERVER_IP:3000
# - allowed.domains=localhost,10.0.2.2,YOUR_SERVER_IP
```

#### Step 2: Google Services Setup
```bash
# Copy the sample Google services config
cp app/src/main/res/values/google_oauth_config.xml.sample app/src/main/res/values/google_oauth_config.xml

# Edit google_oauth_config.xml and replace:
# - YOUR_GOOGLE_OAUTH_CLIENT_ID_HERE with your actual Google OAuth Client ID
# - YOUR_GOOGLE_MAPS_API_KEY_HERE with your actual Google Maps API Key
```

#### Step 3: Network Security Configuration
```bash
# Copy the sample network security config
cp app/src/main/res/xml/network_security_config.xml.sample app/src/main/res/xml/network_security_config.xml

# Edit network_security_config.xml and replace:
# YOUR_SERVER_IP with your actual development server IP
```

#### Step 4: Firebase Configuration
- Download your `google-services.json` from Firebase Console
- Place it in the `app/` directory

### 3. Configuration Details

#### config.properties
```properties
# API Configuration
api.base.url=http://YOUR_SERVER_IP:3000
api.timeout.seconds=30

# Development Settings
development.mode=true
debug.logging=true

# Network Security
allowed.domains=localhost,10.0.2.2,YOUR_SERVER_IP

# Google Maps Configuration
google.maps.api.key=YOUR_GOOGLE_MAPS_API_KEY_HERE
```

#### Key Features:
- ✅ **Externalized Configuration**: No hardcoded URLs or sensitive data
- ✅ **Environment Management**: Easy switching between dev/prod environments
- ✅ **Security**: Sensitive files are excluded from version control
- ✅ **Developer Friendly**: Sample files provided for easy setup

### 4. Google Cloud Console Setup

#### Google Maps API Key:
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing project
3. Enable the following APIs:
   - Maps SDK for Android
   - Directions API
   - Geocoding API (optional)
4. Create credentials → API Key
5. Restrict the API key to your app's package name and SHA-1 fingerprint

#### Google OAuth Client ID:
1. In the same Google Cloud project
2. Go to APIs & Services → Credentials
3. Create OAuth 2.0 Client ID for Android
4. Use your app's package name and SHA-1 fingerprint

### 5. Important Notes

- **Never commit sensitive files**: The `.gitignore` is configured to exclude these files
- **Use sample files**: Always copy from `.sample` files and customize
- **Server IP**: Update all IP addresses to match your development server
- **Google OAuth**: Get your Client ID from Google Cloud Console
- **Google Maps**: Get your API key from Google Cloud Console (for integrated maps functionality)
- **Firebase**: Download `google-services.json` from your Firebase project

### 5. Troubleshooting

#### Common Issues:
1. **Connection Failed**: Check if your server IP is correct in config.properties
2. **CLEARTEXT traffic not permitted**: Ensure network_security_config.xml includes your server IP
3. **Google Sign-In Failed**: Verify your OAuth Client ID is correct
4. **Build Errors**: Make sure all configuration files are present

#### Debug Information:
The app logs configuration status on startup. Check LogCat for:
- Configuration loading status
- API base URL being used
- Authentication configuration details

### 6. File Structure
```
foodie/
├── config.properties          # Main configuration (DO NOT COMMIT)
├── config.properties.sample   # Template for developers
├── app/
│   ├── google-services.json   # Firebase config (DO NOT COMMIT)
│   └── src/main/
│       ├── assets/
│       │   └── config.properties  # Runtime configuration
│       └── res/
│           ├── values/
│           │   ├── google_oauth_config.xml         # OAuth config (DO NOT COMMIT)
│           │   └── google_oauth_config.xml.sample  # Template
│           └── xml/
│               ├── network_security_config.xml         # Network config (DO NOT COMMIT)
│               └── network_security_config.xml.sample  # Template
```

### 7. Building and Running

1. Complete all setup steps above
2. Build the project: `./gradlew build`
3. Run on device or emulator
4. Check LogCat for configuration status logs

---

For any issues, check the configuration logs in LogCat or refer to the sample files.
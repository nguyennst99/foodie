package com.example.project.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.R;
import com.example.project.models.FavoriteItem;
import com.example.project.models.Restaurant;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying user's favorite restaurants
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {
    private static final String TAG = "FavoritesAdapter";

    private List<FavoriteItem> favorites = new ArrayList<>();
    private OnFavoriteClickListener clickListener;
    
    // Array of random restaurant images to use
    private static final int[] RESTAURANT_IMAGES = {
        R.drawable.restaurant_bella_trattoria,
        R.drawable.restaurant_sakura_sushi,
        R.drawable.restaurant_el_taco_loco,
        R.drawable.restaurant_le_petit_bistro,
        R.drawable.restaurant_spice_route,
        R.drawable.restaurant_italian,
        R.drawable.restaurant_burger
    };
    
    public interface OnFavoriteClickListener {
        void onFavoriteClick(Restaurant restaurant);
    }
    
    public FavoritesAdapter(OnFavoriteClickListener clickListener) {
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_restaurant, parent, false);
        return new FavoriteViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Log.d(TAG, "🔗 Binding view holder at position: " + position);
        FavoriteItem favoriteItem = favorites.get(position);
        Restaurant restaurant = favoriteItem.getRestaurantData();

        if (restaurant != null) {
            Log.d(TAG, "🍽️ Binding restaurant: " + restaurant.getName());
            holder.bind(restaurant, position);
        } else {
            Log.e(TAG, " Restaurant data is null at position: " + position);
        }
    }
    
    @Override
    public int getItemCount() {
        int count = favorites.size();
        Log.d(TAG, "📊 getItemCount() returning: " + count);
        return count;
    }
    
    public void updateFavorites(List<FavoriteItem> newFavorites) {
        Log.d(TAG, "📊 Updating favorites: " + (newFavorites != null ? newFavorites.size() : 0) + " items");
        this.favorites.clear();
        if (newFavorites != null) {
            this.favorites.addAll(newFavorites);
            Log.d(TAG, "Added " + newFavorites.size() + " favorites to adapter");

            // Log first item for debugging
            if (!newFavorites.isEmpty()) {
                FavoriteItem first = newFavorites.get(0);
                Restaurant restaurant = first.getRestaurantData();
                Log.d(TAG, "🍽️ First item: " + (restaurant != null ? restaurant.getName() : "null restaurant"));
            }
        }
        notifyDataSetChanged();
        Log.d(TAG, "🔄 notifyDataSetChanged() called");
    }
    
    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView restaurantImage;
        private final TextView restaurantName;
        private final TextView restaurantDescription;
        private final TextView restaurantDetails;
        
        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantImage = itemView.findViewById(R.id.restaurant_image);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            restaurantDescription = itemView.findViewById(R.id.restaurant_description);
            restaurantDetails = itemView.findViewById(R.id.restaurant_details);
        }
        
        public void bind(Restaurant restaurant, int position) {
            Log.d(TAG, "🔗 bind() called for position " + position);
            Log.d(TAG, "🍽️ Restaurant name: " + restaurant.getName());
            Log.d(TAG, "📝 Restaurant description: " + restaurant.getDescription());
            Log.d(TAG, "🏷️ Restaurant cuisine: " + restaurant.getCuisineType());
            Log.d(TAG, "⭐ Restaurant rating: " + restaurant.getRating());

            // Check if views are found
            Log.d(TAG, "🔍 Views found - Name: " + (restaurantName != null) +
                      ", Description: " + (restaurantDescription != null) +
                      ", Details: " + (restaurantDetails != null));

            if (restaurantName != null) {
                restaurantName.setText(restaurant.getName());
                Log.d(TAG, "Set restaurant name: " + restaurant.getName());
            } else {
                Log.e(TAG, " restaurantName view is null");
            }

            if (restaurantDescription != null) {
                restaurantDescription.setText(restaurant.getDescription());
                Log.d(TAG, "Set restaurant description");
            } else {
                Log.e(TAG, " restaurantDescription view is null");
            }

            if (restaurantDetails != null) {
                String details = restaurant.getCuisineType() + " · " + String.format("%.1f", restaurant.getRating());
                restaurantDetails.setText(details);
                Log.d(TAG, "Set restaurant details: " + details);
            } else {
                Log.e(TAG, " restaurantDetails view is null");
            }

            // Set a random restaurant image
            int imageIndex = position % RESTAURANT_IMAGES.length;
            if (restaurantImage != null) {
                restaurantImage.setImageResource(RESTAURANT_IMAGES[imageIndex]);
                Log.d(TAG, "Set restaurant image");
            } else {
                Log.e(TAG, " restaurantImage view is null");
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onFavoriteClick(restaurant);
                }
            });

            Log.d(TAG, "bind() completed for position " + position);
        }
    }
}

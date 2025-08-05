package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.R;
import com.example.project.models.Restaurant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * RecyclerView adapter for displaying restaurant search results
 */
public class RestaurantSearchAdapter extends RecyclerView.Adapter<RestaurantSearchAdapter.RestaurantViewHolder> {
    
    private List<Restaurant> restaurants = new ArrayList<>();
    private OnRestaurantClickListener clickListener;
    private OnFavoriteClickListener favoriteClickListener;
    
    // Array of random restaurant images to use
    private static final int[] RESTAURANT_IMAGES = {
        R.drawable.search_bella_trattoria,
        R.drawable.search_pasta_paradise,
        R.drawable.search_roma_ristorante,
        R.drawable.search_tuscany_bistro,
        R.drawable.restaurant_italian,
        R.drawable.restaurant_burger
    };
    
    private final Random random = new Random();
    
    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Restaurant restaurant);
    }
    
    public RestaurantSearchAdapter(OnRestaurantClickListener clickListener, OnFavoriteClickListener favoriteClickListener) {
        this.clickListener = clickListener;
        this.favoriteClickListener = favoriteClickListener;
    }
    
    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant_search, parent, false);
        return new RestaurantViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurants.get(position);
        holder.bind(restaurant);
    }
    
    @Override
    public int getItemCount() {
        return restaurants.size();
    }
    
    public void updateRestaurants(List<Restaurant> newRestaurants) {
        this.restaurants.clear();
        if (newRestaurants != null) {
            this.restaurants.addAll(newRestaurants);
        }
        notifyDataSetChanged();
    }
    
    public void clearRestaurants() {
        this.restaurants.clear();
        notifyDataSetChanged();
    }
    
    class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private final ImageView restaurantImage;
        private final TextView restaurantName;
        private final TextView restaurantRating;
        private final TextView restaurantReviews;
        private final TextView restaurantCuisine;
        private final ImageView addToFavoritesButton;
        
        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantImage = itemView.findViewById(R.id.restaurant_image);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            restaurantRating = itemView.findViewById(R.id.restaurant_rating);
            restaurantReviews = itemView.findViewById(R.id.restaurant_reviews);
            restaurantCuisine = itemView.findViewById(R.id.restaurant_cuisine);
            addToFavoritesButton = itemView.findViewById(R.id.add_to_favorites_button);

            // Restaurant item click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    clickListener.onRestaurantClick(restaurants.get(getAdapterPosition()));
                }
            });

            // Favorites button click listener
            addToFavoritesButton.setOnClickListener(v -> {
                if (favoriteClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    favoriteClickListener.onFavoriteClick(restaurants.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(Restaurant restaurant) {
            // Set restaurant name
            restaurantName.setText(restaurant.getName());
            
            // Set rating
            restaurantRating.setText(String.format("%.1f", restaurant.getRating()));
            
            // Generate random review count for display (since API doesn't provide this)
            int reviewCount = 100 + random.nextInt(1900); // Random between 100-2000
            restaurantReviews.setText(String.format("(%d+ reviews)", reviewCount));
            
            // Set cuisine type
            restaurantCuisine.setText(restaurant.getCuisineType());
            
            // Set random restaurant image
            int randomImageIndex = random.nextInt(RESTAURANT_IMAGES.length);
            restaurantImage.setImageResource(RESTAURANT_IMAGES[randomImageIndex]);
        }
    }
}

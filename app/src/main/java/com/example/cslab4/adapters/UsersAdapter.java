package com.example.cslab4.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cslab4.activities.UserActivity;
import com.example.cslab4.databinding.ItemContainerUserBinding;
import com.example.cslab4.listeners.UserListener;
import com.example.cslab4.models.User;

import java.util.List;

/**
 * Adapter class for displaying user list in a RecyclerView.
 * Handles the display of user profiles including names, emails, and profile images.
 * Supports click interactions through UserListener interface.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder>{

    private final List<User> users;
    private final UserListener userListener;

    /**
     * Constructs a new UsersAdapter.
     *
     * @param users List of users to display
     * @param userListener Listener for user selection events
     */
    public UsersAdapter(List<User> users, UserActivity userListener) {
        this.users = users;
        this.userListener = userListener;
    }


    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new UsersViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UsersViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding binding;

        /**
         * ViewHolder class for user items.
         * Handles the layout and data binding for individual user entries.
         */
        public UsersViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        /**
         * Binds user data to the view elements.
         * Sets up click listener for user selection.
         *
         * @param user User data to display
         */
        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            Bitmap userImage = getUserImage(user.image);
            if (userImage != null) {
                binding.imageProfile.setImageBitmap(userImage);
            }
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }
    /**
     * Converts a Base64 encoded image string to a Bitmap.
     * Handles null and invalid encoded strings safely.
     *
     * @param encodedImage Base64 encoded string of the image
     * @return Bitmap of the decoded image, or null if conversion fails
     */
    private Bitmap getUserImage(String encodedImage) {
        if (encodedImage == null) {
            return null;
        }
        try {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

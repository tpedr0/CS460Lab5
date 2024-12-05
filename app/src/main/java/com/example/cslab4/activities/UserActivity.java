package com.example.cslab4.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cslab4.R;
import com.example.cslab4.adapters.UsersAdapter;
import com.example.cslab4.databinding.ActivityUserBinding;
import com.example.cslab4.listeners.UserListener;
import com.example.cslab4.models.User;
import com.example.cslab4.utilities.Constants;
import com.example.cslab4.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * UserActivity displays a list of available users for chat interactions.
 * Implements UserListener interface to handle user selection events.
 * Manages the retrieval and display of user data from Firebase Firestore,
 * excluding the current user from the list.
 */
public class UserActivity extends AppCompatActivity implements UserListener {
    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;

    /**
     * Initializes the user activity and sets up the user interface.
     * Configures view binding, preference manager, and initiates user data loading.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        setListeners();
        getUsers();
    }

    /**
     * Sets up click listeners for UI elements.
     * Currently handles the back button navigation.
     */
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Retrieves user data from Firebase Firestore.
     * Filters out the current user and populates the RecyclerView with available users.
     * Handles loading states and error scenarios.
     */
    private void getUsers(){
        loading(true); // Show loading indicator
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Query all users from Firestore
        database.collection(Constants.KEY_COLLECTION_USERS).get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult()!=null){
                        List<User> users = new ArrayList<>();
                        // Process each user document
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            // Skip current user
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }

                            // Create user object from document data
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_FNAME) + " " + queryDocumentSnapshot.getString(Constants.KEY_LNAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        // Update UI based on results
                        if(users.size() > 0){
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }else{
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    /**
     * Displays an error message when no users are available or when query fails.
     * Updates the UI to show the error state.
     */
    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Controls the visibility of the progress bar during loading states.
     *
     * @param isLoading true to show loading indicator, false to hide it
     */
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Handles user selection events from the RecyclerView.
     * Initiates a chat session with the selected user by launching ChatActivity.
     *
     * @param user The selected User object containing user details
     */
    @Override
    public void onUserClicked(User user) {
        // to-do later
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}
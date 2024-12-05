package com.example.cslab4.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cslab4.R;
import com.example.cslab4.databinding.ActivityMainBinding;
import com.example.cslab4.utilities.Constants;
import com.example.cslab4.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

/**
 * MainActivity serves as the primary user interface after successful authentication.
 * Handles user profile display, messaging token management, and navigation to other activities.
 * Implements Firebase Cloud Messaging for push notifications and Firestore for data storage.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    /**
     * Initializes the activity, sets up view binding, and configures user interface components.
     * Loads user profile details, manages Firebase messaging token, and establishes click listeners.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();
    }

    /**
     * Configures click listeners for UI elements.
     * Sets up navigation to UserActivity for new chats and handles sign-out functionality.
     */
    private void setListeners(){
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UserActivity.class)));
    }

    /**
     * Retrieves and displays user profile information from preferences.
     * Handles the display of user's full name and profile image.
     * Includes null checks to prevent NullPointerExceptions when accessing stored preferences.
     */
    private void loadUserDetails() {
        // Retrieve user name components
        String firstName = preferenceManager.getString(Constants.KEY_FNAME);
        String lastName = preferenceManager.getString(Constants.KEY_LNAME);

        //  Construct and display full name with null checks
        if (firstName != null || lastName != null) {
            String fullName = (firstName != null ? firstName : "") + " " +
                    (lastName != null ? lastName : "");
            binding.textName.setText(fullName.trim());
        }

        // Load and display profile image if available
        String imageString = preferenceManager.getString(Constants.KEY_IMAGE);
        if (imageString != null) {
            byte[] bytes = Base64.decode(imageString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.imageProfile.setImageBitmap(bitmap);
        }
    }

    /**
     * Displays a toast message to the user.
     * Provides feedback for user actions and system events.
     *
     * @param message The text to be displayed in the toast notification
     */
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Retrieves the Firebase Cloud Messaging token for push notifications.
     * Initiates the token update process when successful.
     */
    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    /**
     * Updates the user's FCM token in Firestore.
     * Ensures proper delivery of push notifications by maintaining current token.
     *
     * @param token The new FCM token to be stored in Firestore
     */
    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString((Constants.KEY_USER_ID)));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> showToast("Token updated successfully"))
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    /**
     * Handles the sign-out process.
     * Removes the FCM token, clears local preferences, and returns to sign-in screen.
     * Shows appropriate feedback messages for success/failure states.
     */
    private void signOut(){
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        // Prepare updates for sign-out
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        // Execute sign-out process
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                }).addOnFailureListener(e -> showToast("Unable to sign out"));
    }
}
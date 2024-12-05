package com.example.cslab4.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cslab4.R;
import com.example.cslab4.databinding.ActivitySignInBinding;
import com.example.cslab4.utilities.Constants;
import com.example.cslab4.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/**
 * SignInActivity handles user authentication and login functionality.
 * Manages the sign-in process using Firebase authentication and provides
 * navigation to sign-up for new users. Implements input validation and
 * user feedback through the UI.
 */
public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;



    /**
     * Initializes the sign-in activity and sets up the user interface.
     * Configures view binding, preference manager, and establishes UI event listeners.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 /*       EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

  */
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    /**
     * Configures click listeners for interactive UI elements.
     * Handles navigation to sign-up screen and initiates sign-in process
     * after validation.
     */
    private void setListeners(){
        // Navigate to sign-up screen
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        // Attempt sign-in with validation
        binding.buttonSignIn.setOnClickListener(v -> {
            if(isValidateSignInDetails()){
                SignIn();
            }
        });
    }

    /**
     * Displays a toast notification to provide user feedback.
     *
     * @param message The text message to be displayed in the toast
     */
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Executes the sign-in process using Firebase authentication.
     * Queries Firestore to verify user credentials and stores user data in preferences
     * upon successful authentication. Handles error cases and provides appropriate feedback.
     * Upon successful sign-in, launches MainActivity and clears the activity stack.
     */
    private void SignIn() {
        loading(true); // Show loading indicator
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Query Firestore for matching credentials
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                        // Retrieve user document
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        // Store user data in preferences
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_FNAME,documentSnapshot.getString(Constants.KEY_FNAME));
                        preferenceManager.putString(Constants.KEY_LNAME,documentSnapshot.getString(Constants.KEY_LNAME));
                        preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        // Launch MainActivity
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        showToast("Successful Log In");
                        startActivity(intent);
                    } else{
                        loading(false);
                        showToast("Unable to Sign in");
                    }
                });
    }

    /**
     * Controls the visibility of UI elements during loading states.
     * Toggles between showing/hiding the progress bar and sign-in button
     * based on the loading state.
     *
     * @param isLoading true to show loading state, false to show normal state
     */
    private void loading (Boolean isLoading){
        if(isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Validates user input for the sign-in form.
     * Checks for:
     * - Empty email field
     * - Valid email format using Android Patterns
     * - Empty password field
     * Provides appropriate feedback messages for validation failures.
     *
     * @return true if all inputs are valid, false otherwise
     */
    private boolean isValidateSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Please enter your email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Please enter a valid email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Please enter your password");
            return false;
        } else {
            return true;
        }
    }
}
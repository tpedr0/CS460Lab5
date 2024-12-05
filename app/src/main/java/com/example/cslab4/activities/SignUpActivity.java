package com.example.cslab4.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cslab4.R;
import com.example.cslab4.databinding.ActivitySignInBinding;
import com.example.cslab4.databinding.ActivitySignUpBinding;
import com.example.cslab4.utilities.Constants;
import com.example.cslab4.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * SignUpActivity handles new user registration functionality.
 * Manages user profile creation including profile picture upload,
 * input validation, and account creation using Firebase Authentication.
 * Provides a complete registration flow with real-time feedback.
 */
public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodeImage;

    /**
     * Initializes the sign-up activity and sets up the user interface.
     * Configures view binding, preference manager, and UI event listeners.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    /**
     * Configures click listeners for all interactive UI elements.
     * Handles navigation, form submission, and image selection events.
     * Validates user input before initiating the sign-up process.
     */
    private void setListener() {
        // Navigate back to sign-in screen
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        // Handle sign-up button click with validation
        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValidateSignUpDetails()){
                SignUp();
            }
        });
        // Handle profile image selection
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    /**
     * Displays a toast notification for user feedback.
     *
     * @param message The text message to be displayed in the toast
     */
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles the sign-up process using Firebase Authentication.
     * Creates a new user account, stores user data in Firestore,
     * and manages the sign-up flow including:
     * - Showing loading state
     * - Creating user document in Firestore
     * - Storing user preferences
     * - Navigating to MainActivity upon success
     * - Handling potential errors
     */
    private void SignUp(){
        loading(true); // Show loading indicator

        // Initialize Firestore instance
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String,String> user = new HashMap<>();
        // Prepare user data for storage
        user.put(Constants.KEY_FNAME, binding.inputFirstName.getText().toString());
        user.put(Constants.KEY_LNAME, binding.inputLastName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE,encodeImage);

        // Add user to Firestore
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    // Store user data in preferences
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_FNAME,binding.inputFirstName.getText().toString());
                    preferenceManager.putString(Constants.KEY_LNAME,binding.inputLastName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodeImage);

                    // Navigate to MainActivity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }).addOnFailureListener(exception ->{
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    /**
     * Encodes a bitmap image to Base64 string format for storage.
     * Scales the image down to a preview size before encoding to reduce storage size.
     *
     * @param bitmap The bitmap image to be encoded
     * @return Base64 encoded string of the scaled image
     */
    private String encodeImage(Bitmap bitmap) {
        // Scale image to preview size
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);

        // Encode image to Base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    /**
     * ActivityResultLauncher for handling image selection from gallery.
     * Processes the selected image and updates the UI accordingly.
     */
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->{
                if (result.getResultCode() == RESULT_OK){
                    Uri imageUri = result.getData().getData();
                    try {
                        // Load and process selected image
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        // Update UI with selected image
                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                        encodeImage = encodeImage(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    /**
     * Validates all user input fields for the sign-up form.
     * Checks for:
     * - Profile picture selection
     * - First and last name presence
     * - Valid email format
     * - Password matching and presence
     * Provides appropriate feedback for validation failures.
     *
     * @return true if all inputs are valid, false otherwise
     */
    private Boolean isValidateSignUpDetails(){
        if (encodeImage == null){
            showToast("Please Pick Profile Picture");
            return false;
        }else if(binding.inputFirstName.getText().toString().trim().isEmpty()){
            showToast("Please Enter Your First Name");
            return false;
        } else if(binding.inputLastName.getText().toString().trim().isEmpty()){
            showToast("Please Enter Your Last Name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Please Enter Your Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Please Enter Vail Email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please Enter Your Password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Please Confirm Your Password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Passwords Do Not Match");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Controls the visibility of UI elements during loading states.
     * Toggles between showing/hiding the progress bar and sign-up button.
     *
     * @param isLoading true to show loading state, false to show normal state
     */
    private void loading (Boolean isLoading) {
        if(isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}
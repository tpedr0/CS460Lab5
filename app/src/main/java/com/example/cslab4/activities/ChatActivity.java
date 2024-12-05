package com.example.cslab4.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cslab4.R;
import com.example.cslab4.adapters.ChatAdapter;
import com.example.cslab4.databinding.ActivityChatBinding;
import com.example.cslab4.models.ChatMessage;
import com.example.cslab4.models.User;
import com.example.cslab4.utilities.Constants;
import com.example.cslab4.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Activity class that handles the chat interface between two users.
 * Manages real-time messaging using Firebase Firestore and displays chat history.
 */
public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    /**
     * Initializes the chat activity and sets up the user interface.
     * Loads receiver details, initializes listeners, and starts message listening.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();
        init();
        ListenMessage();
    }

    /**
     * Initializes chat-related components and Firebase connection.
     * Sets up the chat adapter with user details and configures the RecyclerView.
     */
    private void init() {
        // Initialize preference manager for user data storage
        preferenceManager = new PreferenceManager((getApplicationContext()));
        // Create list to store chat messages
        chatMessages = new ArrayList<>();
        // Initialize chat adapter with user details
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        // Set up RecyclerView with adapter
        binding.chatRecyclerView.setAdapter(chatAdapter);
        // Initialize Firebase instance
        database = FirebaseFirestore.getInstance();
    }

    /**
     * Sends a new message to the chat.
     * Creates a message object with sender, receiver, content, and timestamp,
     * then stores it in Firebase Firestore.
     */
    private void sendMessages() {
        // Create message data structure
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());

        // Store message in Firebase
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        // Clear input field after sending
        binding.inputMessage.setText(null);
    }

    /**
     * Sets up real-time listeners for incoming and outgoing messages.
     * Monitors Firebase Firestore for changes in the chat collection
     * filtered by sender and receiver IDs.
     */
    private void ListenMessage() {
        // Listen for messages sent by current user
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,
                        preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);

        // Listen for messages received by current user
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,
                        receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,
                        preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    /**
     * Event listener for handling real-time message updates from Firebase.
     * Processes new messages, updates the UI, and handles message sorting.
     */
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(
                            documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }

            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemChanged(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
    });

    /**
     * Converts a Base64 encoded image string to a Bitmap.
     *
     * @param encodedImage String containing the Base64 encoded image data
     * @return Bitmap object created from the decoded image data
     */
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Loads and displays the receiver's details in the chat interface.
     * Retrieves user information from the intent extras and updates the UI.
     */
    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    /**
     * Sets up click listeners for UI elements.
     * Handles back button press and message sending actions.
     */
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessages());
    }

    /**
     * Formats a Date object into a human-readable string.
     *
     * @param date Date object to be formatted
     * @return Formatted string in the pattern "MMM dd, yyyy - hh:mm a"
     */
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy - hh:mm a",
                Locale.getDefault()).format(date);
    }
}

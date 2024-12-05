package com.example.cslab4.utilities;

/**
 * Constants class that holds all the static final strings used throughout the application.
 * Contains keys for:
 * - Firebase collections
 * - User profile fields
 * - Authentication states
 * - Shared preferences
 * - Chat message fields
 */
public class Constants {
    // Firebase collection name for user data
    public static final String KEY_COLLECTION_USERS = "User";

    // Keys for user profile information
    public static final String KEY_NAME = "name";
    public static final String KEY_FNAME = "First name";
    public static final String KEY_LNAME = "Last name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_USER_ID = "userid";
    public static final String KEY_IMAGE = "image";

    // Authentication and session management keys
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";

    // Firebase Cloud Messaging token key
    public static final String KEY_FCM_TOKEN = "fcmToken";

    // Key for passing User object between activities
    public static final String KEY_USER = "user";

    // Firebase collection and field names for chat messages
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timeStamp";
}


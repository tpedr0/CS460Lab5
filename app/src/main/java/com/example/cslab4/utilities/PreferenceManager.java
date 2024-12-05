package com.example.cslab4.utilities;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class for managing shared preferences in the application.
 * Provides methods for storing and retrieving various types of data
 * using Android's SharedPreferences API.
 */
public class PreferenceManager {
    private final SharedPreferences sharedPreferences;


    /**
     * Constructor that initializes SharedPreferences with the application context.
     * Uses a predefined preference name from Constants class.
     *
     * @param context The application context used to get SharedPreferences instance
     */
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Stores a boolean value in SharedPreferences.
     * Uses apply() for asynchronous storage.
     *
     * @param key The key under which the value will be stored
     * @param value The boolean value to be stored
     */
    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Retrieves a boolean value from SharedPreferences.
     *
     * @param key The key whose value is to be retrieved
     * @return The stored boolean value, or false if the key doesn't exist
     */
    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    /**
     * Stores a string value in SharedPreferences.
     * Uses apply() for asynchronous storage of the value.
     *
     * @param key The key under which to store the string value
     * @param value The string value to store
     */
    public void putString(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Retrieves a string value from SharedPreferences.
     * Returns null if the key doesn't exist.
     *
     * @param key The key whose string value should be retrieved
     * @return The stored string value, or null if not found
     */
    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public void clear(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}

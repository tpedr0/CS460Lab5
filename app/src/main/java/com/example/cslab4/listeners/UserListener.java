package com.example.cslab4.listeners;

import com.example.cslab4.models.User;

/**
 * Interface for handling user selection events in the user list.
 * Implements callback pattern for user interaction events.
 */
public interface UserListener {
    /**
     * Called when a user is selected from the list.
     *
     * @param user The selected User object
     */
    void onUserClicked(User user);
}

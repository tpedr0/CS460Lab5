package com.example.cslab4.models;

import java.io.Serializable;

/**
 * Model class representing a user in the chat application.
 * Implements Serializable to support passing user objects between activities.
 * Contains user profile information including name, image, and contact details.
 */
public class User implements Serializable {
    public String name, image, email, token, id;
}

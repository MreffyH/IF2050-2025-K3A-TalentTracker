package com.example.util;

import com.example.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User loggedInUser;

    private SessionManager() {
        // Private constructor to prevent instantiation
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public void clearSession() {
        loggedInUser = null;
    }
} 
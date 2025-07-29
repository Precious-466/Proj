package com.example.apassignment1;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserDashboardController {
    @FXML private Label welcomeLabel;

    // Keep this for backward compatibility
    public void setUsername(String username) {
        welcomeLabel.setText("Welcome, " + username + "!");
    }

    // Add new method to handle User object
    public void setUser(User user) {
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
            // You can access other user properties here if needed
            // Example: user.getFullname(), user.getRole(), etc.
        }
    }
}
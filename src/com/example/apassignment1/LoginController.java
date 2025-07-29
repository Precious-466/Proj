package com.example.apassignment1;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.util.List;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter username and password.");
            return;
        }

        List<User> users = UserStorage.loadUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                if (user.getPassword().equals(password)) {
                    if (!user.isActive()) {
                        showAlert("Account Disabled", "Your account has been deactivated.");
                        return;
                    }

                    // Log successful login
                    ActivityLog.logActivity(username, "LOGIN", "User logged in");
                    user.login();
                    UserStorage.saveUsers(users);

                    HelloApplication.setCurrentUser(user);

                    try {
                        if (user.isAdmin()) {
                            HelloApplication.setRoot("admin-dashboard.fxml");
                        } else if (user.isGuide()) {
                            HelloApplication.setRoot("guide-dashboard.fxml");
                        } else {
                            HelloApplication.setRoot("user-dashboard.fxml");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Error", "Failed to load dashboard.");
                    }
                    return;
                } else {
                    ActivityLog.logActivity(username, "FAILED_LOGIN", "Incorrect password");
                    break;
                }
            }
        }

        // Fallback to default admin credentials
        if ("admin".equalsIgnoreCase(username) && "123456".equals(password)) {
            User adminUser = new User("admin", "123456", "Administrator", User.ROLE_ADMIN);
            ActivityLog.logActivity("admin", "ADMIN_LOGIN", "Admin logged in");
            try {
                HelloApplication.setCurrentUser(adminUser);
                HelloApplication.setRoot("admin-dashboard.fxml");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load admin dashboard.");
            }
            return;
        }

        showAlert("Login Failed", "Invalid username or password.");
    }

    @FXML
    private void goToRegister() {
        try {
            HelloApplication.setRoot("register-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open registration page.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
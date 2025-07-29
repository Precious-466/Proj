package com.example.apassignment1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.util.List;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private Button showPasswordBtn;
    @FXML private TextField fullnameField;

    private boolean passwordVisible = false;

    @FXML
    private void initialize() {
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        confirmPasswordVisibleField.setVisible(false);
        confirmPasswordVisibleField.setManaged(false);
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String pass = getPassword();
        String confirmPass = getConfirmPassword();
        String fullname = fullnameField.getText().trim();

        if (!validateRegistration(username, pass, confirmPass, fullname)) {
            return;
        }

        List<User> users = UserStorage.loadUsers();
        if (isUsernameTaken(users, username)) {
            showAlert("Error", "Username already exists.");
            return;
        }

        User newUser = new User(username, pass, fullname);
        users.add(newUser);
        UserStorage.saveUsers(users);

        // Log the registration
        ActivityLog.logActivity(username, "REGISTRATION", "New user registered");

        showAlert("Success", "Account created for " + username);
        clearFields();
        goToLogin();
    }

    private boolean validateRegistration(String username, String pass, String confirmPass, String fullname) {
        if (username.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() || fullname.isEmpty()) {
            showAlert("Error", "All fields are required.");
            return false;
        }

        if ("admin".equalsIgnoreCase(username)) {
            showAlert("Error", "Username 'admin' is reserved.");
            return false;
        }

        if (pass.length() < 6) {
            showAlert("Weak Password", "Password must be at least 6 characters.");
            return false;
        }

        if (!pass.equals(confirmPass)) {
            showAlert("Mismatch", "Passwords do not match.");
            return false;
        }

        return true;
    }

    private boolean isUsernameTaken(List<User> users, String username) {
        return users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    @FXML
    private void toggleShowPassword() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            passwordVisibleField.setText(passwordField.getText());
            confirmPasswordVisibleField.setText(confirmPasswordField.getText());
            showPasswordBtn.setText("🙈");
        } else {
            passwordField.setText(passwordVisibleField.getText());
            confirmPasswordField.setText(confirmPasswordVisibleField.getText());
            showPasswordBtn.setText("👁");
        }

        toggleFieldVisibility();
    }

    private void toggleFieldVisibility() {
        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
        passwordVisibleField.setVisible(passwordVisible);
        passwordVisibleField.setManaged(passwordVisible);

        confirmPasswordField.setVisible(!passwordVisible);
        confirmPasswordField.setManaged(!passwordVisible);
        confirmPasswordVisibleField.setVisible(passwordVisible);
        confirmPasswordVisibleField.setManaged(passwordVisible);
    }

    private String getPassword() {
        return passwordVisible ? passwordVisibleField.getText() : passwordField.getText();
    }

    private String getConfirmPassword() {
        return passwordVisible ? confirmPasswordVisibleField.getText() : confirmPasswordField.getText();
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        passwordVisibleField.clear();
        confirmPasswordField.clear();
        confirmPasswordVisibleField.clear();
        fullnameField.clear();

        if (passwordVisible) {
            toggleShowPassword();
        }
    }

    @FXML
    private void goToLogin() {
        try {
            HelloApplication.setRoot("login-view.fxml");
        } catch (Exception e) {
            showAlert("Error", "Failed to load login screen.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
package com.example.apassignment1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.util.Arrays;
import java.util.List;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private TextField fullnameField;
    @FXML private Button showPasswordBtn;
    @FXML private ComboBox<String> roleComboBox;

    private boolean passwordVisible = false;

    @FXML
    private void initialize() {
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        confirmPasswordVisibleField.setVisible(false);
        confirmPasswordVisibleField.setManaged(false);

        roleComboBox.getItems().addAll("tourist", "guide", "admin");
        roleComboBox.setValue("tourist"); // default role
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = getPassword();
        String confirmPass = getConfirmPassword();
        String fullname = fullnameField.getText().trim();
        String role = roleComboBox.getValue();

        if (!validateRegistration(username, password, confirmPass, fullname, role)) {
            return;
        }

        List<User> users = UserStorage.loadUsers(role); // ✅ role-specific loading
        if (isUsernameTaken(users, username)) {
            showAlert("Error", "Username already exists.");
            return;
        }

        User newUser = new User(username, password, fullname, role);
        users.add(newUser);
        UserStorage.saveUsers(users, role); // ✅ role-specific saving

        ActivityLog.logActivity(username, "REGISTRATION", "User registered with role: " + role);

        showAlert("Success", "Account created for " + username + " as " + role);
        clearFields();
        goToLogin();
    }

    private boolean validateRegistration(String username, String pass, String confirmPass, String fullname, String role) {
        if (username.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() || fullname.isEmpty() || role == null) {
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

        List<String> validRoles = Arrays.asList("admin", "guide", "tourist");
        if (!validRoles.contains(role.toLowerCase())) {
            showAlert("Error", "Invalid role selected.");
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
        roleComboBox.setValue("tourist");

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

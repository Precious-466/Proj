package com.example.apassignment1;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

public class TouristFormController {

    @FXML private TextField usernameField;
    @FXML private TextField fullnameField;
    @FXML private PasswordField passwordField;

    private ObservableList<User> userList;

    public void setUserList(ObservableList<User> userList) {
        this.userList = userList;
    }

    public void saveTourist() {
        String username = usernameField.getText().trim();
        String fullname = fullnameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || fullname.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Please fill all fields.");
            return;
        }

        // Check for duplicate username
        boolean exists = userList.stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists) {
            showAlert("Validation Error", "Username already exists.");
            return;
        }

        User newUser = new User(username, password, fullname);
        userList.add(newUser);
        UserStorage.saveUsers(userList);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

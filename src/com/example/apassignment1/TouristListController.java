package com.example.apassignment1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.util.ArrayList;
import java.util.List;

public class TouristListController {

    // FXML injections
    @FXML private ListView<Tourist> touristListView;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField fullnameField;
    @FXML private ListView<Package> availablePackagesListView;
    @FXML private ListView<Package> chosenPackagesListView;
    @FXML private Button addTouristBtn;
    @FXML private Button updateTouristBtn;
    @FXML private Button deleteTouristBtn;
    @FXML private Button choosePackageBtn;
    @FXML private Button removeChosenPackageBtn;

    private ObservableList<Tourist> tourists;
    private ObservableList<Package> availablePackages;

    @FXML
    public void initialize() {
        // Initialize data
        tourists = FXCollections.observableArrayList(TouristStorage.loadTourists());
        availablePackages = FXCollections.observableArrayList(PackageStorage.loadPackages());

        // Set up ListViews
        touristListView.setItems(tourists);
        availablePackagesListView.setItems(availablePackages);
        chosenPackagesListView.setItems(FXCollections.observableArrayList());

        // Selection listener
        touristListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                loadTouristDetails(newSel);
            } else {
                clearTouristDetails();
            }
        });
    }

    private void loadTouristDetails(Tourist tourist) {
        usernameField.setText(tourist.getUsername());
        passwordField.setText(tourist.getPassword());
        fullnameField.setText(tourist.getFullname());
        chosenPackagesListView.setItems(FXCollections.observableArrayList(tourist.getChosenPackages()));
    }

    private void clearTouristDetails() {
        usernameField.clear();
        passwordField.clear();
        fullnameField.clear();
        chosenPackagesListView.setItems(FXCollections.observableArrayList());
    }

    @FXML
    private void addTourist() {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String fullname = fullnameField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullname.isEmpty()) {
                showAlert("Error", "All fields are required!");
                return;
            }

            if (tourists.stream().anyMatch(t -> t.getUsername().equalsIgnoreCase(username))) {
                showAlert("Error", "Username already exists!");
                return;
            }

            Tourist newTourist = new Tourist(username, password, fullname, "tourist");
            tourists.add(newTourist);
            TouristStorage.saveTourists(new ArrayList<>(tourists));
            touristListView.refresh();
            showAlert("Success", "Tourist added successfully!");

        } catch (Exception e) {
            showAlert("Error", "Failed to add tourist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void updateTourist() {
        try {
            Tourist selected = touristListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Error", "Select a tourist to update.");
                return;
            }

            String newUsername = usernameField.getText().trim();
            String newPassword = passwordField.getText().trim();
            String newFullname = fullnameField.getText().trim();

            if (newUsername.isEmpty() || newPassword.isEmpty() || newFullname.isEmpty()) {
                showAlert("Error", "All fields are required.");
                return;
            }

            if (tourists.stream()
                    .filter(t -> t != selected)
                    .anyMatch(t -> t.getUsername().equalsIgnoreCase(newUsername))) {
                showAlert("Error", "Username already exists.");
                return;
            }

            selected.setUsername(newUsername);
            selected.setPassword(newPassword);
            selected.setFullname(newFullname);

            TouristStorage.saveTourists(new ArrayList<>(tourists));
            touristListView.refresh();
            showAlert("Success", "Tourist updated successfully!");

        } catch (Exception e) {
            showAlert("Error", "Failed to update tourist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteTourist() {
        try {
            Tourist selected = touristListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Error", "Select a tourist to delete.");
                return;
            }

            tourists.remove(selected);
            TouristStorage.saveTourists(new ArrayList<>(tourists));
            clearTouristDetails();
            showAlert("Success", "Tourist deleted successfully!");

        } catch (Exception e) {
            showAlert("Error", "Failed to delete tourist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void choosePackage() {
        try {
            Tourist selectedTourist = touristListView.getSelectionModel().getSelectedItem();
            Package selectedPackage = availablePackagesListView.getSelectionModel().getSelectedItem();

            if (selectedTourist == null) {
                showAlert("Error", "Select a tourist first.");
                return;
            }
            if (selectedPackage == null) {
                showAlert("Error", "Select a package to choose.");
                return;
            }

            if (selectedTourist.getChosenPackages().contains(selectedPackage)) {
                showAlert("Info", "Package already chosen.");
                return;
            }

            selectedTourist.getChosenPackages().add(selectedPackage);
            chosenPackagesListView.setItems(FXCollections.observableArrayList(selectedTourist.getChosenPackages()));
            TouristStorage.saveTourists(new ArrayList<>(tourists));
            showAlert("Success", "Package added successfully!");

        } catch (Exception e) {
            showAlert("Error", "Failed to add package: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void removeChosenPackage() {
        try {
            Tourist selectedTourist = touristListView.getSelectionModel().getSelectedItem();
            Package selectedPackage = chosenPackagesListView.getSelectionModel().getSelectedItem();

            if (selectedTourist == null) {
                showAlert("Error", "Select a tourist first.");
                return;
            }
            if (selectedPackage == null) {
                showAlert("Error", "Select a package to remove.");
                return;
            }

            selectedTourist.getChosenPackages().remove(selectedPackage);
            chosenPackagesListView.setItems(FXCollections.observableArrayList(selectedTourist.getChosenPackages()));
            TouristStorage.saveTourists(new ArrayList<>(tourists));
            showAlert("Success", "Package removed successfully!");

        } catch (Exception e) {
            showAlert("Error", "Failed to remove package: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
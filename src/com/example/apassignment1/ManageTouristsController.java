package com.example.apassignment1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Optional;

public class ManageTouristsController implements HelloApplication.RoleBasedController {

    // Table components
    @FXML private TableView<Tourist> touristsTable;
    @FXML private TableColumn<Tourist, String> usernameColumn;
    @FXML private TableColumn<Tourist, String> fullnameColumn;
    @FXML private TableColumn<Tourist, String> roleColumn;

    // Form components
    @FXML private TextField searchField;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    // Buttons
    @FXML private Button addTouristButton;
    @FXML private Button updateTouristButton;
    @FXML private Button deleteTouristButton;

    private ObservableList<Tourist> tourists = FXCollections.observableArrayList();
    private FilteredList<Tourist> filteredTourists;
    private Tourist selectedTourist;
    private User currentUser;

    @Override
    public void initializeWithUser(User user) {
        this.currentUser = user;
        initialize();
    }

    @FXML
    public void initialize() {
        try {
            // Verify all FXML injections
            if (touristsTable == null) throw new IllegalStateException("touristsTable not injected!");
            if (usernameColumn == null) throw new IllegalStateException("usernameColumn not injected!");
            if (fullnameColumn == null) throw new IllegalStateException("fullnameColumn not injected!");
            if (roleColumn == null) throw new IllegalStateException("roleColumn not injected!");

            // Setup table columns
            setupTableColumns();

            // Setup other components
            setupRoleComboBox();
            loadTouristData();
            setupSearchFilter();
            setupSelectionListener();
            disableActionButtons();
        } catch (Exception e) {
            System.err.println("Initialization error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Initialization Error", "Failed to initialize controller: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullnameColumn.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    private void setupRoleComboBox() {
        roleComboBox.getItems().setAll("tourist", "guide", "admin");
        roleComboBox.getSelectionModel().selectFirst();
    }

    private void loadTouristData() {
        tourists.setAll(TouristStorage.loadTourists());
        touristsTable.setItems(tourists);
    }

    private void setupSearchFilter() {
        filteredTourists = new FilteredList<>(tourists, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredTourists.setPredicate(tourist -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return tourist.getUsername().toLowerCase().contains(lowerCaseFilter) ||
                        tourist.getFullname().toLowerCase().contains(lowerCaseFilter) ||
                        tourist.getRole().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Tourist> sortedData = new SortedList<>(filteredTourists);
        sortedData.comparatorProperty().bind(touristsTable.comparatorProperty());
        touristsTable.setItems(sortedData);
    }

    @FXML
    private void handleSearch() {
        // Trigger search filter update
        String searchText = searchField.getText();
        if (searchText == null || searchText.isEmpty()) {
            touristsTable.setItems(tourists);
        } else {
            setupSearchFilter();
        }
    }

    private void setupSelectionListener() {
        touristsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedTourist = newSelection;
            if (newSelection != null) {
                populateFields(newSelection);
                enableActionButtons();
            } else {
                clearFields();
                disableActionButtons();
            }
        });
    }

    private void populateFields(Tourist tourist) {
        usernameField.setText(tourist.getUsername());
        fullNameField.setText(tourist.getFullname());
        passwordField.setText(tourist.getPassword());
        roleComboBox.setValue(tourist.getRole());
    }

    private void clearFields() {
        usernameField.clear();
        fullNameField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().selectFirst();
        touristsTable.getSelectionModel().clearSelection();
    }

    private void enableActionButtons() {
        updateTouristButton.setDisable(false);
        deleteTouristButton.setDisable(false);
        addTouristButton.setDisable(true);
    }

    private void disableActionButtons() {
        updateTouristButton.setDisable(true);
        deleteTouristButton.setDisable(true);
        addTouristButton.setDisable(false);
    }

    @FXML
    private void handleAddTourist() {
        try {
            String username = usernameField.getText().trim();
            String fullname = fullNameField.getText().trim();
            String password = passwordField.getText().trim();
            String role = roleComboBox.getValue();

            if (!validateInputs(username, fullname, password)) {
                return;
            }

            if (isUsernameTaken(username, null)) {
                showAlert("Error", "Username already exists");
                return;
            }

            Tourist newTourist = new Tourist(username, password, fullname, role);
            TouristStorage.addTourist(newTourist, currentUser.getUsername());
            ActivityLog.logTouristActivity(currentUser.getUsername(),
                    ActivityLog.TOURIST_CREATED, newTourist);

            loadTouristData();
            clearFields();
            showAlert("Success", "Tourist added successfully!");
        } catch (Exception e) {
            handleError("Failed to add tourist", e);
        }
    }

    @FXML
    private void handleUpdateTourist() {
        try {
            if (selectedTourist == null) {
                showAlert("Error", "No tourist selected");
                return;
            }

            String newUsername = usernameField.getText().trim();
            String newFullname = fullNameField.getText().trim();
            String newPassword = passwordField.getText().trim();
            String newRole = roleComboBox.getValue();

            if (!validateInputs(newUsername, newFullname, newPassword)) {
                return;
            }

            if (isUsernameTaken(newUsername, selectedTourist)) {
                showAlert("Error", "Username already exists");
                return;
            }

            selectedTourist.setUsername(newUsername);
            selectedTourist.setFullname(newFullname);
            selectedTourist.setPassword(newPassword);
            selectedTourist.setRole(newRole);

            TouristStorage.updateTourist(selectedTourist, currentUser.getUsername());
            ActivityLog.logTouristActivity(currentUser.getUsername(),
                    ActivityLog.TOURIST_UPDATED, selectedTourist);

            loadTouristData();
            showAlert("Success", "Tourist updated successfully!");
        } catch (Exception e) {
            handleError("Failed to update tourist", e);
        }
    }

    @FXML
    private void handleDeleteTourist() {
        try {
            if (selectedTourist == null) {
                showAlert("Error", "No tourist selected");
                return;
            }

            Optional<ButtonType> result = showConfirmation(
                    "Confirm Deletion",
                    "Delete Tourist",
                    "Are you sure you want to delete " + selectedTourist.getFullname() + "?"
            );

            if (result.isPresent() && result.get() == ButtonType.OK) {
                TouristStorage.deleteTourist(selectedTourist.getUsername(), currentUser.getUsername());
                ActivityLog.logTouristActivity(currentUser.getUsername(),
                        ActivityLog.TOURIST_DELETED, selectedTourist);

                loadTouristData();
                clearFields();
                showAlert("Success", "Tourist deleted successfully");
            }
        } catch (Exception e) {
            handleError("Failed to delete tourist", e);
        }
    }

    private boolean validateInputs(String username, String fullname, String password) {
        if (username.isEmpty() || fullname.isEmpty() || password.isEmpty()) {
            showAlert("Error", "All fields are required");
            return false;
        }

        if (password.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private boolean isUsernameTaken(String username, Tourist currentTourist) {
        return tourists.stream()
                .filter(t -> currentTourist == null || !t.getUsername().equals(currentTourist.getUsername()))
                .anyMatch(t -> t.getUsername().equalsIgnoreCase(username));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    private void handleError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
        e.printStackTrace();
    }
}
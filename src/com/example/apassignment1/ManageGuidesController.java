package com.example.apassignment1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

public class ManageGuidesController implements HelloApplication.RoleBasedController {
    @FXML private TableView<Guide> guidesTable;
    @FXML private TableColumn<Guide, String> usernameColumn;
    @FXML private TableColumn<Guide, String> fullnameColumn;
    @FXML private TableColumn<Guide, String> packagesColumn;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField fullnameField;

    @FXML private ListView<Package> availablePackagesListView;
    @FXML private ListView<Package> assignedPackagesListView;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button assignPackageButton;
    @FXML private Button removePackageButton;
    @FXML private Button saveGuideBtn;

    private ObservableList<Guide> guides;
    private ObservableList<Package> allPackages;
    private User currentUser;

    @Override
    public void initializeWithUser(User user) {
        this.currentUser = user;
        initialize();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        initializeListViewCells();
        refreshAllData();
        setupSelectionListener();
        setupButtonActions();
    }

    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullnameColumn.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        packagesColumn.setCellValueFactory(cellData -> cellData.getValue().packagesProperty());
    }

    private void initializeListViewCells() {
        availablePackagesListView.setCellFactory(lv -> new ListCell<Package>() {
            @Override
            protected void updateItem(Package item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    if (!item.isActive()) {
                        setStyle("-fx-text-fill: gray;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        assignedPackagesListView.setCellFactory(lv -> new ListCell<Package>() {
            @Override
            protected void updateItem(Package item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    if (!item.isActive()) {
                        setStyle("-fx-text-fill: gray;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void refreshAllData() {
        refreshGuides();
        refreshPackages();
    }

    private void refreshGuides() {
        guides = FXCollections.observableArrayList(GuideStorage.loadGuides());
        guidesTable.setItems(guides);
        clearForm();
    }

    public void refreshPackages() {
        allPackages = FXCollections.observableArrayList(PackageStorage.loadPackages());
        availablePackagesListView.setItems(allPackages);

        Guide selected = guidesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            assignedPackagesListView.setItems(FXCollections.observableArrayList(selected.getAssignedPackages()));
        }
    }

    private void setupSelectionListener() {
        guidesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> showGuideDetails(newSelection)
        );
    }

    private void showGuideDetails(Guide guide) {
        if (guide != null) {
            usernameField.setText(guide.getUsername());
            passwordField.setText(guide.getPassword());
            fullnameField.setText(guide.getFullname());

            assignedPackagesListView.setItems(FXCollections.observableArrayList(guide.getAssignedPackages()));

            availablePackagesListView.setItems(
                    allPackages.filtered(p -> !guide.getAssignedPackages().contains(p))
            );
        }
    }

    private void setupButtonActions() {
        addButton.setOnAction(e -> handleAddGuide());
        updateButton.setOnAction(e -> handleUpdateGuide());
        deleteButton.setOnAction(e -> handleDeleteGuide());
        assignPackageButton.setOnAction(e -> handleAssignPackage());
        removePackageButton.setOnAction(e -> handleRemovePackage());
        saveGuideBtn.setOnAction(e -> handleUpdateGuide());
    }

    @FXML
    private void handleAddGuide() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String fullname = fullnameField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullname.isEmpty()) {
            showAlert("Error", "All fields are required");
            return;
        }

        if (GuideStorage.usernameExists(username)) {
            showAlert("Error", "Username already exists");
            return;
        }

        Guide newGuide = new Guide(username, password, fullname);
        GuideStorage.addGuide(newGuide);
        refreshGuides();
        guidesTable.getSelectionModel().select(newGuide);
    }

    @FXML
    private void handleUpdateGuide() {
        Guide selectedGuide = guidesTable.getSelectionModel().getSelectedItem();
        if (selectedGuide == null) {
            showAlert("Error", "No guide selected");
            return;
        }

        String newUsername = usernameField.getText().trim();
        String newPassword = passwordField.getText().trim();
        String newFullname = fullnameField.getText().trim();

        if (newUsername.isEmpty() || newPassword.isEmpty() || newFullname.isEmpty()) {
            showAlert("Error", "All fields are required");
            return;
        }

        if (!newUsername.equals(selectedGuide.getUsername())) {
            if (GuideStorage.usernameExists(newUsername)) {
                showAlert("Error", "Username already exists");
                return;
            }
        }

        selectedGuide.setUsername(newUsername);
        selectedGuide.setPassword(newPassword);
        selectedGuide.setFullname(newFullname);
        selectedGuide.setAssignedPackages(new ArrayList<>(assignedPackagesListView.getItems()));

        GuideStorage.updateGuide(selectedGuide);
        refreshGuides();
    }

    @FXML
    private void handleDeleteGuide() {
        Guide selectedGuide = guidesTable.getSelectionModel().getSelectedItem();
        if (selectedGuide == null) {
            showAlert("Error", "No guide selected");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Guide");
        alert.setContentText("Are you sure you want to delete " + selectedGuide.getFullname() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                GuideStorage.deleteGuide(selectedGuide.getUsername());
                refreshGuides();
            }
        });
    }

    @FXML
    private void handleAssignPackage() {
        Guide guide = guidesTable.getSelectionModel().getSelectedItem();
        Package pkg = availablePackagesListView.getSelectionModel().getSelectedItem();

        if (guide == null || pkg == null) {
            showAlert("Error", "Select both a guide and a package");
            return;
        }

        guide.getAssignedPackages().add(pkg);
        GuideStorage.updateGuide(guide);
        showGuideDetails(guide);
    }

    @FXML
    private void handleRemovePackage() {
        Guide guide = guidesTable.getSelectionModel().getSelectedItem();
        Package pkg = assignedPackagesListView.getSelectionModel().getSelectedItem();

        if (guide == null || pkg == null) {
            showAlert("Error", "Select both a guide and a package");
            return;
        }

        guide.getAssignedPackages().remove(pkg);
        GuideStorage.updateGuide(guide);
        showGuideDetails(guide);
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        fullnameField.clear();
        assignedPackagesListView.getItems().clear();
        availablePackagesListView.setItems(allPackages);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
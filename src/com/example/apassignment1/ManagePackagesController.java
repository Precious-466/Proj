package com.example.apassignment1;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;

public class ManagePackagesController implements HelloApplication.RoleBasedController {

    @FXML private TableView<Package> packagesTable;
    @FXML private TableColumn<Package, String> idCol;
    @FXML private TableColumn<Package, String> nameCol;
    @FXML private TableColumn<Package, String> descriptionCol;
    @FXML private TableColumn<Package, Double> priceCol;
    @FXML private TableColumn<Package, String> locationCol;
    @FXML private TableColumn<Package, LocalDate> startDateCol;
    @FXML private TableColumn<Package, LocalDate> endDateCol;
    @FXML private TableColumn<Package, String> statusCol;

    @FXML private ComboBox<String> filterComboBox;

    private User currentUser;
    private ObservableList<Package> allPackages;
    private FilteredList<Package> filteredPackages;

    @Override
    public void initializeWithUser(User user) {
        this.currentUser = user;
        initialize();
    }

    private void initialize() {
        setupTableColumns();
        setupFilterComboBox();
        loadPackages();
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("$%.2f", price));
            }
        });

        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));
    }

    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All Packages", "Active Only", "Inactive Only"
        ));
        filterComboBox.getSelectionModel().selectFirst();
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));
    }

    private void loadPackages() {
        allPackages = FXCollections.observableArrayList(PackageStorage.loadPackages());
        filteredPackages = new FilteredList<>(allPackages);
        packagesTable.setItems(filteredPackages);
        applyFilter(filterComboBox.getValue());
    }

    private void applyFilter(String filter) {
        if (filter == null) return;

        switch (filter) {
            case "Active Only":
                filteredPackages.setPredicate(Package::isActive);
                break;
            case "Inactive Only":
                filteredPackages.setPredicate(p -> !p.isActive());
                break;
            default: // "All Packages"
                filteredPackages.setPredicate(null);
        }

    }

    @FXML
    private void handleAddPackage() {
        // Create a dialog to add new package
        Dialog<Package> dialog = new Dialog<>();
        dialog.setTitle("Add New Package");
        dialog.setHeaderText("Enter package details");

        // Set up form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextArea descriptionField = new TextArea();
        TextField priceField = new TextField();
        TextField locationField = new TextField();
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Location:"), 0, 3);
        grid.add(locationField, 1, 3);
        grid.add(new Label("Start Date:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("End Date:"), 0, 5);
        grid.add(endDatePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String id = "PKG-" + System.currentTimeMillis();
                    String name = nameField.getText();
                    String description = descriptionField.getText();
                    double price = Double.parseDouble(priceField.getText());
                    String location = locationField.getText();
                    LocalDate startDate = startDatePicker.getValue();
                    LocalDate endDate = endDatePicker.getValue();

                    if (name.isEmpty() || description.isEmpty() || location.isEmpty() ||
                            startDate == null || endDate == null) {
                        throw new IllegalArgumentException("All fields are required");
                    }

                    return new Package(id, name, description, price, startDate, endDate, location);
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid price");
                    return null;
                } catch (IllegalArgumentException e) {
                    showAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newPackage -> {
            allPackages.add(newPackage);
            PackageStorage.savePackages(allPackages);
            showAlert("Success", "Package added successfully!");
        });
    }

    @FXML
    private void handleEditPackage() {
        Package selected = packagesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a package to edit");
            return;
        }

        // Create a dialog to edit package
        Dialog<Package> dialog = new Dialog<>();
        dialog.setTitle("Edit Package");
        dialog.setHeaderText("Edit package details");

        // Set up form with current values
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selected.getName());
        TextArea descriptionField = new TextArea(selected.getDescription());
        TextField priceField = new TextField(String.valueOf(selected.getPrice()));
        TextField locationField = new TextField(selected.getLocation());
        DatePicker startDatePicker = new DatePicker(selected.getStartDate());
        DatePicker endDatePicker = new DatePicker(selected.getEndDate());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Location:"), 0, 3);
        grid.add(locationField, 1, 3);
        grid.add(new Label("Start Date:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("End Date:"), 0, 5);
        grid.add(endDatePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    selected.nameProperty().set(nameField.getText());
                    selected.descriptionProperty().set(descriptionField.getText());
                    selected.priceProperty().set(Double.parseDouble(priceField.getText()));
                    selected.locationProperty().set(locationField.getText());
                    selected.startDateProperty().set(startDatePicker.getValue());
                    selected.endDateProperty().set(endDatePicker.getValue());
                    return selected;
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid price");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(editedPackage -> {
            PackageStorage.savePackages(allPackages);
            packagesTable.refresh();
            showAlert("Success", "Package updated successfully!");
        });
    }

    @FXML
    private void handleDeletePackage() {
        Package selected = packagesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a package to delete");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Package");
        confirmation.setContentText("Are you sure you want to delete " + selected.getName() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                allPackages.remove(selected);
                PackageStorage.savePackages(allPackages);
                showAlert("Success", "Package deleted successfully!");
            }
        });
    }

    @FXML
    private void handleToggleStatus() {
        Package selected = packagesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a package to toggle status");
            return;
        }

        selected.isActiveProperty().set(!selected.isActive());
        PackageStorage.savePackages(allPackages);
        packagesTable.refresh();
        showAlert("Success", "Package status toggled successfully!");
    }

    @FXML
    private void handleRefresh() {
        loadPackages();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
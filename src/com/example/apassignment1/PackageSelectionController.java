package com.example.apassignment1;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PackageSelectionController implements HelloApplication.RoleBasedController {
    // UI Components (unchanged)
    @FXML private TableView<Package> packagesTable;
    @FXML private TableColumn<Package, String> nameColumn;
    @FXML private TableColumn<Package, String> descriptionColumn;
    @FXML private TableColumn<Package, Double> priceColumn;
    @FXML private TableColumn<Package, String> locationColumn;
    @FXML private TableColumn<Package, LocalDate> startDateColumn;
    @FXML private TableColumn<Package, LocalDate> endDateColumn;

    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, String> bookingPackageColumn;
    @FXML private TableColumn<Booking, LocalDate> bookingDateColumn;
    @FXML private TableColumn<Booking, Integer> peopleColumn;
    @FXML private TableColumn<Booking, Double> totalPriceColumn;

    @FXML private Label totalSpendingLabel;
    @FXML private Spinner<Integer> peopleSpinner;

    private User currentUser;
    private ObservableList<Package> packages;
    private ObservableList<Booking> userBookings;

    @Override
    public void initializeWithUser(User user) {
        this.currentUser = user;
        initialize();
    }

    private void initialize() {
        // Setup tables (unchanged)
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("$%.2f", price));
            }
        });
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        bookingPackageColumn.setCellValueFactory(cellData -> {
            String packageId = cellData.getValue().getPackageId();
            Package pkg = PackageStorage.loadPackages().stream()
                    .filter(p -> p.getId().equals(packageId))
                    .findFirst()
                    .orElse(null);
            return new SimpleStringProperty(pkg != null ? pkg.getName() : "Unknown Package");
        });
        bookingDateColumn.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        peopleColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfPeople"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalPriceColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("$%.2f", price));
            }
        });

        peopleSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        loadPackages();
        loadUserBookings();
        updateTotalSpending();
    }

    private void loadPackages() {
        packages = FXCollections.observableArrayList(
                PackageStorage.loadPackages().stream()
                        .filter(Package::isActive)
                        .collect(Collectors.toList())
        );
        packagesTable.setItems(packages);
    }

    private void loadUserBookings() {
        List<Booking> allBookings = BookingStorage.loadBookings();
        userBookings = FXCollections.observableArrayList(
                allBookings.stream()
                        .filter(b -> b.getUserId().equals(currentUser.getUsername()))
                        .collect(Collectors.toList())
        );
        bookingsTable.setItems(userBookings);
    }

    private void updateTotalSpending() {
        double total = userBookings.stream()
                .mapToDouble(Booking::getTotalPrice)
                .sum();
        totalSpendingLabel.setText(String.format("Total Spending: $%.2f", total));
    }

    @FXML
    private void handleBookPackage() {
        Package selectedPackage = packagesTable.getSelectionModel().getSelectedItem();
        if (selectedPackage == null) {
            showAlert("No Package Selected", "Please select a package to book");
            return;
        }

        int numberOfPeople = peopleSpinner.getValue();
        double totalPrice = selectedPackage.getPrice() * numberOfPeople;

        Booking booking = new Booking(
                generateBookingId(),
                currentUser.getUsername(),
                selectedPackage.getId(),
                LocalDate.now(),
                numberOfPeople,
                totalPrice
        );

        // Save booking
        List<Booking> bookings = BookingStorage.loadBookings();
        bookings.add(booking);
        BookingStorage.saveBookings(bookings);

        // Log activity
        ActivityLog.logActivity(
                currentUser.getUsername(),
                ActivityLog.TOURIST_PACKAGE_CHOSEN,
                String.format("Booked package '%s' for %d people (Total: $%.2f)",
                        selectedPackage.getName(),
                        numberOfPeople,
                        totalPrice)
        );

        // Refresh UI
        loadUserBookings();
        updateTotalSpending();
        showAlert("Success", "Package booked successfully!");
    }

    private String generateBookingId() {
        return "BK-" + System.currentTimeMillis();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
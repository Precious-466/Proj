package com.example.apassignment1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ReportController {

    @FXML private TextArea reportContent;
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button generateBtn;
    @FXML private Button exportBtn;
    @FXML private VBox reportContainer;

    @FXML
    public void initialize() {
        reportTypeComboBox.getItems().addAll(
                "User Registration Report",
                "System Usage Report",
                "Activity Log Report"
        );

        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        exportBtn.setDisable(true);
    }

    @FXML
    private void generateReport() {
        try {
            String selectedReport = reportTypeComboBox.getValue();
            if (selectedReport == null) {
                showAlert("Error", "Please select a report type");
                return;
            }

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showAlert("Error", "Please select both start and end dates");
                return;
            }

            if (endDate.isBefore(startDate)) {
                showAlert("Error", "End date cannot be before start date");
                return;
            }

            String report;
            switch (selectedReport) {
                case "User Registration Report":
                    report = generateUserRegistrationReport(startDate, endDate);
                    break;
                case "System Usage Report":
                    report = generateSystemUsageReport(startDate, endDate);
                    break;
                case "Activity Log Report":
                    report = generateActivityLogReport(startDate, endDate);
                    break;
                default:
                    report = "Error: Unknown report type";
                    break;
            }

            reportContent.setText(report);
            exportBtn.setDisable(false);
        } catch (Exception e) {
            showAlert("Error", "Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateUserRegistrationReport(LocalDate startDate, LocalDate endDate) {
        List<User> users = UserStorage.loadUsers();
        List<User> filteredUsers = users.stream()
                .filter(user -> !user.getRegistrationDate().toLocalDate().isBefore(startDate))
                .filter(user -> !user.getRegistrationDate().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());

        StringBuilder report = new StringBuilder();
        report.append("USER REGISTRATION REPORT\n");
        report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
        report.append("Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        report.append(String.format("%-20s %-25s %-15s %-10s %-20s\n",
                "Username", "Full Name", "Role", "Status", "Registration Date"));
        report.append("----------------------------------------------------------------------------\n");

        filteredUsers.forEach(user -> {
            report.append(String.format("%-20s %-25s %-15s %-10s %s\n",
                    user.getUsername(),
                    user.getFullname(),
                    user.getRole(),
                    user.isActive() ? "Active" : "Inactive",
                    user.getFormattedRegistrationDate()));
        });

        report.append("\nSummary Statistics:\n");
        report.append(String.format("%-30s %10d\n", "Total Users:", filteredUsers.size()));
        report.append(String.format("%-30s %10d\n", "Active Users:",
                filteredUsers.stream().filter(User::isActive).count()));
        report.append(String.format("%-30s %10d\n", "New Users Today:",
                filteredUsers.stream()
                        .filter(u -> u.getRegistrationDate().toLocalDate().equals(LocalDate.now()))
                        .count()));

        return report.toString();
    }

    private String generateSystemUsageReport(LocalDate startDate, LocalDate endDate) {
        List<User> users = UserStorage.loadUsers();
        long totalUsers = users.size();
        long activeUsers = users.stream().filter(User::isActive).count();

        // Get login activities (assuming login activities are logged with a specific type)
        long logins = ActivityLog.getFullActivityLog().stream()
                .filter(a -> a.getTimestamp().toLocalDate().isAfter(startDate.minusDays(1)))
                .filter(a -> a.getTimestamp().toLocalDate().isBefore(endDate.plusDays(1)))
                .filter(a -> a.getActivityType().equals("USER_LOGIN")) // You'll need to define this constant
                .count();

        long newRegistrations = users.stream()
                .filter(u -> !u.getRegistrationDate().toLocalDate().isBefore(startDate))
                .filter(u -> !u.getRegistrationDate().toLocalDate().isAfter(endDate))
                .count();

        StringBuilder report = new StringBuilder();
        report.append("SYSTEM USAGE REPORT\n");
        report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
        report.append("Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        report.append("User Statistics:\n");
        report.append(String.format("%-30s %10d\n", "Total Users:", totalUsers));
        report.append(String.format("%-30s %10d\n", "Active Users:", activeUsers));
        report.append(String.format("%-30s %10.2f%%\n", "Active Percentage:",
                totalUsers > 0 ? (activeUsers * 100.0 / totalUsers) : 0));

        report.append("\nActivity Statistics:\n");
        report.append(String.format("%-30s %10d\n", "Total Logins:", logins));
        report.append(String.format("%-30s %10d\n", "New Registrations:", newRegistrations));
        report.append(String.format("%-30s %10.2f\n", "Avg Logins/Day:",
                (double)logins / startDate.until(endDate).getDays() + 1));

        return report.toString();
    }

    private String generateActivityLogReport(LocalDate startDate, LocalDate endDate) {
        List<ActivityLog.Activity> activities = ActivityLog.getActivitiesBetween(
                startDate,
                endDate
        );

        StringBuilder report = new StringBuilder();
        report.append("ACTIVITY LOG REPORT\n");
        report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
        report.append("Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        if (activities.isEmpty()) {
            report.append("No activities found for the selected period\n");
        } else {
            report.append(String.format("%-20s %-15s %-30s %-25s\n",
                    "Username", "Activity Type", "Description", "Timestamp"));
            report.append("--------------------------------------------------------------------------------\n");

            activities.forEach(activity -> {
                report.append(String.format("%-20s %-15s %-30s %s\n",
                        activity.getUsername(),
                        activity.getActivityType(),
                        activity.getDescription(),
                        activity.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            });
        }

        report.append("\nTotal Activities: ").append(activities.size());

        return report.toString();
    }

    @FXML
    private void exportReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            String reportType = reportTypeComboBox.getValue();
            String fileName = reportType.toLowerCase().replace(" ", "_") + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            fileChooser.setInitialFileName(fileName);

            File file = fileChooser.showSaveDialog(reportContainer.getScene().getWindow());
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.write(reportContent.getText());
                    showAlert("Success", "Report exported successfully to:\n" + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to export report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package com.example.apassignment1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminHomeController implements HelloApplication.RoleBasedController {

    // Summary Cards
    @FXML private Label totalUsersLabel;
    @FXML private Label userGrowthLabel;
    @FXML private Label activeToursLabel;
    @FXML private Label toursTodayLabel;
    @FXML private Label revenueLabel;
    @FXML private Label revenueGrowthLabel;

    // Charts
    @FXML private BarChart<String, Number> userGrowthChart;
    @FXML private LineChart<String, Number> revenueTrendChart;
    @FXML private PieChart userDistributionChart;

    // Recent Activity Table
    @FXML private TableView<ActivityLog.Activity> recentActivityTable;
    @FXML private TableColumn<ActivityLog.Activity, String> timeColumn;
    @FXML private TableColumn<ActivityLog.Activity, String> activityColumn;
    @FXML private TableColumn<ActivityLog.Activity, String> userColumn;

    private User currentUser;

    @Override
    public void initializeWithUser(User user) {
        this.currentUser = user;
        initializeDashboard();
    }

    private void initializeDashboard() {
        setupSummaryCards();
        initializeCharts();
        setupRecentActivityTable();
    }

    private void setupSummaryCards() {
        // Total Users
        int totalUsers = UserStorage.getTotalUserCount();
        totalUsersLabel.setText(String.valueOf(totalUsers));
        userGrowthLabel.setText(String.format("↑ %.1f%% from last month",
                UserStorage.getUserGrowthPercentage()));

        // Active Tours
        int activeTours = TourStorage.getActiveTourCount();
        activeToursLabel.setText(String.valueOf(activeTours));
        toursTodayLabel.setText(TourStorage.getToursStartingToday() + " starting today");

        // Revenue
        double revenue = BookingStorage.getMonthlyRevenue();
        revenueLabel.setText(String.format("$%.2f", revenue));
        revenueGrowthLabel.setText(String.format("↑ %.1f%% from last month",
                BookingStorage.getRevenueGrowthPercentage()));
    }

    private void initializeCharts() {
        // User Growth Chart
        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        userSeries.setName("New Users");
        Map<String, Integer> userGrowthData = UserStorage.getUserGrowthLast6Months();
        userGrowthData.forEach((month, count) ->
                userSeries.getData().add(new XYChart.Data<>(month, count)));
        userGrowthChart.getData().add(userSeries);

        // Revenue Trend Chart
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue ($)");
        Map<String, Double> revenueData = BookingStorage.getRevenueLast6Months();
        revenueData.forEach((month, amount) ->
                revenueSeries.getData().add(new XYChart.Data<>(month, amount)));
        revenueTrendChart.getData().add(revenueSeries);

        // User Distribution Pie Chart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Admins", UserStorage.getAdminCount()),
                new PieChart.Data("Guides", UserStorage.getGuideCount()),
                new PieChart.Data("Tourists", UserStorage.getTouristCount())
        );
        userDistributionChart.setData(pieData);
    }

    private void setupRecentActivityTable() {
        // Configure columns
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        activityColumn.setCellValueFactory(new PropertyValueFactory<>("activityType"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Load data - using getFullActivityLog() instead of getRecentActivities()
        ObservableList<ActivityLog.Activity> recentActivities =
                FXCollections.observableArrayList(ActivityLog.getFullActivityLog())
                        .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                        .stream()
                        .limit(10)
                        .collect(FXCollections::observableArrayList, List::add, List::addAll);

        recentActivityTable.setItems(recentActivities);
    }

    // Helper method to format timestamp if needed
    private String formatTimestamp(LocalDateTime timestamp) {
        return timestamp.toString(); // You can use DateTimeFormatter for better formatting
    }
}
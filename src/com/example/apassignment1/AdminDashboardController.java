package com.example.apassignment1;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

public class AdminDashboardController implements HelloApplication.RoleBasedController {

    // UI Components
    @FXML private StackPane mainContentPane;
    @FXML private VBox dashboardContainer;
    @FXML private PieChart userPieChart;
    @FXML private PieChart activityPieChart;
    @FXML private BarChart<String, Number> registrationBarChart;
    @FXML private LineChart<String, Number> activityTrendChart;
    @FXML private TableView<ActivityLog.Activity> recentActivitiesTable;
    @FXML private TableColumn<ActivityLog.Activity, String> userColumn;
    @FXML private TableColumn<ActivityLog.Activity, String> activityColumn;
    @FXML private TableColumn<ActivityLog.Activity, String> timestampColumn;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private Label welcomeLabel;
    @FXML private Label nameLabel;
    @FXML private Label rankLabel;
    @FXML private Button homeButton;
    @FXML private Button generateReportButton;
    @FXML private Button manageTouristsButton;
    @FXML private Button manageGuidesButton;
    @FXML private Button managePackagesButton;
    @FXML private Button logoutButton;
    @FXML private Label footerLabel;

    private User currentUser;
    private Node defaultHomeContent;
    private final String PREFS_FILE = "user_prefs.json";

    @Override
    public void initializeWithUser(User user) {
        this.currentUser = user;
        Platform.runLater(() -> {
            try {
                initialize();
                initializeCharts();
            } catch (Exception e) {
                handleCriticalError("Failed to initialize dashboard", e);
            }
        });
    }

    @FXML
    public void initialize() {
        try {
            verifyFxmlComponents();
            setEnglishText();

            // Store the default home content
            defaultHomeContent = mainContentPane.getChildren().get(0);

            // Initialize empty states
            recentActivitiesTable.setPlaceholder(new Label("Loading recent activities..."));

            // Initialize empty charts
            userPieChart.setData(FXCollections.observableArrayList());
            activityPieChart.setData(FXCollections.observableArrayList());
            registrationBarChart.getData().clear();
            activityTrendChart.getData().clear();

            // Load preferences
            UserPreferences prefs = safelyLoadPreferences();
            safelyInitializeLanguageSettings(prefs);

            // Set user info
            if (currentUser != null) {
                nameLabel.setText("Name: " + currentUser.getFullname());
                rankLabel.setText("Rank: " + currentUser.getRole());
            }

        } catch (Exception e) {
            handleCriticalError("Dashboard initialization failed", e);
        }
    }

    private void verifyFxmlComponents() throws IllegalStateException {
        Objects.requireNonNull(languageComboBox, "languageComboBox not injected!");
        Objects.requireNonNull(welcomeLabel, "welcomeLabel not injected!");
        Objects.requireNonNull(mainContentPane, "mainContentPane not injected!");
        Objects.requireNonNull(recentActivitiesTable, "recentActivitiesTable not injected!");
        Objects.requireNonNull(timestampColumn, "timestampColumn not injected!");
        Objects.requireNonNull(dashboardContainer, "dashboardContainer not injected!");
    }

    private void initializeCharts() {
        try {
            createUserChart();
            createActivityChart();
            createRegistrationBarChart();
            createActivityTrendChart();
            setupRecentActivitiesTable();
        } catch (Exception e) {
            handleCriticalError("Failed to initialize charts", e);
        }
    }

    private void createUserChart() throws Exception {
        List<User> users = UserStorage.loadUsers();
        if (users == null || users.isEmpty()) {
            userPieChart.setTitle("No user data available");
            return;
        }

        long active = users.stream().filter(User::isActive).count();
        long inactive = users.size() - active;

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Active (" + active + ")", active),
                new PieChart.Data("Inactive (" + inactive + ")", inactive)
        );

        Platform.runLater(() -> {
            userPieChart.setData(data);
            userPieChart.setTitle("User Status");
            userPieChart.setLegendVisible(true);

            int i = 0;
            for (PieChart.Data d : userPieChart.getData()) {
                d.getNode().setStyle("-fx-pie-color: " + (i++ == 0 ? "#2ecc71" : "#e74c3c") + ";");
            }
        });
    }

    private void createActivityChart() throws Exception {
        List<ActivityLog.Activity> activities = ActivityLog.getFullActivityLog();
        if (activities == null || activities.isEmpty()) {
            activityPieChart.setTitle("No activity data available");
            return;
        }

        long logins = activities.stream()
                .filter(a -> a.getActivityType().equals("LOGIN"))
                .count();
        long others = activities.size() - logins;

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Logins (" + logins + ")", logins),
                new PieChart.Data("Others (" + others + ")", others)
        );

        Platform.runLater(() -> {
            activityPieChart.setData(data);
            activityPieChart.setTitle("Activity Types");
            activityPieChart.setLegendVisible(true);

            int i = 0;
            for (PieChart.Data d : activityPieChart.getData()) {
                d.getNode().setStyle("-fx-pie-color: " + (i++ == 0 ? "#3498db" : "#9b59b6") + ";");
            }
        });
    }

    private void createRegistrationBarChart() throws Exception {
        registrationBarChart.getData().clear();

        Map<String, Long> monthlyRegistrations = UserStorage.getMonthlyRegistrations();
        if (monthlyRegistrations == null || monthlyRegistrations.isEmpty()) {
            registrationBarChart.setTitle("No registration data available");
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Registrations");

        monthlyRegistrations.forEach((month, count) -> {
            series.getData().add(new XYChart.Data<>(month, count));
        });

        Platform.runLater(() -> {
            registrationBarChart.getData().add(series);
            registrationBarChart.setTitle("Monthly Registrations");

            for (XYChart.Data<String, Number> d : series.getData()) {
                d.getNode().setStyle("-fx-bar-fill: #1abc9c;");
            }
        });
    }

    private void createActivityTrendChart() throws Exception {
        activityTrendChart.getData().clear();

        Map<String, Long> weeklyActivities = ActivityLog.getWeeklyActivityCounts();
        if (weeklyActivities == null || weeklyActivities.isEmpty()) {
            activityTrendChart.setTitle("No activity trend data available");
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Activities");

        weeklyActivities.forEach((day, count) -> {
            series.getData().add(new XYChart.Data<>(day, count));
        });

        Platform.runLater(() -> {
            activityTrendChart.getData().add(series);
            activityTrendChart.setTitle("Weekly Activity Trend");

            series.getNode().setStyle("-fx-stroke: #e74c3c; -fx-stroke-width: 2px;");
        });
    }

    private void setupRecentActivitiesTable() throws Exception {
        userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        activityColumn.setCellValueFactory(new PropertyValueFactory<>("activityType"));
        timestampColumn.setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getTimestamp();
            String formattedTime = timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
            return new SimpleStringProperty(formattedTime);
        });

        List<ActivityLog.Activity> activities = ActivityLog.getFullActivityLog();
        if (activities == null || activities.isEmpty()) {
            recentActivitiesTable.setPlaceholder(new Label("No recent activities found"));
            return;
        }

        List<ActivityLog.Activity> recentActivities = activities.stream()
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .limit(10)
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            recentActivitiesTable.setItems(FXCollections.observableArrayList(recentActivities));
        });
    }

    @FXML
    public void handleHome() {
        mainContentPane.getChildren().setAll(defaultHomeContent);
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            initializeCharts();
        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Dashboard Refresh Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to refresh dashboard: " + e.getMessage());
                alert.showAndWait();
            });
            System.err.println("Dashboard refresh failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManageTourists() {
        loadContent("manage-tourists.fxml", "Tourist Management");
    }

    @FXML
    private void handleManageGuides() {
        loadContent("manage-guides.fxml", "Guide Management");
    }

    @FXML
    private void handleManagePackages() {
        loadContent("manage-packages.fxml", "Package Management");
    }

    @FXML
    private void handleGenerateReport() {
        loadContent("report-view.fxml", "Report Generation");
    }

    private void loadContent(String fxmlFile, String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node content = loader.load();

            if (loader.getController() instanceof HelloApplication.RoleBasedController) {
                ((HelloApplication.RoleBasedController) loader.getController()).initializeWithUser(currentUser);
            }

            mainContentPane.getChildren().setAll(content);
        } catch (IOException e) {
            handleScreenLoadError(screenName, e);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            ActivityLog.logActivity(currentUser.getUsername(), "LOGOUT", "User logged out");
            HelloApplication.clearCurrentUser();
            HelloApplication.setRoot("login-view.fxml");
        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Logout Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to logout: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    // Preference handling methods
    private UserPreferences safelyLoadPreferences() {
        try {
            return loadPreferences();
        } catch (IOException e) {
            showAlert("Preferences Error", "Using default settings. Error: " + e.getMessage());
            return new UserPreferences();
        }
    }

    private UserPreferences loadPreferences() throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(PREFS_FILE)) {
            UserPreferences prefs = gson.fromJson(reader, UserPreferences.class);
            return prefs != null ? prefs : new UserPreferences();
        }
    }

    private void savePreferences(UserPreferences prefs) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(PREFS_FILE)) {
            gson.toJson(prefs, writer);
        }
    }

    private void safelyInitializeLanguageSettings(UserPreferences prefs) {
        languageComboBox.getItems().setAll("English", "नेपाली");
        languageComboBox.getSelectionModel().select(prefs.language);
        switchLanguage(prefs.language);

        languageComboBox.setOnAction(event -> {
            try {
                String selectedLanguage = languageComboBox.getValue();
                if (selectedLanguage != null) {
                    switchLanguage(selectedLanguage);
                    prefs.language = selectedLanguage;
                    savePreferences(prefs);
                }
            } catch (Exception e) {
                showAlert("Language Error", "Couldn't save language preference: " + e.getMessage());
            }
        });
    }

    private void switchLanguage(String lang) {
        if ("नेपाली".equals(lang)) {
            setNepaliText();
        } else {
            setEnglishText();
        }
    }

    private void setNepaliText() {
        welcomeLabel.setText("प्रशासन ड्यासबोर्डमा स्वागत छ");
        nameLabel.setText(currentUser != null ? "नाम: " + currentUser.getFullname() : "नाम: प्रशासक");
        rankLabel.setText("पद: " + (currentUser != null ? currentUser.getRole() : "प्रशासक"));
        homeButton.setText("गृहपृष्ठ");
        generateReportButton.setText("रिपोर्ट बनाउनुहोस्");
        manageTouristsButton.setText("पर्यटक व्यवस्थापन");
        manageGuidesButton.setText("गाइड व्यवस्थापन");
        managePackagesButton.setText("प्याकेज व्यवस्थापन");
        logoutButton.setText("लगआउट");
        footerLabel.setText("पर्यटन व्यवस्थापन प्रणाली");
    }

    private void setEnglishText() {
        welcomeLabel.setText("Welcome to Admin Dashboard");
        nameLabel.setText(currentUser != null ? "Name: " + currentUser.getFullname() : "Name: Admin");
        rankLabel.setText("Rank: " + (currentUser != null ? currentUser.getRole() : "Admin"));
        homeButton.setText("Home");
        generateReportButton.setText("Generate Report");
        manageTouristsButton.setText("Manage Tourists");
        manageGuidesButton.setText("Manage Guides");
        managePackagesButton.setText("Manage Packages");
        logoutButton.setText("Logout");
        footerLabel.setText("Tourism Management System");
    }

    // Error handling methods
    private void handleCriticalError(String context, Exception e) {
        System.err.println("CRITICAL ERROR: " + context);
        e.printStackTrace();
        Platform.runLater(() -> showAlert("Critical Error", context + ". Application may not function properly."));
    }

    private void handleScreenLoadError(String screenName, Exception e) {
        System.err.println("Failed to load " + screenName + " screen");
        e.printStackTrace();
        Platform.runLater(() -> {
            showAlert("Load Error", "Couldn't load " + screenName + " screen. Error: " + e.getMessage());
            handleHome();
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class UserPreferences {
        String language = "English";
    }
}
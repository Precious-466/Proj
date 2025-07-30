package com.example.apassignment1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private Label welcomeLabel;
    @FXML private Button loginButton;
    @FXML private Button languageButton;
    @FXML private Hyperlink registerLink;

    private boolean isNepali = false;
    private final Map<String, String[]> languageMap = new HashMap<>();

    @FXML
    public void initialize() {
        initializeLanguageMap();
        setupComboBox();
    }

    private void initializeLanguageMap() {
        languageMap.put("welcome", new String[]{"Welcome Back", "स्वागत छ"});
        languageMap.put("username", new String[]{"Username", "प्रयोगकर्ता नाम"});
        languageMap.put("password", new String[]{"Password", "पासवर्ड"});
        languageMap.put("userType", new String[]{"Select User Type", "प्रयोगकर्ता प्रकार चयन गर्नुहोस्"});
        languageMap.put("login", new String[]{"Login", "लगइन गर्नुहोस्"});
        languageMap.put("register", new String[]{"Don't have an account? Register", "खाता छैन? दर्ता गर्नुहोस्"});
        languageMap.put("language", new String[]{"नेपाली", "English"});
        languageMap.put("tourist", new String[]{"Tourist", "पर्यटक"});
        languageMap.put("guide", new String[]{"Guide", "गाईड"});
        languageMap.put("admin", new String[]{"Admin", "प्रशासक"});
    }

    private void setupComboBox() {
        userTypeComboBox.getItems().setAll(
                languageMap.get("tourist")[0],
                languageMap.get("guide")[0],
                languageMap.get("admin")[0]
        );
        userTypeComboBox.setValue(languageMap.get("tourist")[0]);
    }

    @FXML
    private void toggleLanguage() {
        isNepali = !isNepali;
        updateUI();
    }

    private void updateUI() {
        int langIndex = isNepali ? 1 : 0;

        welcomeLabel.setText(languageMap.get("welcome")[langIndex]);
        usernameField.setPromptText(languageMap.get("username")[langIndex]);
        passwordField.setPromptText(languageMap.get("password")[langIndex]);
        userTypeComboBox.setPromptText(languageMap.get("userType")[langIndex]);
        loginButton.setText(languageMap.get("login")[langIndex]);
        registerLink.setText(languageMap.get("register")[langIndex]);
        languageButton.setText(languageMap.get("language")[langIndex]);

        userTypeComboBox.getItems().setAll(
                languageMap.get("tourist")[langIndex],
                languageMap.get("guide")[langIndex],
                languageMap.get("admin")[langIndex]
        );

        userTypeComboBox.setValue(languageMap.get("tourist")[langIndex]);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String selectedUserType = userTypeComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || selectedUserType == null) {
            showAlert("Error", "Please fill all fields.");
            return;
        }

        // Map display text back to internal role
        String role = mapDisplayedRoleToInternal(selectedUserType);

        // Default hardcoded admin login
        if ("admin".equalsIgnoreCase(username) && "123456".equals(password) && "admin".equalsIgnoreCase(role)) {
            User adminUser = new User("admin", "123456", "Administrator", User.ROLE_ADMIN);
            ActivityLog.logActivity("admin", "ADMIN_LOGIN", "Admin logged in");
            try {
                HelloApplication.setCurrentUser(adminUser);
                HelloApplication.setRoot("admin-dashboard.fxml");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load admin dashboard.");
            }
            closeLoginWindow();
            return;
        }

        // ✅ Load users for selected role only
        List<User> users = UserStorage.loadUsers(role);

        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                if (!userTypeMatchesRole(user, role)) {
                    ActivityLog.logActivity(username, "FAILED_LOGIN", "User type mismatch");
                    showAlert("Login Failed", "Invalid user type for this account.");
                    return;
                }

                if (user.getPassword().equals(password)) {
                    if (!user.isActive()) {
                        showAlert("Account Disabled", "Your account has been deactivated.");
                        return;
                    }

                    ActivityLog.logActivity(username, "LOGIN", "User logged in");
                    user.login(); // updates last login date
                    UserStorage.saveUsers(users, role); // ✅ save back to correct file

                    HelloApplication.setCurrentUser(user);
                    try {
                        switch (user.getRole().toLowerCase()) {
                            case "admin" -> HelloApplication.setRoot("admin-dashboard.fxml");
                            case "guide" -> HelloApplication.setRoot("guide-dashboard.fxml");
                            default -> HelloApplication.setRoot("user-dashboard.fxml");
                        }
                        closeLoginWindow();
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Error", "Failed to load dashboard.");
                    }
                    return;
                } else {
                    ActivityLog.logActivity(username, "FAILED_LOGIN", "Incorrect password");
                    break;
                }
            }
        }

        showAlert("Login Failed", "Invalid username or password.");
    }

    private String mapDisplayedRoleToInternal(String displayedRole) {
        if (isNepali) {
            if (displayedRole.equals(languageMap.get("admin")[1])) return "admin";
            if (displayedRole.equals(languageMap.get("guide")[1])) return "guide";
            if (displayedRole.equals(languageMap.get("tourist")[1])) return "tourist";
        }
        return displayedRole.toLowerCase(); // fallback
    }

    private boolean userTypeMatchesRole(User user, String role) {
        return (role.equalsIgnoreCase("admin") && user.isAdmin()) ||
                (role.equalsIgnoreCase("guide") && user.isGuide()) ||
                (role.equalsIgnoreCase("tourist") && !user.isAdmin() && !user.isGuide());
    }

    @FXML
    private void goToRegister() {
        try {
            HelloApplication.setRoot("register-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open registration page.");
        }
    }

    private void closeLoginWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

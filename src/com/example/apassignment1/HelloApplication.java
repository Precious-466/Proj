package com.example.apassignment1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.ArrayList;

public class HelloApplication extends Application {
    public static Stage mainStage;
    private static User currentUser;

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 650);
        stage.setTitle("Tourism Management System");
        stage.setScene(scene);
        stage.show();

        // Create default admin if none exists
        initializeDefaultAdmin();
    }

    private void initializeDefaultAdmin() {
        // Load from admin.json
        List<User> users = new ArrayList<>(UserStorage.loadUsers("admin"));
        boolean adminExists = false;

        for (User u : users) {
            if (u.getRole().equalsIgnoreCase(User.ROLE_ADMIN)) {
                adminExists = true;
                break;
            }
        }

        if (!adminExists) {
            User admin = new User("admin", "123456", "Administrator", User.ROLE_ADMIN);
            users.add(admin);
            UserStorage.saveUsers(users, "admin");
            ActivityLog.logActivity("system", "INIT", "Default admin account created");
        }
    }

    public static void setRoot(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxml));
        Scene scene = new Scene(loader.load(), 900, 650);
        mainStage.setScene(scene);

        // Pass user to controller if applicable
        Object controller = loader.getController();
        if (controller instanceof RoleBasedController) {
            ((RoleBasedController) controller).initializeWithUser(currentUser);
        }
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearCurrentUser() {
        if (currentUser != null) {
            currentUser.logout();
        }
        currentUser = null;
    }

    public static void main(String[] args) {
        launch();
    }

    public interface RoleBasedController {
        void initializeWithUser(User user);
    }
}

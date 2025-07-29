package com.example.apassignment1;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {
    // Role constants
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_GUIDE = "guide";
    public static final String ROLE_USER = "tourist";

    private String username;
    private String password;
    private String fullname;
    private String role;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;
    private boolean active;

    public User() {
        this.role = ROLE_USER;
        this.registrationDate = LocalDateTime.now();
        this.active = true;
    }

    public User(String username, String password, String fullname) {
        this(username, password, fullname, ROLE_USER);
    }

    public User(String username, String password, String fullname, String role) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.role = role;
        this.registrationDate = LocalDateTime.now();
        this.active = true;
        ActivityLog.logActivity(username, "USER_CREATED", "New user registered");
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) {
        ActivityLog.logActivity(this.username, "USERNAME_CHANGE",
                "Changed from " + this.username + " to " + username);
        this.username = username;
    }
    public StringProperty usernameProperty() { return new SimpleStringProperty(username); }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        ActivityLog.logActivity(username, "PASSWORD_CHANGE", "Password updated");
        this.password = password;
    }
    public StringProperty passwordProperty() { return new SimpleStringProperty(password); }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) {
        ActivityLog.logActivity(username, "PROFILE_UPDATE",
                "Name changed from " + this.fullname + " to " + fullname);
        this.fullname = fullname;
    }
    public StringProperty fullnameProperty() { return new SimpleStringProperty(fullname); }

    public String getRole() { return role; }
    public void setRole(String role) {
        ActivityLog.logActivity(username, "ROLE_CHANGE",
                "Changed from " + this.role + " to " + role);
        this.role = role;
    }
    public StringProperty roleProperty() { return new SimpleStringProperty(role); }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }
    public String getFormattedRegistrationDate() {
        return registrationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public LocalDateTime getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
    public String getFormattedLastLoginDate() {
        return lastLoginDate != null ?
                lastLoginDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Never";
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) {
        String action = active ? "ACTIVATED" : "DEACTIVATED";
        ActivityLog.logActivity(username, "ACCOUNT_STATUS",
                "Account " + action);
        this.active = active;
    }
    public StringProperty activeProperty() {
        return new SimpleStringProperty(active ? "Active" : "Inactive");
    }

    // Role check methods
    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(this.role);
    }

    public boolean isGuide() {
        return ROLE_GUIDE.equalsIgnoreCase(this.role);
    }

    public boolean isRegularUser() {
        return ROLE_USER.equalsIgnoreCase(this.role);
    }

    // Activity methods
    public void login() {
        this.lastLoginDate = LocalDateTime.now();
        ActivityLog.logActivity(username, "LOGIN", "User logged in");
    }

    public void logout() {
        ActivityLog.logActivity(username, "LOGOUT", "User logged out");
    }

    public String toReportString() {
        return String.format("%-20s %-25s %-15s %-10s %-20s %-20s",
                username,
                fullname,
                role,
                active ? "Active" : "Inactive",
                getFormattedRegistrationDate(),
                getFormattedLastLoginDate());
    }

    @Override
    public String toString() {
        return username;
    }
}
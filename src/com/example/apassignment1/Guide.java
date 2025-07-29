package com.example.apassignment1;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Guide {
    private String username;
    private String password;
    private String fullname;
    private String role = "guide";  // Default role
    private List<Package> assignedPackages = new ArrayList<>();

    public Guide() {}

    public Guide(String username, String password, String fullname) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
    }

    // Standard getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<Package> getAssignedPackages() { return assignedPackages; }
    public void setAssignedPackages(List<Package> assignedPackages) {
        this.assignedPackages = assignedPackages != null ? assignedPackages : new ArrayList<>();
    }

    // JavaFX Property methods for table binding
    public StringProperty usernameProperty() {
        return new SimpleStringProperty(username);
    }

    public StringProperty fullnameProperty() {
        return new SimpleStringProperty(fullname);
    }

    public StringProperty packagesProperty() {
        return new SimpleStringProperty(assignedPackages.stream()
                .map(Package::getName)
                .collect(Collectors.joining(", ")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guide guide = (Guide) o;
        return Objects.equals(username, guide.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return fullname + " (" + username + ")";
    }
}
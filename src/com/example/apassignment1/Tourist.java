package com.example.apassignment1;

import java.util.ArrayList;
import java.util.List;

public class Tourist {
    private String username;
    private String password;
    private String fullname;
    private String role;
    private List<Package> chosenPackages;

    public Tourist() {
        this.chosenPackages = new ArrayList<>();
    }

    public Tourist(String username, String password, String fullname, String role) {
        this();
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.role = role;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Package management
    public List<Package> getChosenPackages() { return chosenPackages; }
    public void setChosenPackages(List<Package> packages) { this.chosenPackages = packages; }

    public void addChosenPackage(Package pkg) {
        if (!chosenPackages.contains(pkg)) {
            chosenPackages.add(pkg);
        }
    }

    public void removeChosenPackage(Package pkg) {
        chosenPackages.remove(pkg);
    }
}
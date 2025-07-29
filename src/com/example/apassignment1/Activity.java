package com.example.apassignment1;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Activity {
    private final String username;
    private final String activityType;
    private final String description;
    private final LocalDateTime timestamp;

    public Activity(String username, String activityType, String description) {
        this.username = username;
        this.activityType = activityType;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    // Getters only - using simple fields instead of JavaFX properties for serialization
    public String getUsername() { return username; }
    public String getActivityType() { return activityType; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
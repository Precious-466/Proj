package com.example.apassignment1;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.*;

public class ActivityLog {
    private static final String FILE_NAME = "activities.json";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    // Activity Types
    public static final String PACKAGE_CREATED = "PACKAGE_CREATED";
    public static final String PACKAGE_UPDATED = "PACKAGE_UPDATED";
    public static final String PACKAGE_DELETED = "PACKAGE_DELETED";
    public static final String PACKAGE_ASSIGNED = "PACKAGE_ASSIGNED";
    public static final String PACKAGE_UNASSIGNED = "PACKAGE_UNASSIGNED";
    public static final String TOURIST_CREATED = "TOURIST_CREATED";
    public static final String TOURIST_UPDATED = "TOURIST_UPDATED";
    public static final String TOURIST_DELETED = "TOURIST_DELETED";
    public static final String TOURIST_PACKAGE_CHOSEN = "TOURIST_PACKAGE_CHOSEN";
    public static final String TOURIST_PACKAGE_REMOVED = "TOURIST_PACKAGE_REMOVED";
    public static final String GUIDE_CREATED = "GUIDE_CREATED";
    public static final String GUIDE_UPDATED = "GUIDE_UPDATED";
    public static final String GUIDE_DELETED = "GUIDE_DELETED";

    // Core logging method
    public static void logActivity(String username, String activityType, String description) {
        List<Activity> activities = safelyLoadActivities();
        activities.add(new Activity(username, activityType, description));
        safelySaveActivities(activities);
    }

    // Package logging methods
    public static void logPackageActivity(String username, String activityType, Package pkg) {
        String description = String.format("Package: %s (ID: %s)", pkg.getName(), pkg.getId());
        logActivity(username, activityType, description);
    }

    public static void logPackageAssignment(String username, Guide guide, Package pkg, boolean assigned) {
        String activityType = assigned ? PACKAGE_ASSIGNED : PACKAGE_UNASSIGNED;
        String description = String.format("Package '%s' %s to guide '%s'",
                pkg.getName(), assigned ? "assigned" : "unassigned", guide.getFullname());
        logActivity(username, activityType, description);
    }

    // Tourist logging methods
    public static void logTouristActivity(String username, String activityType, Tourist tourist) {
        String description = String.format("Tourist: %s (Username: %s)",
                tourist.getFullname(), tourist.getUsername());
        logActivity(username, activityType, description);
    }

    public static void logTouristPackageActivity(String username, Tourist tourist, Package pkg, boolean chosen) {
        String activityType = chosen ? TOURIST_PACKAGE_CHOSEN : TOURIST_PACKAGE_REMOVED;
        String description = String.format("Tourist %s %s package '%s'",
                tourist.getFullname(),
                chosen ? "chose" : "removed",
                pkg.getName());
        logActivity(username, activityType, description);
    }

    // Guide logging methods
    public static void logGuideActivity(String username, String activityType, Guide guide) {
        String description = String.format("Guide: %s (Username: %s)",
                guide.getFullname(), guide.getUsername());
        logActivity(username, activityType, description);
    }

    // Report generation methods
    public static List<Activity> getFullActivityLog() {
        return safelyLoadActivities().stream()
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .collect(Collectors.toList());
    }

    public static List<Activity> getActivitiesBetween(LocalDate startDate, LocalDate endDate, String... types) {
        List<Activity> activities = safelyLoadActivities();

        return activities.stream()
                .filter(activity -> !activity.getTimestamp().toLocalDate().isBefore(startDate))
                .filter(activity -> !activity.getTimestamp().toLocalDate().isAfter(endDate))
                .filter(activity -> types.length == 0 || Arrays.asList(types).contains(activity.getActivityType()))
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .collect(Collectors.toList());
    }

    public static Map<String, Long> getWeeklyActivityCounts() {
        List<Activity> activities = getFullActivityLog();
        Map<String, Long> weeklyCounts = new LinkedHashMap<>();

        // Initialize days with 0 counts
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            weeklyCounts.put(day, 0L);
        }

        // Count activities per day
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
        for (Activity activity : activities) {
            try {
                String day = activity.getTimestamp().format(dayFormatter);
                weeklyCounts.put(day, weeklyCounts.get(day) + 1);
            } catch (Exception e) {
                System.err.println("Error processing activity timestamp: " + activity);
            }
        }

        return weeklyCounts;
    }

    public static Map<String, Long> getActivityCountsByType() {
        List<Activity> activities = getFullActivityLog();
        return activities.stream()
                .collect(Collectors.groupingBy(
                        Activity::getActivityType,
                        Collectors.counting()
                ));
    }

    private static List<Activity> safelyLoadActivities() {
        Path path = Paths.get(FILE_NAME);

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                return new ArrayList<>();
            } catch (IOException e) {
                System.err.println("Error creating activities file: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            if (Files.size(path) == 0) {
                return new ArrayList<>();
            }

            Type listType = new TypeToken<ArrayList<Activity>>(){}.getType();
            List<Activity> activities = gson.fromJson(reader, listType);
            return activities != null ? activities : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error loading activities: " + e.getMessage());
            handleCorruptedFile(path);
            return new ArrayList<>();
        }
    }

    private static void handleCorruptedFile(Path path) {
        try {
            Path backup = Paths.get(FILE_NAME + ".corrupted_" + System.currentTimeMillis());
            Files.move(path, backup);
            System.out.println("Created backup of corrupted file: " + backup);

            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(new ArrayList<Activity>(), writer);
            }
        } catch (IOException ex) {
            System.err.println("Error handling corrupted file: " + ex.getMessage());
        }
    }

    private static void safelySaveActivities(List<Activity> activities) {
        Path tempPath = Paths.get(FILE_NAME + ".tmp");
        Path targetPath = Paths.get(FILE_NAME);

        try (Writer writer = Files.newBufferedWriter(tempPath)) {
            gson.toJson(activities, writer);
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("Error saving activities: " + e.getMessage());
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ex) {
                System.err.println("Error deleting temp file: " + ex.getMessage());
            }
        }
    }

    public static class Activity {
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

        // Getters
        public String getUsername() { return username; }
        public String getActivityType() { return activityType; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
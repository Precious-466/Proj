package com.example.apassignment1;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class TouristStorage {
    private static final ReentrantLock fileLock = new ReentrantLock();
    private static final String FILE_PATH = "D:\\Java codes\\Proj\\users.json";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
            .create();

    static {
        initializeStorage();
    }

    private static void initializeStorage() {
        fileLock.lock();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                try {
                    Files.createDirectories(file.getParentFile().toPath());
                    saveTouristsInternal(new ArrayList<>());
                    System.out.println("Initialized new data file at: " + file.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("CRITICAL: Failed to initialize data file: " + e.getMessage());
                    throw new RuntimeException("Storage initialization failed", e);
                }
            }
        } finally {
            fileLock.unlock();
        }
    }

    public static List<Tourist> loadTourists() {
        fileLock.lock();
        try {
            File file = new File(FILE_PATH);

            if (!file.exists()) {
                System.out.println("Warning: Data file not found at " + FILE_PATH);
                return new ArrayList<>();
            }

            if (file.length() == 0) {
                System.out.println("Info: Empty data file detected");
                return new ArrayList<>();
            }

            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Tourist>>(){}.getType();
                List<Tourist> tourists = gson.fromJson(reader, listType);

                if (tourists == null) {
                    System.out.println("Warning: Null data read from file");
                    return new ArrayList<>();
                }

                System.out.println("Successfully loaded " + tourists.size() + " tourists");
                return new ArrayList<>(tourists);
            } catch (JsonSyntaxException e) {
                System.err.println("ERROR: Invalid JSON format in data file");
                backupCorruptedFile();
                return new ArrayList<>();
            } catch (Exception e) {
                System.err.println("ERROR loading tourists: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        } finally {
            fileLock.unlock();
        }
    }

    private static void backupCorruptedFile() {
        try {
            File original = new File(FILE_PATH);
            File backup = new File(FILE_PATH + ".corrupted_" + System.currentTimeMillis());
            Files.move(original.toPath(), backup.toPath());
            System.out.println("Created backup of corrupted file: " + backup.getAbsolutePath());
            saveTouristsInternal(new ArrayList<>());
        } catch (IOException e) {
            System.err.println("Failed to backup corrupted file: " + e.getMessage());
        }
    }

    private static void saveTouristsInternal(List<Tourist> tourists) throws IOException {
        File file = new File(FILE_PATH);
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(tourists, writer);
            System.out.println("Successfully saved " + tourists.size() + " tourists");
        }
    }

    public static void saveTourists(List<Tourist> tourists) {
        fileLock.lock();
        try {
            Files.createDirectories(Paths.get(FILE_PATH).getParent());
            saveTouristsInternal(new ArrayList<>(tourists));
        } catch (Exception e) {
            System.err.println("ERROR saving tourists: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save tourists", e);
        } finally {
            fileLock.unlock();
        }
    }

    public static void addTourist(Tourist tourist, String currentUser) {
        validateTourist(tourist);
        fileLock.lock();
        try {
            List<Tourist> tourists = loadTourists();

            if (tourists.stream().anyMatch(t -> t.getUsername().equalsIgnoreCase(tourist.getUsername()))) {
                throw new IllegalArgumentException("Username '" + tourist.getUsername() + "' already exists");
            }

            tourists.add(tourist);
            saveTourists(tourists);
            ActivityLog.logTouristActivity(currentUser, ActivityLog.TOURIST_CREATED, tourist);
        } finally {
            fileLock.unlock();
        }
    }

    public static void updateTourist(Tourist updatedTourist, String currentUser) {
        validateTourist(updatedTourist);
        fileLock.lock();
        try {
            List<Tourist> tourists = loadTourists();

            if (tourists.removeIf(t -> t.getUsername().equals(updatedTourist.getUsername()))) {
                tourists.add(updatedTourist);
                saveTourists(tourists);
                ActivityLog.logTouristActivity(currentUser, ActivityLog.TOURIST_UPDATED, updatedTourist);
            } else {
                throw new IllegalArgumentException("Tourist not found: " + updatedTourist.getUsername());
            }
        } finally {
            fileLock.unlock();
        }
    }

    public static void deleteTourist(String username, String currentUser) {
        Objects.requireNonNull(username, "Username cannot be null");
        fileLock.lock();
        try {
            List<Tourist> tourists = loadTourists();

            Optional<Tourist> toDelete = tourists.stream()
                    .filter(t -> t.getUsername().equals(username))
                    .findFirst();

            if (toDelete.isPresent()) {
                tourists.removeIf(t -> t.getUsername().equals(username));
                saveTourists(tourists);
                ActivityLog.logTouristActivity(currentUser, ActivityLog.TOURIST_DELETED, toDelete.get());
            } else {
                throw new IllegalArgumentException("Tourist not found: " + username);
            }
        } finally {
            fileLock.unlock();
        }
    }

    public static void addTouristPackage(Tourist tourist, Package pkg, String currentUser) {
        validateTouristAndPackage(tourist, pkg);
        fileLock.lock();
        try {
            tourist.addChosenPackage(pkg);
            updateTourist(tourist, currentUser);
            ActivityLog.logTouristPackageActivity(currentUser, tourist, pkg, true);
        } finally {
            fileLock.unlock();
        }
    }

    public static void removeTouristPackage(Tourist tourist, Package pkg, String currentUser) {
        validateTouristAndPackage(tourist, pkg);
        fileLock.lock();
        try {
            tourist.removeChosenPackage(pkg);
            updateTourist(tourist, currentUser);
            ActivityLog.logTouristPackageActivity(currentUser, tourist, pkg, false);
        } finally {
            fileLock.unlock();
        }
    }

    private static void validateTourist(Tourist tourist) {
        Objects.requireNonNull(tourist, "Tourist cannot be null");
        Objects.requireNonNull(tourist.getUsername(), "Username cannot be null");
        // Removed email validation
    }

    private static void validateTouristAndPackage(Tourist tourist, Package pkg) {
        validateTourist(tourist);
        Objects.requireNonNull(pkg, "Package cannot be null");
        Objects.requireNonNull(pkg.getId(), "Package ID cannot be null");
    }

    public static String getFilePath() {
        return FILE_PATH;
    }

    public static void debugFileState() {
        System.out.println("\n=== Tourist Storage Debug ===");
        System.out.println("Storage Path: " + FILE_PATH);

        File file = new File(FILE_PATH);
        System.out.println("File exists: " + file.exists());
        System.out.println("File size: " + file.length() + " bytes");
        System.out.println("File last modified: " + new Date(file.lastModified()));

        List<Tourist> tourists = loadTourists();
        System.out.println("Tourists loaded: " + tourists.size());
        tourists.forEach(t -> System.out.println(" - " + t.getUsername() +
                " (Packages: " + t.getChosenPackages().size() + ")"));
    }
}
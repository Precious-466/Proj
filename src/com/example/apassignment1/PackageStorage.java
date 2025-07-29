package com.example.apassignment1;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class PackageStorage {
    private static final ReentrantLock fileLock = new ReentrantLock();
    private static final String FILE_PATH = "D:\\Java codes\\Proj\\packages.json";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Package.class, new PackageAdapter())
            .setPrettyPrinting()
            .create();

    static {
        initializeStorage();
    }

    private static void initializeStorage() {
        File file = new File(FILE_PATH);
        try {
            if (!file.exists() || file.length() == 0) {
                file.getParentFile().mkdirs();
                Files.write(file.toPath(), "[]".getBytes(StandardCharsets.UTF_8));
                System.out.println("Initialized package storage at: " + FILE_PATH);
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize package storage: " + e.getMessage());
        }
    }

    public static synchronized List<Package> loadPackages() {
        File file = new File(FILE_PATH);

        // Handle empty file case
        if (file.length() == 0) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {
            // First validate JSON structure
            String content = Files.readString(file.toPath());
            if (!content.trim().startsWith("[")) {
                // Invalid format, reset to empty array
                Files.write(file.toPath(), "[]".getBytes(StandardCharsets.UTF_8));
                return new ArrayList<>();
            }

            Type listType = new TypeToken<ArrayList<Package>>(){}.getType();
            List<Package> packages = gson.fromJson(reader, listType);

            System.out.println("Successfully loaded " + (packages != null ? packages.size() : 0) + " packages");
            return packages != null ? packages : new ArrayList<>();

        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON format in packages file: " + e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error loading packages: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static synchronized void savePackages(List<Package> packages) {
        Objects.requireNonNull(packages, "Packages list cannot be null");

        File file = new File(FILE_PATH);
        try {
            // Ensure directory exists
            file.getParentFile().mkdirs();

            // Write to temporary file first
            File tempFile = new File(FILE_PATH + ".tmp");
            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(packages, writer);
            }

            // Atomic replace operation
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Successfully saved " + packages.size() + " packages");
        } catch (Exception e) {
            System.err.println("Error saving packages: " + e.getMessage());
        }
    }

    // CRUD operations
    public static void addPackage(Package pkg) {
        List<Package> packages = loadPackages();
        if (packages.stream().anyMatch(p -> p.getId().equals(pkg.getId()))) {
            throw new IllegalArgumentException("Package with ID " + pkg.getId() + " already exists");
        }
        packages.add(pkg);
        savePackages(packages);
    }

    public static void updatePackage(Package updatedPackage) {
        List<Package> packages = loadPackages();
        packages.removeIf(p -> p.getId().equals(updatedPackage.getId()));
        packages.add(updatedPackage);
        savePackages(packages);
    }

    public static void deletePackage(String packageId) {
        List<Package> packages = loadPackages();
        packages.removeIf(p -> p.getId().equals(packageId));
        savePackages(packages);
    }

    public static Package getPackageById(String packageId) {
        return loadPackages().stream()
                .filter(p -> p.getId().equals(packageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + packageId));
    }

    public static boolean packageExists(String packageId) {
        return loadPackages().stream()
                .anyMatch(p -> p.getId().equals(packageId));
    }
}
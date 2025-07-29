package com.example.apassignment1;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class GuideStorage {
    private static final ReentrantLock fileLock = new ReentrantLock();
    private static final String FILE_PATH = "D:\\Java codes\\Proj\\guides.json";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Guide.class, new GuideAdapter())
            .registerTypeAdapter(Package.class, new PackageAdapter())
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
                    Files.write(file.toPath(), "[]".getBytes(StandardCharsets.UTF_8));
                    System.out.println("Created new guides file at: " + FILE_PATH);
                } catch (IOException e) {
                    System.err.println("Failed to initialize guides file: " + e.getMessage());
                }
            }
        } finally {
            fileLock.unlock();
        }
    }

    public static List<Guide> loadGuides() {
        fileLock.lock();
        try {
            File file = new File(FILE_PATH);

            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }

            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Guide>>(){}.getType();
                List<Guide> guides = gson.fromJson(reader, listType);

                if (guides == null) {
                    return new ArrayList<>();
                }

                // Initialize assignedPackages if null
                guides.forEach(guide -> {
                    if (guide.getAssignedPackages() == null) {
                        guide.setAssignedPackages(new ArrayList<>());
                    }
                });

                return guides;
            } catch (Exception e) {
                System.err.println("Error loading guides: " + e.getMessage());
                return new ArrayList<>();
            }
        } finally {
            fileLock.unlock();
        }
    }

    public static void saveGuides(List<Guide> guides) {
        fileLock.lock();
        try {
            Files.createDirectories(Paths.get(FILE_PATH).getParent());
            try (Writer writer = Files.newBufferedWriter(Paths.get(FILE_PATH), StandardCharsets.UTF_8)) {
                gson.toJson(guides, writer);
            }
        } catch (Exception e) {
            System.err.println("Error saving guides: " + e.getMessage());
        } finally {
            fileLock.unlock();
        }
    }

    public static void addGuide(Guide guide) {
        fileLock.lock();
        try {
            List<Guide> guides = loadGuides();
            if (usernameExists(guide.getUsername())) {
                throw new IllegalArgumentException("Guide already exists: " + guide.getUsername());
            }
            guides.add(guide);
            saveGuides(guides);
        } finally {
            fileLock.unlock();
        }
    }

    public static void updateGuide(Guide updatedGuide) {
        fileLock.lock();
        try {
            List<Guide> guides = loadGuides();
            guides.removeIf(g -> g.getUsername().equals(updatedGuide.getUsername()));
            guides.add(updatedGuide);
            saveGuides(guides);
        } finally {
            fileLock.unlock();
        }
    }

    public static void deleteGuide(String username) {
        fileLock.lock();
        try {
            List<Guide> guides = loadGuides();
            guides.removeIf(g -> g.getUsername().equals(username));
            saveGuides(guides);
        } finally {
            fileLock.unlock();
        }
    }

    public static Guide getGuideByUsername(String username) {
        fileLock.lock();
        try {
            return loadGuides().stream()
                    .filter(g -> g.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);
        } finally {
            fileLock.unlock();
        }
    }

    public static boolean usernameExists(String username) {
        fileLock.lock();
        try {
            return loadGuides().stream()
                    .anyMatch(g -> g.getUsername().equals(username));
        } finally {
            fileLock.unlock();
        }
    }

    public static void assignPackageToGuide(String username, Package pkg) {
        fileLock.lock();
        try {
            Guide guide = getGuideByUsername(username);
            if (guide != null && !guide.getAssignedPackages().contains(pkg)) {
                guide.getAssignedPackages().add(pkg);
                updateGuide(guide);
            }
        } finally {
            fileLock.unlock();
        }
    }

    public static void unassignPackageFromGuide(String username, Package pkg) {
        fileLock.lock();
        try {
            Guide guide = getGuideByUsername(username);
            if (guide != null) {
                guide.getAssignedPackages().remove(pkg);
                updateGuide(guide);
            }
        } finally {
            fileLock.unlock();
        }
    }

    public static void debugPrintGuides() {
        fileLock.lock();
        try {
            List<Guide> guides = loadGuides();
            System.out.println("=== Guides (" + guides.size() + ") ===");
            guides.forEach(guide -> {
                System.out.println(guide.getFullname() + " (" + guide.getUsername() + ")");
                System.out.println("  Packages: " + guide.getAssignedPackages().size());
            });
        } finally {
            fileLock.unlock();
        }
    }
}
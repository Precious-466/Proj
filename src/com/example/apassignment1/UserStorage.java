package com.example.apassignment1;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

public class UserStorage {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    private static final Map<String, String> ROLE_FILE_MAP = Map.of(
            "admin", "admin.json",
            "guide", "guide.json",
            "tourist", "tourist.json"
    );

    // ========== ROLE-BASED LOADING/SAVING ==========

    public static List<User> loadUsers(String role) {
        String fileName = getFileNameByRole(role);
        try (Reader reader = new FileReader(fileName)) {
            Type listType = new TypeToken<ArrayList<User>>(){}.getType();
            List<User> users = gson.fromJson(reader, listType);
            return users != null ? users : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static void saveUsers(List<User> users, String role) {
        String fileName = getFileNameByRole(role);
        try (Writer writer = new FileWriter(fileName)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileNameByRole(String role) {
        return ROLE_FILE_MAP.getOrDefault(role.toLowerCase(), "users.json");
    }

    // ========== LOAD ALL USERS ACROSS ROLES ==========

    public static List<User> loadUsers() {
        List<User> allUsers = new ArrayList<>();
        for (String fileName : ROLE_FILE_MAP.values()) {
            try (Reader reader = new FileReader(fileName)) {
                Type listType = new TypeToken<ArrayList<User>>(){}.getType();
                List<User> users = gson.fromJson(reader, listType);
                if (users != null) allUsers.addAll(users);
            } catch (IOException e) {
                // ignore missing file
            }
        }
        return allUsers;
    }

    // ========== STATISTICS ==========

    public static int getTotalUserCount() {
        return loadUsers().size();
    }

    public static int getAdminCount() {
        return (int) loadUsers().stream()
                .filter(u -> u.getRole().equalsIgnoreCase(User.ROLE_ADMIN))
                .count();
    }

    public static int getGuideCount() {
        return (int) loadUsers().stream()
                .filter(u -> u.getRole().equalsIgnoreCase(User.ROLE_GUIDE))
                .count();
    }

    public static int getTouristCount() {
        return (int) loadUsers().stream()
                .filter(u -> u.getRole().equalsIgnoreCase(User.ROLE_USER))
                .count();
    }

    public static double getUserGrowthPercentage() {
        List<User> users = loadUsers();
        if (users.size() < 2) return 0;

        long currentMonthCount = users.stream()
                .filter(u -> u.getRegistrationDate().getMonth() == LocalDateTime.now().getMonth())
                .count();

        long lastMonthCount = users.stream()
                .filter(u -> u.getRegistrationDate().getMonth() == LocalDateTime.now().minusMonths(1).getMonth())
                .count();

        if (lastMonthCount == 0) return 0;
        return ((currentMonthCount - lastMonthCount) * 100.0) / lastMonthCount;
    }

    public static Map<String, Integer> getUserGrowthLast6Months() {
        Map<String, Integer> growthData = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            String monthName = month.format(DateTimeFormatter.ofPattern("MMM"));
            int count = (int) loadUsers().stream()
                    .filter(u -> u.getRegistrationDate().getMonth() == month.getMonth() &&
                            u.getRegistrationDate().getYear() == month.getYear())
                    .count();
            growthData.put(monthName, count);
        }

        return growthData;
    }

    public static Map<String, Long> getMonthlyRegistrations() {
        List<User> users = loadUsers();
        Map<String, Long> monthlyCounts = new LinkedHashMap<>();

        // Initialize all months
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String month : months) {
            monthlyCounts.put(month, 0L);
        }

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");

        for (User user : users) {
            try {
                LocalDate registrationDate = user.getRegistrationDate().toLocalDate();
                String month = registrationDate.format(monthFormatter);
                monthlyCounts.put(month, monthlyCounts.get(month) + 1);
            } catch (Exception e) {
                System.err.println("Error processing registration date for user: " + user.getUsername());
            }
        }

        return monthlyCounts;
    }

    public static Map<String, Long> getDailyActivityCounts() {
        List<User> users = loadUsers();
        Map<String, Long> dailyCounts = new LinkedHashMap<>();

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            dailyCounts.put(day, 0L);
        }

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);

        for (User user : users) {
            try {
                if (user.getLastLoginDate() != null) {
                    String day = user.getLastLoginDate().format(dayFormatter);
                    dailyCounts.put(day, dailyCounts.get(day) + 1);
                }
            } catch (Exception e) {
                System.err.println("Error processing login date for user: " + user.getUsername());
            }
        }

        return dailyCounts;
    }
}

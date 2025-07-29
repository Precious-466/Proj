package com.example.apassignment1;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BookingStorage {
    private static final String FILE_NAME = "bookings.json";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    public static List<Booking> loadBookings() {
        try (FileReader reader = new FileReader(FILE_NAME)) {
            Type listType = new TypeToken<ArrayList<Booking>>(){}.getType();
            List<Booking> bookings = gson.fromJson(reader, listType);
            return bookings != null ? bookings : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void saveBookings(List<Booking> bookings) {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            gson.toJson(bookings, writer);
        } catch (Exception e) {
            System.err.println("Error saving bookings: " + e.getMessage());
        }
    }

    public static List<Booking> getBookingsByUser(String userId) {
        return loadBookings().stream()
                .filter(b -> b.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public static void addBooking(Booking booking) {
        List<Booking> bookings = loadBookings();
        bookings.add(booking);
        saveBookings(bookings);
    }

    public static double getMonthlyRevenue() {
        LocalDate now = LocalDate.now();
        return loadBookings().stream()
                .filter(b -> b.getBookingDate().getMonth() == now.getMonth())
                .mapToDouble(Booking::getTotalPrice)
                .sum();
    }

    public static double getRevenueGrowthPercentage() {
        List<Booking> bookings = loadBookings();
        if (bookings.size() < 2) return 0;

        double currentMonthRevenue = bookings.stream()
                .filter(b -> b.getBookingDate().getMonth() == LocalDate.now().getMonth())
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        double lastMonthRevenue = bookings.stream()
                .filter(b -> b.getBookingDate().getMonth() == LocalDate.now().minusMonths(1).getMonth())
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        if (lastMonthRevenue == 0) return 0;
        return ((currentMonthRevenue - lastMonthRevenue) * 100.0) / lastMonthRevenue;
    }

    public static Map<String, Double> getRevenueLast6Months() {
        Map<String, Double> revenueData = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthName = month.format(DateTimeFormatter.ofPattern("MMM"));
            double revenue = loadBookings().stream()
                    .filter(b -> b.getBookingDate().getMonth() == month.getMonth() &&
                            b.getBookingDate().getYear() == month.getYear())
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();
            revenueData.put(monthName, revenue);
        }

        return revenueData;
    }
}
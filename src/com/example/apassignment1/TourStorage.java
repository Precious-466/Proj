package com.example.apassignment1;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

public class TourStorage {
    private static final String FILE_NAME = "tours.json";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    public static List<Tour> loadTours() {
        try (FileReader reader = new FileReader(FILE_NAME)) {
            Type listType = new TypeToken<ArrayList<Tour>>(){}.getType();
            List<Tour> tours = gson.fromJson(reader, listType);
            return tours != null ? tours : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static int getActiveTourCount() {
        return (int) loadTours().stream()
                .filter(Tour::isActive)
                .count();
    }

    public static int getToursStartingToday() {
        LocalDate today = LocalDate.now();
        return (int) loadTours().stream()
                .filter(t -> t.getStartDate().equals(today))
                .count();
    }
}
package com.example.apassignment1;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PackageAdapter implements JsonSerializer<Package>, JsonDeserializer<Package> {
    @Override
    public JsonElement serialize(Package src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        try {
            // Required fields
            obj.addProperty("id", src.getId() != null ? src.getId() : "");
            obj.addProperty("name", src.getName() != null ? src.getName() : "");

            // Optional fields
            if (src.getDescription() != null && !src.getDescription().isEmpty()) {
                obj.addProperty("description", src.getDescription());
            }
            obj.addProperty("price", src.getPrice());

            // Date handling
            if (src.getStartDate() != null) {
                obj.addProperty("startDate", src.getStartDate().toString());
            }
            if (src.getEndDate() != null) {
                obj.addProperty("endDate", src.getEndDate().toString());
            }

            if (src.getLocation() != null && !src.getLocation().isEmpty()) {
                obj.addProperty("location", src.getLocation());
            }
            obj.addProperty("isActive", src.isActive());

            return obj;
        } catch (Exception e) {
            throw new JsonParseException("Error serializing Package: " + e.getMessage(), e);
        }
    }

    @Override
    public Package deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            JsonObject obj = json.getAsJsonObject();
            Package pkg = new Package();

            // Handle required fields
            if (obj.has("id")) {
                pkg.setId(obj.get("id").getAsString());
            }
            if (obj.has("name")) {
                pkg.setName(obj.get("name").getAsString());
            }

            // Handle optional fields
            if (obj.has("description")) {
                pkg.setDescription(obj.get("description").getAsString());
            }
            if (obj.has("price")) {
                pkg.setPrice(obj.get("price").getAsDouble());
            }
            if (obj.has("location")) {
                pkg.setLocation(obj.get("location").getAsString());
            }
            if (obj.has("isActive")) {
                pkg.setActive(obj.get("isActive").getAsBoolean());
            }

            // Date handling with validation
            if (obj.has("startDate")) {
                try {
                    pkg.setStartDate(LocalDate.parse(obj.get("startDate").getAsString()));
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid start date format: " + e.getMessage());
                    pkg.setStartDate(LocalDate.now());
                }
            }

            if (obj.has("endDate")) {
                try {
                    pkg.setEndDate(LocalDate.parse(obj.get("endDate").getAsString()));
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid end date format: " + e.getMessage());
                    pkg.setEndDate(pkg.getStartDate().plusDays(7));
                }
            }

            return pkg;
        } catch (Exception e) {
            throw new JsonParseException("Error deserializing Package: " + e.getMessage(), e);
        }
    }
}
package com.example.apassignment1;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GuideAdapter implements JsonSerializer<Guide>, JsonDeserializer<Guide> {
    @Override
    public JsonElement serialize(Guide src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        try {
            // Required fields
            obj.addProperty("username", src.getUsername());
            obj.addProperty("fullname", src.getFullname());

            // Optional fields
            if (src.getPassword() != null) {
                obj.addProperty("password", src.getPassword());
            }
            if (src.getRole() != null) {
                obj.addProperty("role", src.getRole());
            }

            // Handle assigned packages - store only package IDs
            JsonArray packagesArray = new JsonArray();
            if (src.getAssignedPackages() != null) {
                for (Package pkg : src.getAssignedPackages()) {
                    if (pkg != null && pkg.getId() != null) {
                        packagesArray.add(pkg.getId());
                    }
                }
            }
            obj.add("assignedPackages", packagesArray);

            return obj;
        } catch (Exception e) {
            throw new JsonParseException("Error serializing Guide: " + e.getMessage(), e);
        }
    }

    @Override
    public Guide deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            JsonObject obj = json.getAsJsonObject();

            // Validate required fields
            if (!obj.has("username") || !obj.has("fullname")) {
                throw new JsonParseException("Missing required guide fields");
            }

            Guide guide = new Guide(
                    obj.get("username").getAsString(),
                    obj.has("password") ? obj.get("password").getAsString() : "",
                    obj.get("fullname").getAsString()
            );

            // Handle optional role field
            if (obj.has("role")) {
                guide.setRole(obj.get("role").getAsString());
            }

            // Handle assigned packages - convert IDs to Package objects
            List<Package> assignedPackages = new ArrayList<>();
            if (obj.has("assignedPackages")) {
                JsonArray packagesArray = obj.getAsJsonArray("assignedPackages");
                for (JsonElement element : packagesArray) {
                    try {
                        String packageId = element.getAsString();
                        Package pkg = PackageStorage.getPackageById(packageId);
                        if (pkg != null) {
                            assignedPackages.add(pkg);
                        }
                    } catch (Exception e) {
                        System.err.println("Warning: Failed to load package reference - " + e.getMessage());
                    }
                }
            }
            guide.setAssignedPackages(assignedPackages);

            return guide;
        } catch (Exception e) {
            throw new JsonParseException("Error deserializing Guide: " + e.getMessage(), e);
        }
    }
}
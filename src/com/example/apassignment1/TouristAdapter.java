package com.example.apassignment1;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TouristAdapter implements JsonSerializer<Tourist>, JsonDeserializer<Tourist> {
    @Override
    public JsonElement serialize(Tourist src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("username", src.getUsername());
        obj.addProperty("password", src.getPassword());
        obj.addProperty("fullname", src.getFullname());
        obj.addProperty("role", src.getRole());
        obj.add("chosenPackages", context.serialize(src.getChosenPackages()));
        return obj;
    }

    @Override
    public Tourist deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Tourist tourist = new Tourist(
                obj.get("username").getAsString(),
                obj.get("password").getAsString(),
                obj.get("fullname").getAsString(),
                obj.get("role").getAsString()
        );
        tourist.setChosenPackages(context.deserialize(obj.get("chosenPackages"),
                new TypeToken<ArrayList<Package>>(){}.getType()));
        return tourist;
    }
}
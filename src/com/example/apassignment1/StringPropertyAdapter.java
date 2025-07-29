package com.example.apassignment1;

import com.google.gson.*;
import javafx.beans.property.*;
import java.lang.reflect.Type;

public class StringPropertyAdapter implements JsonSerializer<StringProperty>, JsonDeserializer<StringProperty> {
    @Override
    public JsonElement serialize(StringProperty src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.get());
    }

    @Override
    public StringProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return new SimpleStringProperty(json.getAsString());
    }
}
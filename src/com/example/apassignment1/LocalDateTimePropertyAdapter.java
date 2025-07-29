package com.example.apassignment1;

import com.google.gson.*;
import javafx.beans.property.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class LocalDateTimePropertyAdapter implements JsonSerializer<ObjectProperty<LocalDateTime>>,
        JsonDeserializer<ObjectProperty<LocalDateTime>> {

    @Override
    public JsonElement serialize(ObjectProperty<LocalDateTime> src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.get());
    }

    @Override
    public ObjectProperty<LocalDateTime> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return new SimpleObjectProperty<>(context.deserialize(json, LocalDateTime.class));
    }
}
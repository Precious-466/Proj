package com.example.apassignment1;

import com.google.gson.*;
import javafx.beans.property.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class JavaFXPropertyAdapters {

    public static class StringPropertyAdapter implements JsonSerializer<StringProperty>,
            JsonDeserializer<StringProperty> {
        @Override
        public JsonElement serialize(StringProperty src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.getValue());
        }

        @Override
        public StringProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return json.isJsonNull() ? null : new SimpleStringProperty(json.getAsString());
        }
    }

    public static class LocalDateTimePropertyAdapter implements JsonSerializer<ObjectProperty<LocalDateTime>>,
            JsonDeserializer<ObjectProperty<LocalDateTime>> {
        @Override
        public JsonElement serialize(ObjectProperty<LocalDateTime> src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null || src.get() == null ? JsonNull.INSTANCE :
                    new JsonPrimitive(src.get().toString());
        }

        @Override
        public ObjectProperty<LocalDateTime> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return json.isJsonNull() ? null :
                    new SimpleObjectProperty<>(LocalDateTime.parse(json.getAsString()));
        }
    }
}
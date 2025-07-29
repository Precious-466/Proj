package com.example.apassignment1;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Tour {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final StringProperty location = new SimpleStringProperty();
    private final BooleanProperty isActive = new SimpleBooleanProperty(true);

    public Tour() {}

    public Tour(String id, String name, String description, double price,
                LocalDate startDate, LocalDate endDate, String location) {
        this.id.set(id);
        this.name.set(name);
        this.description.set(description);
        this.price.set(price);
        this.startDate.set(startDate);
        this.endDate.set(endDate);
        this.location.set(location);
    }

    // Getters and property methods
    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }

    public double getPrice() { return price.get(); }
    public DoubleProperty priceProperty() { return price; }

    public LocalDate getStartDate() { return startDate.get(); }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }

    public LocalDate getEndDate() { return endDate.get(); }
    public ObjectProperty<LocalDate> endDateProperty() { return endDate; }

    public String getLocation() { return location.get(); }
    public StringProperty locationProperty() { return location; }

    public boolean isActive() { return isActive.get(); }
    public BooleanProperty isActiveProperty() { return isActive; }

    public void setActive(boolean active) {
        isActive.set(active);
    }

    @Override
    public String toString() {
        return name.get() + " (" + location.get() + ") - $" + price.get();
    }
}
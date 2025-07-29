package com.example.apassignment1;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Package {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final StringProperty location = new SimpleStringProperty();
    private final BooleanProperty isActive = new SimpleBooleanProperty(true);

    public Package() {
        // Initialize with default dates to prevent nulls
        this.startDate.set(LocalDate.now());
        this.endDate.set(LocalDate.now().plusDays(7));
    }

    public Package(String id, String name, String description, double price,
                   LocalDate startDate, LocalDate endDate, String location) {
        this();
        setId(id);
        setName(name);
        setDescription(description);
        setPrice(price);
        setStartDate(startDate);
        setEndDate(endDate);
        setLocation(location);
    }

    // Copy constructor
    public Package(Package other) {
        this();
        this.id.set(other.id.get());
        this.name.set(other.name.get());
        this.description.set(other.description.get());
        this.price.set(other.price.get());
        this.startDate.set(other.startDate.get());
        this.endDate.set(other.endDate.get());
        this.location.set(other.location.get());
        this.isActive.set(other.isActive.get());
    }

    // Getters and setters
    public String getId() { return id.get(); }
    public void setId(String id) { this.id.set(Objects.requireNonNullElse(id, "")); }
    public StringProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(Objects.requireNonNullElse(name, "")); }
    public StringProperty nameProperty() { return name; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(Objects.requireNonNullElse(description, "")); }
    public StringProperty descriptionProperty() { return description; }

    public double getPrice() { return price.get(); }
    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.price.set(price);
    }
    public DoubleProperty priceProperty() { return price; }

    public LocalDate getStartDate() { return startDate.get(); }
    public void setStartDate(LocalDate startDate) {
        LocalDate newDate = Objects.requireNonNullElse(startDate, LocalDate.now());
        if (this.endDate.get() != null && newDate.isAfter(this.endDate.get())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        this.startDate.set(newDate);
    }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }

    public LocalDate getEndDate() { return endDate.get(); }
    public void setEndDate(LocalDate endDate) {
        LocalDate newDate = Objects.requireNonNullElse(endDate, getStartDate().plusDays(7));
        if (newDate.isBefore(getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        this.endDate.set(newDate);
    }
    public ObjectProperty<LocalDate> endDateProperty() { return endDate; }

    public String getLocation() { return location.get(); }
    public void setLocation(String location) { this.location.set(Objects.requireNonNullElse(location, "")); }
    public StringProperty locationProperty() { return location; }

    public boolean isActive() { return isActive.get(); }
    public void setActive(boolean active) { this.isActive.set(active); }
    public BooleanProperty isActiveProperty() { return isActive; }

    // Utility methods
    public boolean isValid() {
        return !getName().isEmpty() &&
                getStartDate() != null &&
                getEndDate() != null &&
                getEndDate().isAfter(getStartDate()) &&
                getPrice() >= 0;
    }

    public String toDisplayString() {
        return String.format("%s (%.2f) - %s", getName(), getPrice(), getLocation());
    }

    public String getDateRangeString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        String start = getStartDate() != null ? getStartDate().format(formatter) : "Not set";
        String end = getEndDate() != null ? getEndDate().format(formatter) : "Not set";
        return String.format("%s to %s", start, end);
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)",
                getName(),
                getLocation(),
                getDateRangeString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Package pkg = (Package) o;
        return Objects.equals(getId(), pkg.getId()) &&
                Objects.equals(getName(), pkg.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    // Builder pattern for fluent creation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String description = "";
        private double price;
        private LocalDate startDate = LocalDate.now();
        private LocalDate endDate = LocalDate.now().plusDays(7);
        private String location = "";
        private boolean isActive = true;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder price(double price) {
            this.price = price;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Package build() {
            Package pkg = new Package();
            pkg.setId(id);
            pkg.setName(name);
            pkg.setDescription(description);
            pkg.setPrice(price);
            pkg.setStartDate(startDate);
            pkg.setEndDate(endDate);
            pkg.setLocation(location);
            pkg.setActive(isActive);
            return pkg;
        }
    }
}
package com.example.apassignment1;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Booking {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty userId = new SimpleStringProperty();
    private final StringProperty packageId = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> bookingDate = new SimpleObjectProperty<>();
    private final IntegerProperty numberOfPeople = new SimpleIntegerProperty();
    private final DoubleProperty totalPrice = new SimpleDoubleProperty();
    private final BooleanProperty isConfirmed = new SimpleBooleanProperty(true);

    public Booking() {}

    public Booking(String id, String userId, String packageId,
                   LocalDate bookingDate, int numberOfPeople, double totalPrice) {
        this.id.set(id);
        this.userId.set(userId);
        this.packageId.set(packageId);
        this.bookingDate.set(bookingDate);
        this.numberOfPeople.set(numberOfPeople);
        this.totalPrice.set(totalPrice);
    }

    // Getters and property methods
    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getUserId() { return userId.get(); }
    public StringProperty userIdProperty() { return userId; }

    public String getPackageId() { return packageId.get(); }
    public StringProperty packageIdProperty() { return packageId; }

    public LocalDate getBookingDate() { return bookingDate.get(); }
    public ObjectProperty<LocalDate> bookingDateProperty() { return bookingDate; }

    public int getNumberOfPeople() { return numberOfPeople.get(); }
    public IntegerProperty numberOfPeopleProperty() { return numberOfPeople; }

    public double getTotalPrice() { return totalPrice.get(); }
    public DoubleProperty totalPriceProperty() { return totalPrice; }

    public boolean isConfirmed() { return isConfirmed.get(); }
    public BooleanProperty isConfirmedProperty() { return isConfirmed; }
}
package com.reservations.reservation_system.enums;

public enum ReservationStatus {
    PENDING("Pending Payment"),
    CONFIRMED("Confirmed - Visit Office Before Parking"),
    CHECKED_IN("Checked In"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");
    
    private final String description;
    
    ReservationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActive() {
        return this == CONFIRMED || this == CHECKED_IN;
    }
    
    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED;
    }
    
    public boolean requiresPayment() {
        return this == PENDING;
    }
}
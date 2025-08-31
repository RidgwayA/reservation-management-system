package com.reservations.reservation_system.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateTimeRange {
    
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    // Factory methods for common use cases
    public static DateTimeRange of(LocalDate startDate, LocalDate endDate) {
        return new DateTimeRange(startDate, endDate, null, null);
    }
    
    public static DateTimeRange of(LocalDate date, LocalTime startTime, LocalTime endTime) {
        return new DateTimeRange(date, null, startTime, endTime);
    }
    
    public static DateTimeRange of(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        return new DateTimeRange(startDate, endDate, startTime, endTime);
    }
    
    public static DateTimeRange allDay(LocalDate date) {
        return new DateTimeRange(date, null, null, null);
    }
    
    public static DateTimeRange multiDay(LocalDate startDate, LocalDate endDate) {
        return new DateTimeRange(startDate, endDate, null, null);
    }
    
    // Business logic methods
    public boolean isAllDay() {
        return startTime == null && endTime == null;
    }
    
    public boolean isMultiDay() {
        return endDate != null && !endDate.equals(startDate);
    }
    
    public boolean isSingleDay() {
        return endDate == null || endDate.equals(startDate);
    }
    
    public LocalDate getEffectiveEndDate() {
        return endDate != null ? endDate : startDate;
    }
    
    public long getDurationInDays() {
        return ChronoUnit.DAYS.between(startDate, getEffectiveEndDate()) + 1;
    }
    
    public long getDurationInMinutes() {
        if (isAllDay()) {
            return 24 * 60; // Full day in minutes
        }
        if (startTime == null || endTime == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(startTime, endTime);
    }
    
    public boolean overlapsWithDate(DateTimeRange other) {
        if (other == null) return false;
        
        LocalDate thisEnd = getEffectiveEndDate();
        LocalDate otherEnd = other.getEffectiveEndDate();
        
        return !thisEnd.isBefore(other.startDate) && !this.startDate.isAfter(otherEnd);
    }
    
    public boolean overlapsWithTime(DateTimeRange other) {
        if (other == null || isAllDay() || other.isAllDay()) {
            return false; // All-day reservations don't have time conflicts
        }
        
        if (this.startTime == null || this.endTime == null || 
            other.startTime == null || other.endTime == null) {
            return false;
        }
        
        return !this.endTime.isBefore(other.startTime) && !this.startTime.isAfter(other.endTime);
    }
    
    public boolean fullyOverlaps(DateTimeRange other) {
        return overlapsWithDate(other) && (isAllDay() || other.isAllDay() || overlapsWithTime(other));
    }
    
    public boolean contains(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();
        
        // Check date range
        if (!date.equals(startDate) && (endDate == null || date.isAfter(endDate) || date.isBefore(startDate))) {
            return false;
        }
        
        // If all day, any time on valid dates is acceptable
        if (isAllDay()) {
            return true;
        }
        
        // Check time range
        if (startTime == null || endTime == null) {
            return true; // No time restrictions
        }
        
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
    
    public boolean isValid() {
        // Check date validity
        if (endDate != null && endDate.isBefore(startDate)) {
            return false;
        }
        
        // Check time validity
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            return false;
        }
        
        return true;
    }
    
    public LocalDateTime getStartDateTime() {
        return startDate.atTime(startTime != null ? startTime : LocalTime.MIN);
    }
    
    public LocalDateTime getEndDateTime() {
        LocalDate effectiveEndDate = getEffectiveEndDate();
        LocalTime effectiveEndTime = endTime != null ? endTime : LocalTime.MAX;
        return effectiveEndDate.atTime(effectiveEndTime);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(startDate);
        
        if (endDate != null && !endDate.equals(startDate)) {
            sb.append(" - ").append(endDate);
        }
        
        if (startTime != null && endTime != null) {
            sb.append(" ").append(startTime).append(" - ").append(endTime);
        } else if (!isAllDay()) {
            sb.append(" (all day)");
        }
        
        return sb.toString();
    }
}
package com.reservations.reservation_system.dto;

import com.reservations.reservation_system.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {
    private Long id;
    private String confirmationNumber;
    private CustomerDto customer;
    private CampsiteDto campsite;
    private LocalDate startDate;
    private LocalDate endDate;
    private ReservationStatus status;
    private List<String> partyMembers;
    private Integer partySize;
    private String vehicleLicensePlate;
    private String vehicleMake;
    private String vehicleModel;
    private Integer rvLengthFeet;
    private BigDecimal campsiteTotal;
    private BigDecimal atvTotal;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String notes;
    
    public long getStayDurationInDays() {
        if (startDate == null) return 0;
        LocalDate effectiveEndDate = endDate != null ? endDate : startDate;
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, effectiveEndDate) + 1;
    }
    
    public BigDecimal getOutstandingBalance() {
        if (totalAmount == null || paidAmount == null) {
            return totalAmount != null ? totalAmount : BigDecimal.ZERO;
        }
        return totalAmount.subtract(paidAmount);
    }
    
    public boolean isPaidInFull() {
        BigDecimal outstanding = getOutstandingBalance();
        return outstanding.compareTo(BigDecimal.ZERO) <= 0;
    }
}
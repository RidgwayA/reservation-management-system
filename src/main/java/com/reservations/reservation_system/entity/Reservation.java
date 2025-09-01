package com.reservations.reservation_system.entity;

import com.reservations.reservation_system.enums.ReservationStatus;
import com.reservations.reservation_system.valueobject.DateTimeRange;
import com.reservations.reservation_system.valueobject.Money;
import com.reservations.reservation_system.valueobject.VehicleInfo;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Reservation extends BaseEntity {
    
    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @NotNull(message = "Campsite is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campsite_id", nullable = false)
    private Campsite campsite;
    
    @Valid
    @Embedded
    private DateTimeRange stayPeriod;
    
    @NotNull(message = "Reservation status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;
    
    @Valid
    @Embedded
    private VehicleInfo vehicleInfo;
    
    @ElementCollection
    @CollectionTable(name = "party_members", joinColumns = @JoinColumn(name = "reservation_id"))
    @Column(name = "member_name")
    @Size(min = 1, message = "At least one party member is required")
    private List<String> partyMembers = new ArrayList<>();
    
    @Min(value = 1, message = "Party size must be at least 1")
    @Column(name = "party_size", nullable = false)
    private Integer partySize;
    
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AtvPass> atvPasses = new ArrayList<>();
    
    @Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "campsite_total")),
        @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money campsiteTotal;
    
    @Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "atv_total")),
        @AttributeOverride(name = "currency", column = @Column(name = "atv_currency"))
    })
    private Money atvTotal;
    
    @Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "total_currency"))
    })
    private Money totalAmount;
    
    @Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "paid_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "paid_currency"))
    })
    private Money paidAmount;
    
    @Column(name = "confirmation_number", unique = true)
    private String confirmationNumber;
    
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;
    
    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;
    
    @Column(name = "notes")
    private String notes;
    
    // Business logic methods
    public boolean canCheckInEarly() {
        LocalTime standardCheckIn = LocalTime.of(13, 0); // 1 PM
        return stayPeriod.getStartDate().equals(java.time.LocalDate.now()) &&
               java.time.LocalTime.now().isBefore(standardCheckIn);
    }
    
    public boolean isLateCheckOut() {
        LocalTime standardCheckOut = LocalTime.of(11, 0); // 11 AM
        return checkOutTime != null && 
               checkOutTime.toLocalTime().isAfter(standardCheckOut);
    }
    
    public long getStayDurationInDays() {
        return stayPeriod != null ? stayPeriod.getDurationInDays() : 0;
    }
    
    public boolean isPartyFullyRegistered() {
        return partyMembers != null && 
               partySize != null && 
               partyMembers.size() == partySize;
    }
    
    public boolean exceedsMaxPartySize() {
        return partySize != null && 
               campsite != null && 
               partySize > campsite.getMaxPartySize();
    }
    
    public Money getOutstandingBalance() {
        if (totalAmount == null || paidAmount == null) {
            return totalAmount != null ? totalAmount : Money.zero();
        }
        return totalAmount.subtract(paidAmount);
    }
    
    public boolean isPaidInFull() {
        Money outstanding = getOutstandingBalance();
        return outstanding.isZero() || outstanding.isNegative();
    }
    
    public boolean requiresRvInfo() {
        return campsite != null && campsite.isRvSite();
    }
    
    public void addAtvPass(String passHolderName, Integer age) {
        if (stayPeriod != null) {
            for (long i = 0; i < getStayDurationInDays(); i++) {
                java.time.LocalDate passDate = stayPeriod.getStartDate().plusDays(i);
                AtvPass pass = AtvPass.createForReservation(this, passHolderName, age, passDate);
                atvPasses.add(pass);
            }
        }
    }
    
    public void confirm(String confirmationNumber) {
        this.status = ReservationStatus.CONFIRMED;
        this.confirmationNumber = confirmationNumber;
    }
    
    public void checkIn() {
        this.status = ReservationStatus.CHECKED_IN;
        this.checkInTime = LocalDateTime.now();
        if (campsite != null) {
            campsite.markAsOccupied();
        }
    }
    
    public void checkOut() {
        this.status = ReservationStatus.COMPLETED;
        this.checkOutTime = LocalDateTime.now();
        if (campsite != null) {
            campsite.markAsAvailable();
        }
    }
    
    public void cancel() {
        if (status.canBeCancelled()) {
            this.status = ReservationStatus.CANCELLED;
            if (campsite != null) {
                campsite.markAsAvailable();
            }
        }
    }
}
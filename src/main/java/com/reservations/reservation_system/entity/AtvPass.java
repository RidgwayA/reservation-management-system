package com.reservations.reservation_system.entity;

import com.reservations.reservation_system.valueobject.Money;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "atv_passes")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor  
@AllArgsConstructor
public class AtvPass extends BaseEntity {
    
    @NotNull(message = "Reservation is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
    
    @NotBlank(message = "Pass holder name is required")
    @Column(name = "pass_holder_name", nullable = false)
    private String passHolderName;
    
    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 120, message = "Age must be reasonable")
    @Column(name = "age", nullable = false)
    private Integer age;
    
    @NotNull(message = "Pass date is required")
    @Column(name = "pass_date", nullable = false)
    private LocalDate passDate;
    
    @Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "daily_rate")),
        @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money dailyRate;
    
    @Column(name = "issued", nullable = false)
    private Boolean issued = false;
    
    @Column(name = "wristband_number")
    private String wristbandNumber;
    
    // Factory method following SOLID principles
    public static AtvPass createForReservation(Reservation reservation, String passHolderName, Integer age, LocalDate passDate) {
        AtvPass pass = new AtvPass();
        pass.setReservation(reservation);
        pass.setPassHolderName(passHolderName);
        pass.setAge(age);
        pass.setPassDate(passDate);
        pass.setDailyRate(calculateDailyRate(age));
        return pass;
    }
    
    // Business logic methods
    public boolean isChild() {
        return age < 15;
    }
    
    public boolean isTeen() {
        return age >= 15 && age <= 17;
    }
    
    public boolean isAdult() {
        return age >= 18;
    }
    
    public boolean isFree() {
        return isChild();
    }
    
    public void issuePass(String wristbandNumber) {
        this.issued = true;
        this.wristbandNumber = wristbandNumber;
    }
    
    public boolean isIssued() {
        return issued != null && issued;
    }
    
    // Private method for pricing logic
    private static Money calculateDailyRate(Integer age) {
        if (age < 15) {
            return Money.of(BigDecimal.ZERO); // Free for children
        } else if (age >= 15 && age <= 17) {
            return Money.of(new BigDecimal("10.00")); // $10 for teens
        } else {
            return Money.of(new BigDecimal("20.00")); // $20 for adults
        }
    }
}
package com.reservations.reservation_system.valueobject;

import com.reservations.reservation_system.config.ValidationConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Money {
    
    @DecimalMin(value = ValidationConstants.DECIMAL_MIN_VALUE, message = "Amount must be non-negative")
    @Column(name = "amount", precision = ValidationConstants.DECIMAL_PRECISION, scale = ValidationConstants.DECIMAL_SCALE)
    private BigDecimal amount;
    
    @Column(name = "currency", length = 3)
    private String currency = "USD";
    
    // Factory methods
    public static Money of(BigDecimal amount) {
        return new Money(amount, "USD");
    }
    
    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP), "USD");
    }
    
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }
    
    public static Money zero() {
        return new Money(BigDecimal.ZERO, "USD");
    }
    
    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
    
    // Business logic methods
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier).setScale(2, RoundingMode.HALF_UP), this.currency);
    }
    
    public Money multiply(double multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }
    
    public Money divide(BigDecimal divisor) {
        return new Money(this.amount.divide(divisor, 2, RoundingMode.HALF_UP), this.currency);
    }
    
    public Money divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }
    
    public Money percentage(BigDecimal percentage) {
        return multiply(percentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
    }
    
    public Money percentage(double percentage) {
        return percentage(BigDecimal.valueOf(percentage));
    }
    
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }
    
    public boolean isEqualTo(Money other) {
        if (other == null) return false;
        return this.currency.equals(other.currency) && this.amount.compareTo(other.amount) == 0;
    }
    
    public Money abs() {
        return new Money(amount.abs(), currency);
    }
    
    private void validateSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot perform operation with null Money");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot perform operation with different currencies: " + 
                    this.currency + " and " + other.currency);
        }
    }
    
    public boolean isValidCurrency() {
        try {
            Currency.getInstance(currency);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s %.2f", currency, amount);
    }
    
    public String toDisplayString() {
        Currency curr = Currency.getInstance(currency);
        return String.format("%s%.2f", curr.getSymbol(), amount);
    }
}
package com.reservations.reservation_system.repository;

import com.reservations.reservation_system.entity.Reservation;
import com.reservations.reservation_system.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    Optional<Reservation> findByConfirmationNumber(String confirmationNumber);
    
    List<Reservation> findByCustomerId(Long customerId);
    
    List<Reservation> findByCampsiteId(Long campsiteId);
    
    List<Reservation> findByStatus(ReservationStatus status);
    
    @Query("SELECT r FROM Reservation r WHERE r.customer.id = :customerId AND r.active = true")
    List<Reservation> findActiveReservationsByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT r FROM Reservation r WHERE " +
           "r.stayPeriod.startDate >= :startDate AND r.stayPeriod.startDate <= :endDate " +
           "AND r.active = true")
    List<Reservation> findReservationsStartingBetween(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT r FROM Reservation r WHERE " +
           "((r.stayPeriod.startDate <= :endDate AND r.stayPeriod.endDate >= :startDate) OR " +
           " (r.stayPeriod.startDate <= :endDate AND r.stayPeriod.endDate IS NULL AND r.stayPeriod.startDate >= :startDate)) " +
           "AND r.status IN ('CONFIRMED', 'CHECKED_IN') AND r.active = true")
    List<Reservation> findActiveReservationsForDateRange(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT r FROM Reservation r WHERE " +
           "r.campsite.id = :campsiteId AND " +
           "((r.stayPeriod.startDate <= :endDate AND r.stayPeriod.endDate >= :startDate) OR " +
           " (r.stayPeriod.startDate <= :endDate AND r.stayPeriod.endDate IS NULL AND r.stayPeriod.startDate >= :startDate)) " +
           "AND r.status IN ('CONFIRMED', 'CHECKED_IN') AND r.active = true")
    List<Reservation> findConflictingReservations(
            @Param("campsiteId") Long campsiteId,
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT r FROM Reservation r WHERE r.stayPeriod.startDate = :today AND r.status = 'CONFIRMED'")
    List<Reservation> findReservationsCheckingInToday(@Param("today") LocalDate today);
    
    @Query("SELECT r FROM Reservation r WHERE r.stayPeriod.endDate = :today AND r.status = 'CHECKED_IN'")
    List<Reservation> findReservationsCheckingOutToday(@Param("today") LocalDate today);
}
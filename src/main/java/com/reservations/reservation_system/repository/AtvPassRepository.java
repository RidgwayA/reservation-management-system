package com.reservations.reservation_system.repository;

import com.reservations.reservation_system.entity.AtvPass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AtvPassRepository extends JpaRepository<AtvPass, Long> {
    
    List<AtvPass> findByReservationId(Long reservationId);
    
    List<AtvPass> findByPassDate(LocalDate passDate);
    
    List<AtvPass> findByIssuedFalse();
    
    List<AtvPass> findByIssuedTrue();
    
    @Query("SELECT ap FROM AtvPass ap WHERE ap.reservation.id = :reservationId AND ap.passDate = :passDate")
    List<AtvPass> findByReservationIdAndPassDate(
            @Param("reservationId") Long reservationId, 
            @Param("passDate") LocalDate passDate);
    
    @Query("SELECT ap FROM AtvPass ap WHERE ap.passDate = :passDate AND ap.issued = false")
    List<AtvPass> findUnissuedPassesForDate(@Param("passDate") LocalDate passDate);
    
    @Query("SELECT COUNT(ap) FROM AtvPass ap WHERE ap.passDate = :passDate")
    long countPassesForDate(@Param("passDate") LocalDate passDate);
}
package com.reservations.reservation_system.repository;

import com.reservations.reservation_system.entity.Campsite;
import com.reservations.reservation_system.enums.CampsiteStatus;
import com.reservations.reservation_system.enums.CampsiteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampsiteRepository extends JpaRepository<Campsite, Long> {
    
    Optional<Campsite> findBySiteNumber(Integer siteNumber);
    
    List<Campsite> findBySiteType(CampsiteType siteType);
    
    List<Campsite> findByStatus(CampsiteStatus status);
    
    List<Campsite> findByActiveAndStatus(Boolean active, CampsiteStatus status);
    
    @Query("SELECT c FROM Campsite c WHERE c.active = true AND c.status = 'AVAILABLE'")
    List<Campsite> findAvailableCampsites();
    
    @Query("SELECT c FROM Campsite c WHERE c.siteType = :siteType AND c.active = true AND c.status = 'AVAILABLE'")
    List<Campsite> findAvailableCampsitesByType(@Param("siteType") CampsiteType siteType);
    
    @Query("SELECT c FROM Campsite c WHERE c.id NOT IN (" +
           "SELECT r.campsite.id FROM Reservation r WHERE " +
           "r.status IN ('CONFIRMED', 'CHECKED_IN') AND " +
           "((r.stayPeriod.startDate <= :endDate AND r.stayPeriod.endDate >= :startDate) OR " +
           " (r.stayPeriod.startDate <= :endDate AND r.stayPeriod.endDate IS NULL AND r.stayPeriod.startDate >= :startDate))" +
           ") AND c.active = true AND c.status = 'AVAILABLE'")
    List<Campsite> findAvailableCampsitesForDateRange(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT c FROM Campsite c WHERE c.siteType = :siteType AND c.id NOT IN (" +
           "SELECT r.campsite.id FROM Reservation r WHERE " +
           "r.status IN ('CONFIRMED', 'CHECKED_IN') AND " +
           "((r.stayPeriod.startDate <= :endDate AND r.stayPeriod.endDate >= :startDate) OR " +
           " (r.stayPeriod.startDate <= :endDate AND r.stayPeriod.endDate IS NULL AND r.stayPeriod.startDate >= :startDate))" +
           ") AND c.active = true AND c.status = 'AVAILABLE'")
    List<Campsite> findAvailableCampsitesForDateRangeAndType(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate,
            @Param("siteType") CampsiteType siteType);
}
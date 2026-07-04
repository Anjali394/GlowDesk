package com.glowdesk.api.repository;

import com.glowdesk.api.entity.Stylist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface StylistRepository extends JpaRepository<Stylist, UUID> {

    List<Stylist> findByBranchIdAndIsActiveTrue(UUID branchId);

    @Query("""
        SELECT s FROM Stylist s
        WHERE s.branch.id = :branchId
          AND s.isActive = true
          AND s.id NOT IN (
              SELECT a.stylist.id FROM Appointment a
              WHERE a.scheduledDate = :date
                AND a.status IN ('PENDING', 'CONFIRMED')
                AND a.startTime < :endTime
                AND a.endTime > :startTime
          )
        ORDER BY s.rating DESC
        """)
    List<Stylist> findAvailableStylists(
        @Param("branchId") UUID branchId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
}
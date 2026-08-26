package com.glowdesk.api.repository;

import com.glowdesk.api.entity.Appointment;
import com.glowdesk.api.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Appointment> findByBranchIdAndStatus(UUID branchId, AppointmentStatus status);

    List<Appointment> findByStatusAndExpiresAtBefore(AppointmentStatus status, OffsetDateTime now);

    @Query("""
        SELECT a FROM Appointment a
        JOIN FETCH a.stylist
        WHERE a.branch.id = :branchId
          AND a.scheduledDate = :date
          AND a.status IN ('PENDING', 'CONFIRMED')
        """)
    List<Appointment> findBookedAppointmentsWithStylist(
            @Param("branchId") UUID branchId,
            @Param("date") LocalDate date);

    List<Appointment> findByBranchIdOrderByScheduledDateDescStartTimeAsc(UUID branchId);
}
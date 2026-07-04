package com.glowdesk.api.repository;

import com.glowdesk.api.entity.Appointment;
import com.glowdesk.api.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Appointment> findByBranchIdAndStatus(UUID branchId, AppointmentStatus status);

    List<Appointment> findByStatusAndExpiresAtBefore(AppointmentStatus status, OffsetDateTime now);
}
package com.glowdesk.api.repository;

import com.glowdesk.api.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, UUID> {

    List<Service> findByBranchIdAndIsActiveTrue(UUID branchId);
}
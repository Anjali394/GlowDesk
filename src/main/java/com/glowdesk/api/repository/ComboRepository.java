package com.glowdesk.api.repository;

import com.glowdesk.api.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ComboRepository extends JpaRepository<Combo, UUID> {

    List<Combo> findByBranchIdAndIsActiveTrue(UUID branchId);
}
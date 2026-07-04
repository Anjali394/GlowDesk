package com.glowdesk.api.repository;

import com.glowdesk.api.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    List<Branch> findByBrandIdAndIsActiveTrue(UUID brandId);
}
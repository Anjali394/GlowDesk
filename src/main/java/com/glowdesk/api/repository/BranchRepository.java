package com.glowdesk.api.repository;

import com.glowdesk.api.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    List<Branch> findByBrandIdAndIsActiveTrue(UUID brandId);

    List<Branch> findByIsActiveTrue();

    List<Branch> findByCityAndIsActiveTrue(String city);

    @Query("SELECT DISTINCT b.city FROM Branch b WHERE b.isActive = true AND b.city IS NOT NULL ORDER BY b.city")
    List<String> findDistinctActiveCities();
}
package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides inventory queries for company assets. */
public interface AssetRepository extends JpaRepository<Asset, Long> {
    /** Counts assets in a particular inventory state. */
    long countByStatus(AssetStatus status);
}

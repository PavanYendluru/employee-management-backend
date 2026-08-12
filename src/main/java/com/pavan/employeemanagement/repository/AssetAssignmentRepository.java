package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides assignment-history queries while retaining returned records. */
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    /** Finds the current assignment of an asset, if one exists. */
    Optional<AssetAssignment> findFirstByAssetAndReturnedDateIsNull(Asset asset);
/** Lists the active assets held by one employee. */
    List<AssetAssignment> findByEmployeeIdAndReturnedDateIsNull(Long employeeId);

    void deleteByEmployeeId(Long employeeId);
}

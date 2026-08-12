package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;

/** Provides leave request queries for dashboard and leave management. */
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    long countByStatus(LeaveStatus status);
    long countByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
    long countByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LeaveStatus status, LocalDate endDate, LocalDate startDate);
    List<Leave> findByEmployeeIdOrderByAppliedAtDesc(Long employeeId);
    @Query("SELECT l FROM Leave l JOIN FETCH l.employee ORDER BY l.appliedAt ASC")
    List<Leave> findAllWithEmployee();
    boolean existsByEmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId, LeaveStatus status, LocalDate endDate, LocalDate startDate);
    @Query("SELECT l FROM Leave l JOIN FETCH l.employee WHERE l.status = :status ORDER BY l.appliedAt DESC")
    List<Leave> findByStatusWithEmployee(LeaveStatus status);

    /** Returns approved leave records overlapping the given date range (used for payroll deductions). */
    @Query("SELECT l FROM Leave l WHERE l.employee.id = :employeeId AND l.status = com.pavan.employeemanagement.entity.LeaveStatus.APPROVED "
            + "AND l.startDate <= :endDate AND l.endDate >= :startDate")
    List<Leave> findApprovedOverlapping(Long employeeId, LocalDate startDate, LocalDate endDate);

    void deleteByEmployeeId(Long employeeId);
}

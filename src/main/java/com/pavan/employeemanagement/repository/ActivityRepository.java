package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.*;
import java.util.*;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.*;

/** Provides activity timeline queries for the dashboard. */
public interface ActivityRepository extends JpaRepository<Activity, Long> {
@Query("SELECT a FROM Activity a JOIN FETCH a.employee ORDER BY a.createdAt DESC")
    List<Activity> findAllWithEmployee();
    List<Activity> findTop20ByOrderByCreatedAtDesc();
    @Query("SELECT a FROM Activity a JOIN FETCH a.employee WHERE a.createdAt >= :start AND a.createdAt < :end ORDER BY a.createdAt DESC")
    List<Activity> findByCreatedAtRangeWithEmployee(LocalDateTime start, LocalDateTime end);
    void deleteByEmployeeId(Long employeeId);
}

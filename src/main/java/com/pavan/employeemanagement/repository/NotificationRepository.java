package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

/** Provides notification queries for the navbar and dashboard. */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByEmployeeIdAndIsReadFalse(Long employeeId);
    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
List<Notification> findTop10ByEmployeeIdAndIsReadFalseOrderByCreatedAtDesc(Long employeeId);

    void deleteByEmployeeId(Long employeeId);
}

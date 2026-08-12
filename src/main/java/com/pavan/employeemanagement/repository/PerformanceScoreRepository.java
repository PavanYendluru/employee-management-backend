package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.PerformanceScore;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for PerformanceScore entities. */
public interface PerformanceScoreRepository extends JpaRepository<PerformanceScore, Long> {
    List<PerformanceScore> findAllByOrderByReviewDateDesc();
    List<PerformanceScore> findByEmployeeIdOrderByReviewDateDesc(Long employeeId);
    Optional<PerformanceScore> findTopByEmployeeIdOrderByReviewDateDesc(Long employeeId);
}

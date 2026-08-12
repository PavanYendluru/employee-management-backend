package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.JobOpening;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for JobOpening entities. */
public interface JobOpeningRepository extends JpaRepository<JobOpening, Long> {
    List<JobOpening> findAllByOrderByCreatedAtDesc();
    List<JobOpening> findByActiveTrueOrderByCreatedAtDesc();
    long countByActiveTrue();
}

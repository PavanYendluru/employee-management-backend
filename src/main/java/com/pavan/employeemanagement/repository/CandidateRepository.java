package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.Candidate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for Candidate entities. */
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findAllByOrderByCreatedAtDesc();
    List<Candidate> findByJobOpeningIdOrderByCreatedAtDesc(Long jobOpeningId);
    long countByStage(String stage);
}

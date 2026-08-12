package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.*;
import com.pavan.employeemanagement.service.HrmsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Manages job openings and the candidate recruitment pipeline. */
@RestController
@RequestMapping("/api")
public class RecruitmentController {
    private final HrmsService hrmsService;

    public RecruitmentController(HrmsService hrmsService) { this.hrmsService = hrmsService; }

    // -------------------------------------------------------------------------
    // Job openings
    // -------------------------------------------------------------------------

    /** Lists all job openings. */
    @GetMapping("/job-openings")
    public List<JobOpeningView> jobOpenings() { return hrmsService.jobOpenings(); }

    /** Creates a job opening (HR). */
    @PostMapping("/admin/job-openings")
    public ResponseEntity<JobOpeningView> createOpening(@Valid @RequestBody JobOpeningRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrmsService.createJobOpening(request));
    }

    /** Updates a job opening (HR). */
    @PutMapping("/admin/job-openings/{id}")
    public JobOpeningView updateOpening(@PathVariable Long id, @Valid @RequestBody JobOpeningRequest request) {
        return hrmsService.updateJobOpening(id, request);
    }

    /** Deletes a job opening (HR). */
    @DeleteMapping("/admin/job-openings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOpening(@PathVariable Long id) { hrmsService.deleteJobOpening(id); }

    // -------------------------------------------------------------------------
    // Candidates
    // -------------------------------------------------------------------------

    /** Lists all candidates. */
    @GetMapping("/candidates")
    public List<CandidateView> candidates() { return hrmsService.candidates(); }

    /** Creates a candidate (HR). */
    @PostMapping("/admin/candidates")
    public ResponseEntity<CandidateView> createCandidate(@Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrmsService.createCandidate(request));
    }

    /** Updates a candidate (HR). */
    @PutMapping("/admin/candidates/{id}")
    public CandidateView updateCandidate(@PathVariable Long id, @Valid @RequestBody CandidateRequest request) {
        return hrmsService.updateCandidate(id, request);
    }

    /** Moves a candidate to a different pipeline stage (HR). */
    @PutMapping("/candidates/{id}/stage/{stage}")
    public CandidateView updateStage(@PathVariable Long id, @PathVariable String stage) {
        return hrmsService.updateCandidateStage(id, stage);
    }

    /** Deletes a candidate (HR). */
    @DeleteMapping("/admin/candidates/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCandidate(@PathVariable Long id) { hrmsService.deleteCandidate(id); }

    // -------------------------------------------------------------------------
    // Recruitment dashboard summary
    // -------------------------------------------------------------------------

    /** Returns aggregate pipeline counts for the recruitment dashboard. */
    @GetMapping("/recruitment/summary")
    public RecruitmentSummaryView summary() { return hrmsService.recruitmentSummary(); }
}

package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.*;
import com.pavan.employeemanagement.service.HrmsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Manages employee performance scores. */
@RestController
@RequestMapping("/api")
public class PerformanceController {
    private final HrmsService hrmsService;

    public PerformanceController(HrmsService hrmsService) { this.hrmsService = hrmsService; }

    /** Lists all performance scores (HR view). */
    @GetMapping("/performance")
    public List<PerformanceScoreView> list() { return hrmsService.performanceScores(); }

    /** Lists performance scores for a specific employee (self or HR). */
    @GetMapping("/performance/employee/{employeeId}")
    public List<PerformanceScoreView> forEmployee(@PathVariable Long employeeId, Authentication authentication) {
        return hrmsService.employeePerformance(employeeId, authentication.getName());
    }

    /** Creates a performance score (HR only). */
    @PostMapping("/admin/performance")
    public ResponseEntity<PerformanceScoreView> create(Authentication authentication, @Valid @RequestBody PerformanceScoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hrmsService.createPerformanceScore(request, authentication.getName()));
    }

    /** Updates a performance score (HR only). */
    @PutMapping("/admin/performance/{id}")
    public PerformanceScoreView update(@PathVariable Long id, Authentication authentication, @Valid @RequestBody PerformanceScoreRequest request) {
        return hrmsService.updatePerformanceScore(id, request, authentication.getName());
    }
}

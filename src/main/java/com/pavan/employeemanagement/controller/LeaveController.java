package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.*;
import com.pavan.employeemanagement.service.HrmsService;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Manages leave requests for employees. */
@RestController
@RequestMapping("/api/leaves")
public class LeaveController {
    private final HrmsService hrmsService;

    public LeaveController(HrmsService hrmsService) { this.hrmsService = hrmsService; }

    /** Gets all leave requests (HR view). */
    @GetMapping
    public ResponseEntity<List<LeaveView>> allLeaves(Authentication authentication) {
        return ResponseEntity.ok(hrmsService.allLeaves(authentication.getName()));
    }

    /** Gets leave requests for a specific employee. */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveView>> employeeLeaves(@PathVariable Long employeeId, Authentication authentication) {
        return ResponseEntity.ok(hrmsService.employeeLeaves(employeeId, authentication.getName()));
    }

    /** Submits a new leave request. */
    @PostMapping
    public ResponseEntity<LeaveView> applyLeave(Authentication authentication, @Valid @RequestBody LeaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrmsService.applyLeave(request, authentication.getName()));
    }

    /** Approves a leave request. */
    @PutMapping("/{leaveId}/approve")
    public ResponseEntity<LeaveView> approveLeave(@PathVariable Long leaveId, Authentication authentication) {
        return ResponseEntity.ok(hrmsService.approveLeave(leaveId, authentication.getName()));
    }

    /** Rejects a leave request. */
    @PutMapping("/{leaveId}/reject")
    public ResponseEntity<LeaveView> rejectLeave(@PathVariable Long leaveId, Authentication authentication) {
        return ResponseEntity.ok(hrmsService.rejectLeave(leaveId, authentication.getName()));
    }

    /** Cancels an employee's own pending leave request. */
    @DeleteMapping("/{leaveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelLeave(@PathVariable Long leaveId, Authentication authentication) {
        hrmsService.cancelLeave(leaveId, authentication.getName());
    }
}

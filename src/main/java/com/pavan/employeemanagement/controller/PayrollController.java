package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.*;
import com.pavan.employeemanagement.service.HrmsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Manages payroll records for HR and employees. */
@RestController
@RequestMapping("/api/payroll")
public class PayrollController {
    private final HrmsService hrmsService;

    public PayrollController(HrmsService hrmsService) { this.hrmsService = hrmsService; }

    /** Gets all payroll records, optionally filtered by month (HR view). */
    @GetMapping
    public ResponseEntity<List<PayrollView>> payrollRecords(
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(hrmsService.payrollRecords(month));
    }

    /** Gets payroll records for a specific employee (self-service view). */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayrollView>> employeePayroll(@PathVariable Long employeeId) {
        return ResponseEntity.ok(hrmsService.employeePayroll(employeeId));
    }

    /** Creates a new payroll record. */
    @PostMapping
    public ResponseEntity<PayrollView> createPayroll(@Valid @RequestBody PayrollRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrmsService.createPayroll(request));
    }

    /** Updates an existing payroll record. */
    @PutMapping("/{id}")
    public ResponseEntity<PayrollView> updatePayroll(@PathVariable Long id, @Valid @RequestBody PayrollRequest request) {
        return ResponseEntity.ok(hrmsService.updatePayroll(id, request));
    }

    /** Generates payroll records for all active employees for a given month. */
    @PostMapping("/generate")
    public ResponseEntity<List<PayrollView>> generatePayroll(@RequestParam String month) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrmsService.generatePayroll(month));
    }

    /** Returns payroll summary cards for the HR dashboard. */
    @GetMapping("/summary")
    public ResponseEntity<PayrollSummaryView> payrollSummary(
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(hrmsService.payrollSummary(month));
    }
}

package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.AttendanceView;
import com.pavan.employeemanagement.entity.AttendanceStatus;
import com.pavan.employeemanagement.service.HrmsService;
import java.time.LocalTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Manages attendance records for employees. */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final HrmsService hrmsService;

    public AttendanceController(HrmsService hrmsService) { this.hrmsService = hrmsService; }

    /** Records punch-in for an employee for today. */
    @PostMapping("/punch-in/{employeeId}")
    public ResponseEntity<AttendanceView> punchIn(@PathVariable Long employeeId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hrmsService.punchIn(employeeId));
    }

    /** Records punch-out for an employee for today. */
    @PostMapping("/punch-out/{employeeId}")
    public ResponseEntity<AttendanceView> punchOut(@PathVariable Long employeeId) {
        return ResponseEntity.ok(hrmsService.punchOut(employeeId));
    }

/** Returns today's attendance records for the HR attendance sheet. */
    @GetMapping("/today")
    public ResponseEntity<List<AttendanceView>> todayAttendance() {
        return ResponseEntity.ok(hrmsService.todayAttendance());
    }

    /** Returns attendance history for all employees (HR view), optionally filtered by month/year. */
    @GetMapping("/history")
    public ResponseEntity<List<AttendanceView>> attendanceHistory(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(hrmsService.attendanceHistory(month, year));
    }

    /** Returns an employee's current attendance summary for today. */
    @GetMapping("/summary/{employeeId}")
    public ResponseEntity<AttendanceView> attendanceSummary(@PathVariable Long employeeId) {
        return ResponseEntity.ok(hrmsService.attendanceSummary(employeeId));
    }

    /** Records attendance for an employee (punch in/out). */
    @PostMapping("/employee/{employeeId}")
    public ResponseEntity<AttendanceView> recordAttendance(
            @PathVariable Long employeeId,
            @RequestParam AttendanceStatus status,
            @RequestParam(required = false) LocalTime checkIn,
            @RequestParam(required = false) LocalTime checkOut) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hrmsService.recordAttendance(employeeId, status, checkIn, checkOut));
    }

    /** Gets attendance records for an employee, optionally filtered by month/year. */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceView>> employeeAttendance(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(hrmsService.employeeAttendance(employeeId, month, year));
    }
}

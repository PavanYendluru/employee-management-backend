package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.*;
import com.pavan.employeemanagement.service.HrmsService;
import java.util.List;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Returns aggregate data for dashboard cards. */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final HrmsService hrmsService;

    /** Injects the business service used to build dashboard data. */
    public DashboardController(HrmsService hrmsService) {
        this.hrmsService = hrmsService;
    }

    /** Gets the current organization overview (basic). */
    @GetMapping("/overview")
    public DashboardView overview() {
        return hrmsService.dashboard();
    }

    /**
     * Gets the enhanced HR dashboard with live attendance, leave, and payroll data.
     */
    @GetMapping("/hr")
    public ResponseEntity<DashboardViewEnhanced> hrDashboard() {
        return ResponseEntity.ok(hrmsService.dashboardEnhanced());
    }

    /** Returns chart-ready counts calculated from the current HRMS database. */
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsView> analytics(Authentication authentication) {
        return ResponseEntity.ok(hrmsService.analytics(authentication.getName()));
    }

    /** Gets employee overview data for the employee dashboard. */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<EmployeeOverviewView> employeeOverview(@PathVariable Long employeeId) {
        return ResponseEntity.ok(hrmsService.employeeOverview(employeeId));
    }

    /** Gets upcoming birthdays (within next 5 days). */
    @GetMapping("/birthdays")
    public ResponseEntity<List<UpcomingBirthdayView>> upcomingBirthdays() {
        return ResponseEntity.ok(hrmsService.upcomingBirthdays());
    }

    /** Gets recent activities for the dashboard timeline. */
    @GetMapping("/activities")
    public ResponseEntity<List<RecentActivityView>> recentActivities(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(hrmsService.recentActivities(startDate, endDate, limit));
    }
}

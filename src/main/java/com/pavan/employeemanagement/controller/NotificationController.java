package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.NotificationView;
import com.pavan.employeemanagement.service.HrmsService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Manages employee notifications. */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final HrmsService hrmsService;

    public NotificationController(HrmsService hrmsService) { this.hrmsService = hrmsService; }

    /** Gets all notifications for an employee. */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<NotificationView>> employeeNotifications(@PathVariable Long employeeId) {
        return ResponseEntity.ok(hrmsService.employeeNotifications(employeeId));
    }

    /** Gets the unread notification count for an employee. */
    @GetMapping("/employee/{employeeId}/unread-count")
    public ResponseEntity<Long> unreadCount(@PathVariable Long employeeId) {
        return ResponseEntity.ok(hrmsService.unreadNotificationCount(employeeId));
    }

    /** Marks a notification as read. */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationView> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(hrmsService.markNotificationRead(notificationId));
    }
}

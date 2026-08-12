package com.pavan.employeemanagement.entity;

/** Defines the types of activities tracked in the system. */
public enum ActivityType {
    LEAVE_APPLIED, LEAVE_APPROVED, LEAVE_REJECTED,
    PROMOTION, PERFORMANCE_REVIEW,
    EMPLOYEE_CREATED, EMPLOYEE_UPDATED, EMPLOYEE_JOINED, EMPLOYEE_RESIGNED,
    ATTENDANCE_PUNCH_IN, ATTENDANCE_PUNCH_OUT,
    ASSET_ASSIGNED, ASSET_RETURNED
}

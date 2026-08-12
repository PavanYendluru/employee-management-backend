package com.pavan.employeemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Stores system-generated notifications for employees. */
@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Employee getEmployee() { return employee; } public void setEmployee(Employee employee) { this.employee = employee; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; } public void setMessage(String message) { this.message = message; }
    public NotificationType getType() { return type; } public void setType(NotificationType type) { this.type = type; }
    public boolean isRead() { return isRead; } public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

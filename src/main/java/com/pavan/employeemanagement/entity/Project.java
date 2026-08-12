package com.pavan.employeemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Represents an organizational project. */
@Entity
@Table(name = "projects")
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, length = 2000) private String description;
    private String technology;
    private String status = "PLANNING";
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id")
    private Employee projectManager;

    public Long getId() { return id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getTechnology() { return technology; } public void setTechnology(String technology) { this.technology = technology; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Employee getProjectManager() { return projectManager; } public void setProjectManager(Employee projectManager) { this.projectManager = projectManager; }
}

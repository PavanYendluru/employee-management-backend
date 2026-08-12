package com.pavan.employeemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Represents a job opening managed by HR. */
@Entity
@Table(name = "job_openings")
public class JobOpening {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(nullable = false) private String position;
    @Column(nullable = false) private String department;
    @Column(nullable = false) private Integer vacancies;
    @Column(nullable = false) private String location;
    private String employmentType;
    private String experience;
    @Column(length = 2000) private String description;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getPosition() { return position; } public void setPosition(String position) { this.position = position; }
    public String getDepartment() { return department; } public void setDepartment(String department) { this.department = department; }
    public Integer getVacancies() { return vacancies; } public void setVacancies(Integer vacancies) { this.vacancies = vacancies; }
    public String getLocation() { return location; } public void setLocation(String location) { this.location = location; }
    public String getEmploymentType() { return employmentType; } public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public String getExperience() { return experience; } public void setExperience(String experience) { this.experience = experience; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; } public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

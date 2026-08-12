package com.pavan.employeemanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Stores performance review scores for an employee. */
@Entity
@Table(name = "performance_scores", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "review_date"}))
public class PerformanceScore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** Scores out of 100. */
    @Column(nullable = false) private BigDecimal communication = BigDecimal.ZERO;
    @Column(nullable = false) private BigDecimal attendance = BigDecimal.ZERO;
    @Column(nullable = false) private BigDecimal productivity = BigDecimal.ZERO;
    @Column(nullable = false) private BigDecimal technicalSkills = BigDecimal.ZERO;
    @Column(nullable = false) private BigDecimal leadership = BigDecimal.ZERO;
    @Column(nullable = false) private BigDecimal discipline = BigDecimal.ZERO;
    @Column(nullable = false) private BigDecimal overallRating = BigDecimal.ZERO;

    private LocalDate reviewDate = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    public Long getId() { return id; }
    public Employee getEmployee() { return employee; } public void setEmployee(Employee employee) { this.employee = employee; }
    public BigDecimal getCommunication() { return communication; } public void setCommunication(BigDecimal communication) { this.communication = communication; }
    public BigDecimal getAttendance() { return attendance; } public void setAttendance(BigDecimal attendance) { this.attendance = attendance; }
    public BigDecimal getProductivity() { return productivity; } public void setProductivity(BigDecimal productivity) { this.productivity = productivity; }
    public BigDecimal getTechnicalSkills() { return technicalSkills; } public void setTechnicalSkills(BigDecimal technicalSkills) { this.technicalSkills = technicalSkills; }
    public BigDecimal getLeadership() { return leadership; } public void setLeadership(BigDecimal leadership) { this.leadership = leadership; }
    public BigDecimal getDiscipline() { return discipline; } public void setDiscipline(BigDecimal discipline) { this.discipline = discipline; }
    public BigDecimal getOverallRating() { return overallRating; } public void setOverallRating(BigDecimal overallRating) { this.overallRating = overallRating; }
    public LocalDate getReviewDate() { return reviewDate; } public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
    public User getReviewedBy() { return reviewedBy; } public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }
}

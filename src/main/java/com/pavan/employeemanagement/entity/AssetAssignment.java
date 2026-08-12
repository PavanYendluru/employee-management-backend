package com.pavan.employeemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Preserves the complete assignment and return history for an asset. */
@Entity
@Table(name = "asset_assignments")
public class AssetAssignment {
    // Primary key and relationship fields.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Asset asset;
    @ManyToOne(optional = false) private Employee employee;
    // Dates define whether the assignment remains active.
    private LocalDate assignedDate;
    private LocalDate returnedDate;
    public Long getId() { return id; }
    public Asset getAsset() { return asset; } public void setAsset(Asset asset) { this.asset = asset; }
    public Employee getEmployee() { return employee; } public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDate getAssignedDate() { return assignedDate; } public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }
    public LocalDate getReturnedDate() { return returnedDate; } public void setReturnedDate(LocalDate returnedDate) { this.returnedDate = returnedDate; }
}

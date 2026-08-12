package com.pavan.employeemanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Represents an organizational department to which employees can belong. */
@Entity
@Table(name = "departments", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Department {
    // Database identifier and required department name.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    // Optional presentation and financial information.
    @Column(length = 1000) private String description;
    private String color;
    private BigDecimal budget;
    public Long getId() {
    	return id; 
    	}
    public String getName() {
    	return name; 
    	}
    public void setName(String name) {
    	this.name = name;
    	}
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
}

package com.pavan.employeemanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Represents a monthly payroll record for an employee. */
@Entity
@Table(name = "payroll", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "month"}))
public class Payroll {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** Payroll period in YYYY-MM format. */
    @Column(nullable = false, length = 7)
    private String month;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal basicSalary;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal hra = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal allowances = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal bonuses = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal pf = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status = PayrollStatus.PENDING;

    private LocalDate payDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public Employee getEmployee() { return employee; } public void setEmployee(Employee employee) { this.employee = employee; }
    public String getMonth() { return month; } public void setMonth(String month) { this.month = month; }
    public BigDecimal getBasicSalary() { return basicSalary; } public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public BigDecimal getHra() { return hra; } public void setHra(BigDecimal hra) { this.hra = hra; }
    public BigDecimal getAllowances() { return allowances; } public void setAllowances(BigDecimal allowances) { this.allowances = allowances; }
    public BigDecimal getBonuses() { return bonuses; } public void setBonuses(BigDecimal bonuses) { this.bonuses = bonuses; }
    public BigDecimal getDeductions() { return deductions; } public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
    public BigDecimal getPf() { return pf; } public void setPf(BigDecimal pf) { this.pf = pf; }
    public BigDecimal getTax() { return tax; } public void setTax(BigDecimal tax) { this.tax = tax; }
    public BigDecimal getNetSalary() { return netSalary; } public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }
    public PayrollStatus getStatus() { return status; } public void setStatus(PayrollStatus status) { this.status = status; }
    public LocalDate getPayDate() { return payDate; } public void setPayDate(LocalDate payDate) { this.payDate = payDate; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

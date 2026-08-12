package com.pavan.employeemanagement.repository;
import com.pavan.employeemanagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides standard persistence operations for departments. */
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}

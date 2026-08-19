package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.Employee;
import com.pavan.employeemanagement.entity.EmployeeStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeCodeIgnoreCase(String code);
    
    @Query("select e from Employee e where lower(e.firstName) like lower(concat('%', :q, '%')) or lower(e.lastName) like lower(concat('%', :q, '%')) or lower(e.email) like lower(concat('%', :q, '%')) or lower(e.employeeCode) like lower(concat('%', :q, '%')) or lower(e.jobTitle) like lower(concat('%', :q, '%')) or lower(e.location) like lower(concat('%', :q, '%'))")
    List<Employee> search(String q);
    
    long countByStatus(EmployeeStatus status);
    long countByDepartmentId(Long departmentId);

    @Query(value = "SELECT * FROM employees e WHERE DATE_FORMAT(e.date_of_birth, '%m-%d') BETWEEN DATE_FORMAT(CURDATE(), '%m-%d') AND DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :days DAY), '%m-%d')", nativeQuery = true)
    List<Employee> findUpcomingBirthdays(@Param("days") int days);
}

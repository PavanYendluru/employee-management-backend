package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.Payroll;
import com.pavan.employeemanagement.entity.PayrollStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

/** Provides payroll queries for employee payslips and HR payroll management. */
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByMonthOrderByEmployeeIdAsc(String month);
    Optional<Payroll> findByEmployeeIdAndMonth(Long employeeId, String month);
    List<Payroll> findByEmployeeIdOrderByMonthDesc(Long employeeId);
    long countByMonth(String month);
    long countByStatus(PayrollStatus status);
    @Query("SELECT p FROM Payroll p JOIN FETCH p.employee WHERE p.month = :month ORDER BY p.employee.id")
    List<Payroll> findByMonthWithEmployee(String month);
    @Query("SELECT p FROM Payroll p JOIN FETCH p.employee ORDER BY p.month DESC, p.employee.id")
    List<Payroll> findAllWithEmployee();
    void deleteByEmployeeId(Long employeeId);
}

package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;

/** Provides attendance queries for dashboard and reporting. */
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    long countByStatusAndDate(AttendanceStatus status, LocalDate date);
    long countByStatusAndDateAndEmployeeStatus(AttendanceStatus status, LocalDate date, EmployeeStatus employeeStatus);
    long countByDate(LocalDate date);
    long countByStatus(AttendanceStatus status);
    List<Attendance> findByEmployeeIdAndDateBetweenOrderByDateDesc(Long employeeId, LocalDate start, LocalDate end);
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee WHERE a.date = :date ORDER BY a.checkIn")
    List<Attendance> findByDateWithEmployee(LocalDate date);
    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee ORDER BY a.date DESC, a.checkIn")
    List<Attendance> findAllWithEmployee();
@Query("SELECT a FROM Attendance a JOIN FETCH a.employee WHERE a.date BETWEEN :start AND :end ORDER BY a.date DESC, a.checkIn")
    List<Attendance> findByDateRangeWithEmployee(LocalDate start, LocalDate end);

    void deleteByEmployeeId(Long employeeId);
}

package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides persistence operations for application login accounts. */
public interface UserRepository extends JpaRepository<User, Long> {
/** Finds an account without treating letter case in an email as significant. */
    Optional<User> findByEmailIgnoreCase(String email);

    /** Finds the login account linked to an employee when employee data changes. */
    Optional<User> findByEmployeeId(Long employeeId);

    /** Deletes all login accounts linked to an employee, used before removing the employee. */
    void deleteByEmployeeId(Long employeeId);
}

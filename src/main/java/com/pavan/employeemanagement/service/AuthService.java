package com.pavan.employeemanagement.service;

import com.pavan.employeemanagement.dto.Dtos.AuthResponse;
import com.pavan.employeemanagement.dto.Dtos.EmployeeLoginRequest;
import com.pavan.employeemanagement.dto.Dtos.LoginRequest;
import com.pavan.employeemanagement.entity.Employee;
import com.pavan.employeemanagement.entity.EmployeeStatus;
import com.pavan.employeemanagement.entity.Role;
import com.pavan.employeemanagement.entity.User;
import com.pavan.employeemanagement.repository.EmployeeRepository;
import com.pavan.employeemanagement.repository.UserRepository;
import com.pavan.employeemanagement.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// Marks this class as a Spring Service Bean
@Service
public class AuthService {

    // Repository used to access user data
    private final UserRepository users;

    // Repository used to access employee data
    private final EmployeeRepository employees;

    // Used to encrypt and verify passwords
    private final PasswordEncoder encoder;

    // Service used to generate JWT tokens
    private final JwtService jwt;

    // Service used to fetch HRMS user details
    private final HrmsService hrms;

    // Constructor Injection
    public AuthService(
            UserRepository users,
            EmployeeRepository employees,
            PasswordEncoder encoder,
            JwtService jwt,
            HrmsService hrms) {

        this.users = users;
        this.employees = employees;
        this.encoder = encoder;
        this.jwt = jwt;
        this.hrms = hrms;
    }

    // Handles HR/Admin login
    public AuthResponse login(LoginRequest request) {

        // Find user by email
        User user = users.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        // Check if the account is enabled and password is correct
        if (!user.isEnabled() ||
                !encoder.matches(request.password(), user.getPasswordHash())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        // Prevent employees from logging into the Admin portal
        if (user.getRole() == Role.EMPLOYEE) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Use the employee portal"
            );
        }

        // Generate authentication response
        return response(user);
    }

    // Handles Employee login
    public AuthResponse employeeLogin(EmployeeLoginRequest request) {

        // Find employee using employee ID
        Employee employee = employees
                .findByEmployeeCodeIgnoreCase(request.employeeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid employee ID or password"
                ));

        // Find the user account linked to the employee
        User user = users.findAll()
                .stream()
                .filter(u ->
                        u.getEmployee() != null &&
                        u.getEmployee().getId().equals(employee.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Employee account has not been activated"
                ));

        // Check whether employee is active and password is correct
        if (employee.getStatus() != EmployeeStatus.ACTIVE ||
                !encoder.matches(request.password(), user.getPasswordHash())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid employee ID or password"
            );
        }

        // Generate authentication response
        return response(user);
    }

    // Creates the authentication response after successful login
    private AuthResponse response(User user) {

        // Generate JWT token
        String token = jwt.create(user);

        // Return authentication details
        return new AuthResponse(
                token,
                "Bearer",
                user.getRole().name(),
                hrms.userView(user)
        );
    }
}
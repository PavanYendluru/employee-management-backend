package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.AuthResponse;
import com.pavan.employeemanagement.dto.Dtos.EmployeeLoginRequest;
import com.pavan.employeemanagement.dto.Dtos.LoginRequest;
import com.pavan.employeemanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** Exposes the two login portals used by the HRMS frontend. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /** Injects the service that validates credentials and creates JWTs. */
    public AuthController(AuthService authService) { this.authService = authService; }

    /** Authenticates an HR, manager, or administrator account. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }

    /** Authenticates an employee with their generated employee ID. */
    @PostMapping("/employee/login")
    public AuthResponse employeeLogin(@Valid @RequestBody EmployeeLoginRequest request) { return authService.employeeLogin(request); }
}

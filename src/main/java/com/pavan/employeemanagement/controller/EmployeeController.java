package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.*;
import com.pavan.employeemanagement.service.HrmsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Provides the employee directory and self-service profile endpoints. */
@RestController
@RequestMapping("/api")
public class EmployeeController {
    private final HrmsService hrmsService;

    /** Injects the employee business service. */
    public EmployeeController(HrmsService hrmsService) { this.hrmsService = hrmsService; }

    /** Lists employees and optionally filters by a search term. */
    @GetMapping("/employees") public List<EmployeeView> list(@RequestParam(required = false) String search) { return hrmsService.employees(search); }
    /** Gets one employee by its numeric database identifier. */
    @GetMapping("/employees/{id}") public EmployeeView get(@PathVariable Long id) { return hrmsService.employee(id); }
    /** Creates an employee record. */
    @PostMapping("/admin/employees") public ResponseEntity<EmployeeView> create(@Valid @RequestBody EmployeeRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(hrmsService.createEmployee(request)); }
    /** Updates an employee record. */
    @PutMapping("/admin/employees/{id}") public EmployeeView update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) { return hrmsService.updateEmployee(id, request); }
    /** Deletes an employee record. */
    @DeleteMapping("/admin/employees/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { hrmsService.deleteEmployee(id); }
    /** Returns the signed-in account's public information. */
    @GetMapping("/me") public UserView me(Authentication authentication) { return hrmsService.userView(hrmsService.user(authentication.getName())); }
    /** Allows an employee to update only their permitted personal fields. */
    @PatchMapping("/me/profile") public EmployeeView profile(Authentication authentication, @Valid @RequestBody PersonalProfileRequest request) { return hrmsService.personalProfile(authentication.getName(), request); }
    /** Gets the authenticated HR/Admin profile; no user id is accepted to prevent cross-account updates. */
    @GetMapping("/me/hr-profile") public HrProfileView hrProfile(Authentication authentication) { return hrmsService.hrProfile(authentication.getName()); }
    @PatchMapping("/me/hr-profile") public HrProfileView updateHrProfile(Authentication authentication, @Valid @RequestBody HrProfileRequest request) { return hrmsService.updateHrProfile(authentication.getName(), request); }
}

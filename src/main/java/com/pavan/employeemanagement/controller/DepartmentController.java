package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.DepartmentRequest;
import com.pavan.employeemanagement.dto.Dtos.DepartmentView;
import com.pavan.employeemanagement.service.HrmsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Manages departments used by employee records. */
@RestController
@RequestMapping("/api")
public class DepartmentController {
    private final HrmsService hrmsService;
    /** Injects the department business service. */
    public DepartmentController(HrmsService hrmsService) { this.hrmsService = hrmsService; }
    /** Lists all departments for dropdowns and administration. */
    @GetMapping("/departments") public List<DepartmentView> list() { return hrmsService.departments(); }
    /** Creates a department; security restricts this URL to HR/Admin. */
    @PostMapping("/admin/departments") public DepartmentView create(@Valid @RequestBody DepartmentRequest request) { return hrmsService.createDepartment(request); }
    /** Updates a department by database ID. */
    @PutMapping("/admin/departments/{id}") public DepartmentView update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) { return hrmsService.updateDepartment(id, request); }
    /** Deletes an unused department. */
    @DeleteMapping("/admin/departments/{id}") public void delete(@PathVariable Long id) { hrmsService.deleteDepartment(id); }
}

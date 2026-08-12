package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.*;
import com.pavan.employeemanagement.service.HrmsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Manages organizational projects. */
@RestController
@RequestMapping("/api")
public class ProjectController {
    private final HrmsService hrmsService;

    public ProjectController(HrmsService hrmsService) { this.hrmsService = hrmsService; }

    /** Lists all projects (HR view). */
    @GetMapping("/projects")
    public List<ProjectView> list() { return hrmsService.projects(); }

    /** Lists projects managed by the signed-in employee. */
    @GetMapping("/me/projects")
    public List<ProjectView> myProjects(Authentication authentication) {
        return hrmsService.employeeProjects(authentication.getName());
    }

    /** Creates a project. */
    @PostMapping("/admin/projects")
    public ResponseEntity<ProjectView> create(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrmsService.createProject(request));
    }

    /** Updates a project. */
    @PutMapping("/admin/projects/{id}")
    public ProjectView update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return hrmsService.updateProject(id, request);
    }

    /** Deletes a project. */
    @DeleteMapping("/admin/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { hrmsService.deleteProject(id); }
}

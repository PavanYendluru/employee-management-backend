package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.*;
import com.pavan.employeemanagement.entity.TaskStatus;
import com.pavan.employeemanagement.service.HrmsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Manages the task workflow (To Do → In Progress → Review → Done). */
@RestController
@RequestMapping("/api")
public class TaskController {
    private final HrmsService hrmsService;

    public TaskController(HrmsService hrmsService) { this.hrmsService = hrmsService; }

    /** Lists all tasks (HR view). */
    @GetMapping("/tasks")
    public List<TaskView> list(Authentication authentication) { return hrmsService.tasks(authentication.getName()); }

    /** Lists tasks assigned to the signed-in employee. */
    @GetMapping("/me/tasks")
    public List<TaskView> myTasks(Authentication authentication) {
        return hrmsService.myTasks(authentication.getName());
    }

    /** Filters tasks by workflow status. */
    @GetMapping("/tasks/status/{status}")
    public List<TaskView> byStatus(@PathVariable TaskStatus status, Authentication authentication) {
        return hrmsService.tasksByStatus(status, authentication.getName());
    }

    /** Creates a new task (HR). */
    @PostMapping("/admin/tasks")
    public ResponseEntity<TaskView> create(Authentication authentication, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hrmsService.createTask(request, authentication.getName()));
    }

    /** Updates task details (HR). */
    @PutMapping("/admin/tasks/{id}")
    public TaskView update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return hrmsService.updateTask(id, request);
    }

    /** Transitions a task to a new workflow status. */
    @PutMapping("/tasks/{id}/status/{status}")
    public TaskView transition(@PathVariable Long id, @PathVariable TaskStatus status, Authentication authentication) {
        return hrmsService.transitionTask(id, status, authentication.getName());
    }

    /** Lets the assigned employee/team leader accept a To Do task before HR starts it. */
    @PostMapping("/tasks/{id}/accept")
    public TaskView accept(@PathVariable Long id, Authentication authentication) {
        return hrmsService.acceptTask(id, authentication.getName());
    }

    /** Deletes a task (HR). */
    @DeleteMapping("/admin/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { hrmsService.deleteTask(id); }
}

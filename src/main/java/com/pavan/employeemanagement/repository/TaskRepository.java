package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.Task;
import com.pavan.employeemanagement.entity.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for Task entities. */
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOrderByCreatedAtDesc();
    List<Task> findByAssignedToIdOrderByCreatedAtDesc(Long employeeId);
    List<Task> findByStatusOrderByCreatedAtDesc(TaskStatus status);
    long countByAssignedToId(Long employeeId);
}

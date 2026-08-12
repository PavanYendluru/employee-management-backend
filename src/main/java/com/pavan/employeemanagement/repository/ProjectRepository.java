package com.pavan.employeemanagement.repository;

import com.pavan.employeemanagement.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for Project entities. */
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByOrderByNameAsc();
}

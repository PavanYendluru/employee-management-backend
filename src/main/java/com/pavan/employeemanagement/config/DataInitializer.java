package com.pavan.employeemanagement.config;

import com.pavan.employeemanagement.entity.*;
import com.pavan.employeemanagement.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Seeds minimum development records and Orchasp demo content only when absent. */
@Configuration
public class DataInitializer {
    /** Creates the HR admin, departments, portfolio projects, and job openings. */
    @Bean
    CommandLineRunner initialData(
            UserRepository users,
            DepartmentRepository departments,
            ProjectRepository projects,
            JobOpeningRepository openings,
            PasswordEncoder passwords) {
        return args -> {
            // Seed the specified administrator exactly once, using a BCrypt hash.
            if (users.findByEmailIgnoreCase("hr@orchasp.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("hr@orchasp.com");
                admin.setPasswordHash(passwords.encode("orchaspadmin@32login"));
                admin.setRole(Role.ADMIN);
                users.save(admin);
            }

            // Seed starter departments for a new HRMS installation.
            if (departments.count() == 0) {
                for (String name : new String[]{"Human Resources", "Engineering", "Finance", "Sales", "Marketing"}) {
                    Department department = new Department();
                    department.setName(name);
                    departments.save(department);
                }
            }

            // Seed the Orchasp company portfolio projects.
            if (projects.count() == 0) {
                List<Object[]> portfolio = List.of(
                        new Object[]{"IndusCare", "Healthcare management suite for clinics and hospitals.", "Java, Spring Boot, React", "ACTIVE"},
                        new Object[]{"IndusAyush", "Ayurveda and wellness practice management platform.", "Java, Spring Boot, Angular", "ACTIVE"},
                        new Object[]{"IndusRetail", "Retail POS and inventory management system.", "Java, Spring Boot, React", "PLANNING"},
                        new Object[]{"IndusCargo", "Logistics and cargo tracking platform.", "Java, Spring Boot, Vue", "ACTIVE"},
                        new Object[]{"IndusNetworkX", "Network infrastructure monitoring dashboard.", "Java, Spring Boot, React", "PLANNING"},
                        new Object[]{"IndusCrafts", "Handicraft e-commerce and artisan marketplace.", "Java, Spring Boot, React", "ON-HOLD"}
                );
                LocalDate now = LocalDate.now();
                for (int i = 0; i < portfolio.size(); i++) {
                    Object[] row = portfolio.get(i);
                    Project project = new Project();
                    project.setName((String) row[0]);
                    project.setDescription((String) row[1]);
                    project.setTechnology((String) row[2]);
                    project.setStatus((String) row[3]);
                    project.setStartDate(now.plusMonths(i - 2));
                    project.setEndDate(now.plusMonths(i + 8));
                    projects.save(project);
                }
            }

            // Seed a demo job opening.
            if (openings.count() == 0) {
                JobOpening opening = new JobOpening();
                opening.setPosition("Senior Java Developer");
                opening.setDepartment("Engineering");
                opening.setVacancies(3);
                opening.setLocation("Hyderabad, IN");
                opening.setEmploymentType("Full-time");
                opening.setExperience("5-8 years");
                opening.setDescription("Build enterprise Java/Spring Boot services for Orchasp products.");
                opening.setActive(true);
                opening.setCreatedAt(LocalDateTime.now());
                openings.save(opening);
            }
        };
    }
}

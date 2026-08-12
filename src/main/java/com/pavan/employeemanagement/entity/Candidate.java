package com.pavan.employeemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Represents a candidate in the recruitment pipeline. */
@Entity
@Table(name = "candidates")
public class Candidate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(nullable = false) private String name;
    @Column(nullable = false) private String role;
    @Column(nullable = false) private String stage = "SOURCED";
    private Integer rating;
    private String phone;
    private String email;
    private String resumeUrl;
    private String source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opening_id")
    private JobOpening jobOpening;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getRole() { return role; } public void setRole(String role) { this.role = role; }
    public String getStage() { return stage; } public void setStage(String stage) { this.stage = stage; }
    public Integer getRating() { return rating; } public void setRating(Integer rating) { this.rating = rating; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getResumeUrl() { return resumeUrl; } public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }
    public String getSource() { return source; } public void setSource(String source) { this.source = source; }
    public JobOpening getJobOpening() { return jobOpening; } public void setJobOpening(JobOpening jobOpening) { this.jobOpening = jobOpening; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

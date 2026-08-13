package com.pavan.employeemanagement.dto;

// Import all entity classes (EmployeeStatus, AssetStatus, etc.)
import com.pavan.employeemanagement.entity.*;

// Import validation annotations
import jakarta.validation.constraints.*;

// Import required Java classes
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/*
 * ============================================================
 * Dtos Class
 * ============================================================
 * This class acts as a container for all DTO (Data Transfer Object)
 * records used in the application.
 *
 * DTOs are used to transfer data between:
 * Client  <---->  Controller  <---->  Service
 *
 * They help prevent exposing database entities directly.
 * ============================================================
 */
public final class Dtos {

    /*
     * Private constructor prevents object creation.
     * Since this class only contains static record definitions,
     * there is no need to create an object of this class.
     */
    private Dtos() {
    }

    // ============================================================
    // LOGIN REQUEST DTO
    // ============================================================
    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    // ============================================================
    // EMPLOYEE LOGIN REQUEST DTO
    // ============================================================
    public record EmployeeLoginRequest(
            @NotBlank String employeeId,
            @NotBlank String password
    ) {
    }

    // ============================================================
    // AUTH RESPONSE DTO
    // ============================================================
    public record AuthResponse(
            String token,
            String tokenType,
            String role,
            UserView user
    ) {
    }

    // ============================================================
    // USER VIEW DTO
    // ============================================================
    public record UserView(
            Long id,
            String email,
            String role,
            Long employeeId,
            String employeeCode,
            String name
    ) {
    }

    // ============================================================
    // EMPLOYEE REQUEST DTO
    // ============================================================
    public record EmployeeRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Email String email,
            @NotBlank @Pattern(regexp = "^[+]?[0-9][0-9() .-]{6,19}$", message = "Phone number is invalid") String phone,
            @NotBlank String jobTitle,
            Long departmentId,
            @NotBlank String location,
            @NotNull @PositiveOrZero BigDecimal salary,
            @NotNull LocalDate hireDate,
            @NotNull @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,
            String employmentType,
            EmployeeStatus status,
            String profilePicture,
            String address,
            String emergencyContact,
            @Size(min = 8, message = "Temporary password must contain at least 8 characters") String initialPassword
    ) {
    }

    // ============================================================
    // EMPLOYEE VIEW DTO
    // ============================================================
    public record EmployeeView(
            Long id,
            String employeeId,
            String firstName,
            String lastName,
            String email,
            String phone,
            String jobTitle,
            Long departmentId,
            String departmentName,
            String location,
            BigDecimal salary,
            LocalDate hireDate,
            LocalDate dateOfBirth,
            String employmentType,
            EmployeeStatus status,
            String profilePicture,
            String address,
            String emergencyContact
    ) {
    }

    // ============================================================
    // DEPARTMENT REQUEST DTO
    // ============================================================
    public record DepartmentRequest(
            @NotBlank String name,
            String description,
            String color,
            BigDecimal budget
    ) {
    }

    // ============================================================
    // DEPARTMENT VIEW DTO
    // ============================================================
    public record DepartmentView(
            Long id,
            String name,
            String description,
            String color,
            BigDecimal budget
    ) {
    }

    // ============================================================
    // ASSET REQUEST DTO
    // ============================================================
    public record AssetRequest(
            @NotBlank String name,
            @NotBlank String category,
            @NotBlank String serial,
            @NotNull @PositiveOrZero BigDecimal value,
            AssetStatus status
    ) {
    }

    // ============================================================
    // ASSET VIEW DTO
    // ============================================================
    public record AssetView(
            Long id,
            String name,
            String category,
            String serial,
            BigDecimal value,
            AssetStatus status,
            Long assignedToId,
            String assignedDate
    ) {
    }

    // ============================================================
    // ASSIGN ASSET REQUEST DTO
    // ============================================================
    public record AssignAssetRequest(
            @NotNull Long employeeId
    ) {
    }

    // ============================================================
    // PERSONAL PROFILE REQUEST DTO
    // ============================================================
    public record PersonalProfileRequest(
            @NotBlank String phone,
            String address,
            String emergencyContact,
            String profilePicture
    ) {
    }

    // ============================================================
    // DASHBOARD VIEW DTO
    // ============================================================
    public record DashboardView(
            long totalEmployees,
            long activeEmployees,
            long onLeave,
            long pendingLeaves,
            long totalDepartments,
            long totalAssets,
            long assignedAssets,
            BigDecimal monthlyPayroll,
            long activeProjects
    ) {
    }

    // ============================================================
    // ENHANCED DASHBOARD VIEW DTO
    // ============================================================
    public record DashboardViewEnhanced(
            long totalEmployees,
            long activeEmployees,
            long presentEmployees,
            long absentEmployees,
            long onLeave,
            long pendingLeaves,
            long approvedLeaves,
            long rejectedLeaves,
            long totalDepartments,
            long totalAssets,
            long assignedAssets,
            BigDecimal monthlyPayroll,
            long activeProjects,
            long openPositions
    ) {
    }

    // ============================================================
    // UPCOMING BIRTHDAY VIEW DTO
    // ============================================================
    public record UpcomingBirthdayView(
            Long id,
            String firstName,
            String lastName,
            String profilePicture,
            String departmentName,
            String jobTitle,
            LocalDate dateOfBirth,
            long daysRemaining
    ) {
    }

    // ============================================================
    // RECENT ACTIVITY VIEW DTO
    // ============================================================
    public record RecentActivityView(
            Long id,
            Long employeeId,
            String employeeName,
            String employeePhoto,
            String activityType,
            String description,
            String createdAt
    ) {
    }

    // ============================================================
    // NOTIFICATION VIEW DTO
    // ============================================================
    public record NotificationView(
            Long id,
            String title,
            String message,
            String type,
            boolean isRead,
            String createdAt
    ) {
    }

    // ============================================================
    // LEAVE REQUEST DTO
    // ============================================================
    public record LeaveRequest(
            @NotNull Long employeeId,
            @NotBlank String leaveType,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @NotBlank @Size(min = 5, max = 1000) String reason
    ) {
    }

    // ============================================================
    // LEAVE VIEW DTO
    // ============================================================
    public record LeaveView(
            Long id,
            Long employeeId,
            String employeeName,
            String employeePhoto,
            String leaveType,
            LocalDate startDate,
            LocalDate endDate,
            String reason,
            String status,
            String appliedAt,
            String reviewedAt
    ) {
    }

    // ============================================================
    // ATTENDANCE VIEW DTO
    // ============================================================
    public record AttendanceView(
            Long id,
            Long employeeId,
            String employeeCode,
            String employeeName,
            String departmentName,
            String date,
            String checkIn,
            String checkOut,
            String workingHours,
            String status
    ) {
    }

// ============================================================
    // PAYROLL REQUEST DTO
    // ============================================================
    public record PayrollRequest(
            @NotNull Long employeeId,
            @NotBlank String month,
            @NotNull @PositiveOrZero BigDecimal basicSalary,
            @NotNull @PositiveOrZero BigDecimal hra,
            @NotNull @PositiveOrZero BigDecimal allowances,
            @NotNull @PositiveOrZero BigDecimal bonuses,
            @NotNull @PositiveOrZero BigDecimal deductions,
            @NotNull @PositiveOrZero BigDecimal pf,
            @NotNull @PositiveOrZero BigDecimal tax,
            PayrollStatus status
    ) {
    }

    // ============================================================
    // PAYROLL VIEW DTO
    // ============================================================
    public record PayrollView(
            Long id,
            Long employeeId,
            String employeeCode,
            String employeeName,
            String departmentName,
            String month,
            BigDecimal basicSalary,
            BigDecimal hra,
            BigDecimal allowances,
            BigDecimal bonuses,
            BigDecimal deductions,
            BigDecimal pf,
            BigDecimal tax,
            BigDecimal netSalary,
            String status,
            String payDate
    ) {
    }

    // ============================================================
    // PAYROLL SUMMARY VIEW DTO
    // ============================================================
    public record PayrollSummaryView(
            BigDecimal totalPayroll,
            long employeeCount,
            String month
    ) {
    }

    // ============================================================
    // EMPLOYEE OVERVIEW DTO
    // ============================================================
    public record EmployeeOverviewView(
            long totalLeaves,
            long pendingLeaves,
            long approvedLeaves,
            long rejectedLeaves,
            long attendanceRecordsCount,
            long presentDays,
            long absentDays,
            long lateDays,
            long remoteDays,
            long totalAssets,
            long assignedAssets,
            long totalHikes,
            BigDecimal totalPayroll,
            long totalJourneys
    ) {
    }

    // ============================================================
    // MESSAGE RESPONSE DTO
    // ============================================================
    public record MessageResponse(
            String message
    ) {
    }

    /** Result returned by a CSV import. Failed rows never prevent valid rows from being saved. */
    public record BulkImportResult(
            String module,
            int totalRows,
            int importedRows,
            List<String> errors
    ) { }

    // ============================================================
    // PROJECT REQUEST DTO
    // ============================================================
    public record ProjectRequest(
            @NotBlank String name,
            @NotBlank String description,
            String technology,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            Long projectManagerId
    ) {
    }

    // ============================================================
    // PROJECT VIEW DTO
    // ============================================================
    public record ProjectView(
            Long id,
            String name,
            String description,
            String technology,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            Long projectManagerId,
            String projectManagerName,
            long assignedEmployees
    ) {
    }

    // ============================================================
    // TASK REQUEST DTO
    // ============================================================
    public record TaskRequest(
            @NotBlank String title,
            String description,
            @NotBlank String priority,
            @NotNull LocalDate dueDate,
            @NotNull Long assignedToId
    ) {
    }

    // ============================================================
    // TASK VIEW DTO
    // ============================================================
    public record TaskView(
            Long id,
            String title,
            String description,
            String priority,
            String status,
            LocalDate dueDate,
            Long assignedToId,
            String assignedToName,
            String assignedToPhoto,
            Long createdById,
            String createdByName,
            String createdAt,
            String updatedAt,
            String acceptedAt
    ) {
    }

    // ============================================================
    // PERFORMANCE SCORE REQUEST DTO
    // ============================================================
    public record PerformanceScoreRequest(
            @NotNull Long employeeId,
            @NotNull @Min(0) @Max(100) BigDecimal communication,
            @NotNull @Min(0) @Max(100) BigDecimal attendance,
            @NotNull @Min(0) @Max(100) BigDecimal productivity,
            @NotNull @Min(0) @Max(100) BigDecimal technicalSkills,
            @NotNull @Min(0) @Max(100) BigDecimal leadership,
            @NotNull @Min(0) @Max(100) BigDecimal discipline,
            @NotNull @Min(0) @Max(100) BigDecimal overallRating
    ) {
    }

    // ============================================================
    // PERFORMANCE SCORE VIEW DTO
    // ============================================================
    public record PerformanceScoreView(
            Long id,
            Long employeeId,
            String employeeName,
            String employeePhoto,
            BigDecimal communication,
            BigDecimal attendance,
            BigDecimal productivity,
            BigDecimal technicalSkills,
            BigDecimal leadership,
            BigDecimal discipline,
            BigDecimal overallRating,
            String reviewDate
    ) {
    }

    // ============================================================
    // JOB OPENING REQUEST DTO
    // ============================================================
    public record JobOpeningRequest(
            @NotBlank String position,
            @NotBlank String department,
            @NotNull @Positive Integer vacancies,
            @NotBlank String location,
            String employmentType,
            String experience,
            String description
    ) {
    }

    // ============================================================
    // JOB OPENING VIEW DTO
    // ============================================================
    public record JobOpeningView(
            Long id,
            String position,
            String department,
            Integer vacancies,
            String location,
            String employmentType,
            String experience,
            String description,
            boolean active,
            String createdAt
    ) {
    }

    // ============================================================
    // CANDIDATE REQUEST DTO
    // ============================================================
    public record CandidateRequest(
            @NotBlank String name,
            @NotBlank String role,
            String stage,
            @Min(0) @Max(100) Integer rating,
            String phone,
            @Email String email,
            String resumeUrl,
            String source,
            Long jobOpeningId
    ) {
    }

    // ============================================================
    // CANDIDATE VIEW DTO
    // ============================================================
    public record CandidateView(
            Long id,
            String name,
            String role,
            String stage,
            Integer rating,
            String phone,
            String email,
            String resumeUrl,
            String source,
            Long jobOpeningId,
            String jobOpeningPosition,
            String createdAt
    ) {
    }

    // ============================================================
    // RECRUITMENT SUMMARY DTO
    // ============================================================
    public record RecruitmentSummaryView(
            long totalCandidates,
            long interviewScheduled,
            long offers,
            long hired,
            long rejected,
            long openPositions
    ) {
    }

    public record HrProfileRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @NotBlank String phone,
            @NotBlank String department,
            @NotBlank String designation
    ) { }

    public record HrProfileView(
            Long id, String name, String email, String phone, String department, String designation, String role
    ) { }
}

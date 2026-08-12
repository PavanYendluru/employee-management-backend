package com.pavan.employeemanagement.service;

import com.pavan.employeemanagement.dto.Dtos.*;
import com.pavan.employeemanagement.entity.*;
import com.pavan.employeemanagement.repository.*;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class HrmsService {

    private static final Logger log = LoggerFactory.getLogger(HrmsService.class);

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AssetRepository assetRepository;
    private final AssetAssignmentRepository assetAssignmentRepository;
    private final UserRepository userRepository;
    private final LeaveRepository leaveRepository;
private final AttendanceRepository attendanceRepository;
    private final ActivityRepository activityRepository;
    private final NotificationRepository notificationRepository;
    private final PayrollRepository payrollRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PerformanceScoreRepository performanceScoreRepository;
    private final JobOpeningRepository jobOpeningRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;

    public HrmsService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            AssetRepository assetRepository,
            AssetAssignmentRepository assetAssignmentRepository,
            UserRepository userRepository,
            LeaveRepository leaveRepository,
            AttendanceRepository attendanceRepository,
            ActivityRepository activityRepository,
            NotificationRepository notificationRepository,
            PayrollRepository payrollRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            PerformanceScoreRepository performanceScoreRepository,
            JobOpeningRepository jobOpeningRepository,
            CandidateRepository candidateRepository,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.assetRepository = assetRepository;
        this.assetAssignmentRepository = assetAssignmentRepository;
        this.userRepository = userRepository;
        this.leaveRepository = leaveRepository;
        this.attendanceRepository = attendanceRepository;
        this.activityRepository = activityRepository;
        this.notificationRepository = notificationRepository;
        this.payrollRepository = payrollRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.performanceScoreRepository = performanceScoreRepository;
        this.jobOpeningRepository = jobOpeningRepository;
        this.candidateRepository = candidateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -------------------------------------------------------------------------
    // Employee directory operations
    // -------------------------------------------------------------------------

    public List<EmployeeView> employees(String search) {
        List<Employee> employees = search == null || search.isBlank()
                ? employeeRepository.findAll()
                : employeeRepository.search(search);
        return employees.stream().map(this::toEmployeeView).toList();
    }

    public EmployeeView employee(Long id) {
        return toEmployeeView(findEmployee(id));
    }

    public EmployeeView createEmployee(EmployeeRequest request) {
        Employee employee = new Employee();
        applyEmployeeRequest(employee, request);
        Employee savedEmployee = employeeRepository.save(employee);
        savedEmployee.setEmployeeCode("EMP-%03d".formatted(savedEmployee.getId()));
        if (savedEmployee.getStatus() == EmployeeStatus.ACTIVE) {
            createPayrollForEmployee(savedEmployee, YearMonth.now().toString());
        }
        recordActivity(savedEmployee.getId(), ActivityType.EMPLOYEE_CREATED, "Employee profile created");
        createNotification(savedEmployee.getId(), "Welcome to Orchasp HRMS", "Your employee profile has been created.", NotificationType.EMPLOYEE_CREATED);
        if (request.initialPassword() != null && !request.initialPassword().isBlank()) {
            User account = new User();
            account.setEmail(savedEmployee.getEmail());
            account.setPasswordHash(passwordEncoder.encode(request.initialPassword()));
            account.setRole(Role.EMPLOYEE);
            account.setEmployee(savedEmployee);
            userRepository.save(account);
        }
        return toEmployeeView(savedEmployee);
    }

    public EmployeeView updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        applyEmployeeRequest(employee, request);
        userRepository.findByEmployeeId(id).ifPresent(account -> account.setEmail(employee.getEmail()));
        payrollRepository.findByEmployeeIdOrderByMonthDesc(id).stream()
                .filter(payroll -> payroll.getStatus() == PayrollStatus.PENDING)
                .forEach(payroll -> {
                    recalculatePayrollFromEmployee(payroll, employee);
                    payrollRepository.save(payroll);
                });
        recordActivity(id, ActivityType.EMPLOYEE_UPDATED, "Employee profile updated");
        return toEmployeeView(employee);
    }

public void deleteEmployee(Long id) {
        Employee employee = findEmployee(id);
        Long employeeId = employee.getId();
// Remove dependent records first to avoid foreign-key constraint violations.
        attendanceRepository.deleteByEmployeeId(employeeId);
        leaveRepository.deleteByEmployeeId(employeeId);
        payrollRepository.deleteByEmployeeId(employeeId);
        activityRepository.deleteByEmployeeId(employeeId);
        notificationRepository.deleteByEmployeeId(employeeId);
        assetAssignmentRepository.deleteByEmployeeId(employeeId);
        userRepository.deleteByEmployeeId(employeeId);
        employeeRepository.delete(employee);
    }

    private void applyEmployeeRequest(Employee employee, EmployeeRequest request) {
        if (request.dateOfBirth().isAfter(LocalDate.now().minusYears(14))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee must be at least 14 years old");
        }
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email().trim().toLowerCase());
        employee.setPhone(request.phone());
        employee.setJobTitle(request.jobTitle());
        employee.setLocation(request.location());
        employee.setSalary(request.salary());
        employee.setHireDate(request.hireDate());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setEmploymentType(request.employmentType());
        employee.setStatus(request.status() == null ? EmployeeStatus.ACTIVE : request.status());
        employee.setProfilePicture(request.profilePicture());
        employee.setAddress(request.address());
        employee.setEmergencyContact(request.emergencyContact());
        if (request.departmentId() == null) {
            employee.setDepartment(null);
        } else {
            employee.setDepartment(findDepartment(request.departmentId()));
        }
    }

    public EmployeeView personalProfile(String email, PersonalProfileRequest request) {
        User user = user(email);
        if (user.getEmployee() == null) {
            throw new AccessDeniedException("Only employees have a personal profile");
        }
        Employee employee = user.getEmployee();
        employee.setPhone(request.phone());
        employee.setAddress(request.address());
        employee.setEmergencyContact(request.emergencyContact());
        employee.setProfilePicture(request.profilePicture());
        return toEmployeeView(employee);
    }

    public HrProfileView hrProfile(String email) {
        User account = user(email);
        requireHrOrAdmin(email);
        return toHrProfileView(account);
    }

    public HrProfileView updateHrProfile(String email, HrProfileRequest request) {
        User account = user(email);
        requireHrOrAdmin(email);
        if (!account.getEmail().equalsIgnoreCase(request.email())
                && userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }
        account.setDisplayName(request.name());
        account.setEmail(request.email());
        account.setPhone(request.phone());
        account.setDepartment(request.department());
        account.setDesignation(request.designation());
        return toHrProfileView(userRepository.save(account));
    }

    private HrProfileView toHrProfileView(User account) {
        String fallbackName = account.getEmployee() == null ? account.getEmail()
                : account.getEmployee().getFirstName() + " " + account.getEmployee().getLastName();
        String fallbackDepartment = account.getEmployee() != null && account.getEmployee().getDepartment() != null
                ? account.getEmployee().getDepartment().getName() : "Human Resources";
        String fallbackDesignation = account.getRole() == Role.ADMIN ? "Administrator" : "HR Manager";
        return new HrProfileView(account.getId(),
                account.getDisplayName() == null || account.getDisplayName().isBlank() ? fallbackName : account.getDisplayName(),
                account.getEmail(), account.getPhone() == null ? "" : account.getPhone(),
                account.getDepartment() == null || account.getDepartment().isBlank() ? fallbackDepartment : account.getDepartment(),
                account.getDesignation() == null || account.getDesignation().isBlank() ? fallbackDesignation : account.getDesignation(),
                account.getRole().name());
    }

    // -------------------------------------------------------------------------
    // Department operations
    // -------------------------------------------------------------------------

    public List<DepartmentView> departments() {
        return departmentRepository.findAll().stream().map(this::toDepartmentView).toList();
    }

    public DepartmentView createDepartment(DepartmentRequest request) {
        Department department = new Department();
        applyDepartmentRequest(department, request);
        return toDepartmentView(departmentRepository.save(department));
    }

    public DepartmentView updateDepartment(Long id, DepartmentRequest request) {
        Department department = findDepartment(id);
        applyDepartmentRequest(department, request);
        return toDepartmentView(department);
    }

    public void deleteDepartment(Long id) {
        departmentRepository.delete(findDepartment(id));
    }

    private void applyDepartmentRequest(Department department, DepartmentRequest request) {
        department.setName(request.name());
        department.setDescription(request.description());
        department.setColor(request.color());
        department.setBudget(request.budget());
    }

    // -------------------------------------------------------------------------
    // Asset inventory and assignment operations
    // -------------------------------------------------------------------------

    public List<AssetView> assets() {
        return assetRepository.findAll().stream().map(this::toAssetView).toList();
    }

    public AssetView createAsset(AssetRequest request) {
        Asset asset = new Asset();
        applyAssetRequest(asset, request);
        return toAssetView(assetRepository.save(asset));
    }

    public AssetView updateAsset(Long id, AssetRequest request) {
        Asset asset = findAsset(id);
        applyAssetRequest(asset, request);
        return toAssetView(asset);
    }

    public void deleteAsset(Long id) {
        assetRepository.delete(findAsset(id));
    }

    private void applyAssetRequest(Asset asset, AssetRequest request) {
        asset.setName(request.name());
        asset.setCategory(request.category());
        asset.setSerialNumber(request.serial());
        asset.setValue(request.value());
        asset.setStatus(request.status() == null ? AssetStatus.AVAILABLE : request.status());
    }

    public AssetView assign(Long assetId, Long employeeId) {
        Asset asset = findAsset(assetId);
        if (asset.getStatus() != AssetStatus.AVAILABLE && asset.getStatus() != AssetStatus.RETURNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset is not available");
        }
        AssetAssignment assignment = new AssetAssignment();
        assignment.setAsset(asset);
        assignment.setEmployee(findEmployee(employeeId));
        assignment.setAssignedDate(LocalDate.now());
        assetAssignmentRepository.save(assignment);
        asset.setStatus(AssetStatus.ASSIGNED);
        return toAssetView(asset);
    }

    public AssetView returnAsset(Long assetId) {
        Asset asset = findAsset(assetId);
        AssetAssignment assignment = assetAssignmentRepository.findFirstByAssetAndReturnedDateIsNull(asset)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Asset is not assigned"));
        assignment.setReturnedDate(LocalDate.now());
        asset.setStatus(AssetStatus.RETURNED);
        return toAssetView(asset);
    }

    public List<AssetView> myAssets(String email) {
        User user = user(email);
        if (user.getEmployee() == null) {
            throw new AccessDeniedException("Employee account required");
        }
        return assetAssignmentRepository.findByEmployeeIdAndReturnedDateIsNull(user.getEmployee().getId())
                .stream()
                .map(assignment -> toAssetView(assignment.getAsset()))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Dashboard operations
    // -------------------------------------------------------------------------

    public DashboardView dashboard() {
        return new DashboardView(
                employeeRepository.count(),
                employeeRepository.countByStatus(EmployeeStatus.ACTIVE),
                employeeRepository.countByStatus(EmployeeStatus.ON_LEAVE),
                0,
                departmentRepository.count(),
                assetRepository.count(),
                assetRepository.countByStatus(AssetStatus.ASSIGNED),
                BigDecimal.ZERO,
                0
        );
    }

    public DashboardViewEnhanced dashboardEnhanced() {
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
        LocalDate today = LocalDate.now();
        long onLeave = leaveRepository.countByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                LeaveStatus.APPROVED, today, today);
        long presentEmployees = attendanceRepository.countByStatusAndDateAndEmployeeStatus(
                AttendanceStatus.ACTIVE, today, EmployeeStatus.ACTIVE);
        long absentEmployees = attendanceRepository.countByStatusAndDate(AttendanceStatus.ABSENT, today);
        long pendingLeaves = leaveRepository.countByStatus(LeaveStatus.PENDING);
        long approvedLeaves = leaveRepository.countByStatus(LeaveStatus.APPROVED);
        long rejectedLeaves = leaveRepository.countByStatus(LeaveStatus.REJECTED);
        long totalDepartments = departmentRepository.count();
        long totalAssets = assetRepository.count();
        long assignedAssets = assetRepository.countByStatus(AssetStatus.ASSIGNED);
        BigDecimal monthlyPayroll = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .map(Employee::getSalary)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardViewEnhanced(
                totalEmployees, activeEmployees, presentEmployees, absentEmployees,
                onLeave, pendingLeaves, approvedLeaves, rejectedLeaves,
                totalDepartments, totalAssets, assignedAssets, monthlyPayroll, 0, 0
        );
    }

    public EmployeeOverviewView employeeOverview(Long employeeId) {
        findEmployee(employeeId);
        long totalLeaves = leaveRepository.findByEmployeeIdOrderByAppliedAtDesc(employeeId).size();
        long pendingLeaves = leaveRepository.countByEmployeeIdAndStatus(employeeId, LeaveStatus.PENDING);
        long approvedLeaves = leaveRepository.countByEmployeeIdAndStatus(employeeId, LeaveStatus.APPROVED);
        long rejectedLeaves = leaveRepository.countByEmployeeIdAndStatus(employeeId, LeaveStatus.REJECTED);
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        List<Attendance> monthlyAttendance = attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(employeeId, monthStart, today);
        long attendanceRecordsCount = monthlyAttendance.size();
        long presentDays = monthlyAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long absentDays = monthlyAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long lateDays = monthlyAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long remoteDays = monthlyAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.REMOTE).count();
        List<AssetView> employeeAssets = myAssetsForEmployee(employeeId);
        return new EmployeeOverviewView(
                totalLeaves, pendingLeaves, approvedLeaves, rejectedLeaves,
                attendanceRecordsCount, presentDays, absentDays, lateDays, remoteDays,
                employeeAssets.size(), employeeAssets.size(), 0, BigDecimal.ZERO, 0
        );
    }

    public List<UpcomingBirthdayView> upcomingBirthdays() {
        return employeeRepository.findAll().stream()
                .filter(emp -> emp.getDateOfBirth() != null)
                .map(emp -> {
                    Department dept = emp.getDepartment();
                    LocalDate today = LocalDate.now();
                    int month = emp.getDateOfBirth().getMonthValue();
                    int day = Math.min(emp.getDateOfBirth().getDayOfMonth(), YearMonth.of(today.getYear(), month).lengthOfMonth());
                    LocalDate birthday = LocalDate.of(today.getYear(), month, day);
                    if (birthday.isBefore(today)) birthday = birthday.plusYears(1);
                    long daysRemaining = ChronoUnit.DAYS.between(today, birthday);
                    return new UpcomingBirthdayView(
                            emp.getId(), emp.getFirstName(), emp.getLastName(),
                            emp.getProfilePicture(), dept != null ? dept.getName() : "N/A",
                            emp.getJobTitle(), emp.getDateOfBirth(), daysRemaining
                    );
                })
                .filter(birthday -> birthday.daysRemaining() <= 5)
                .sorted(Comparator.comparingLong(UpcomingBirthdayView::daysRemaining))
                .toList();
    }

    public List<RecentActivityView> recentActivities(LocalDate startDate, LocalDate endDate, int limit) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date");
        }
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<Activity> activities = (startDate == null && endDate == null)
                ? activityRepository.findTop20ByOrderByCreatedAtDesc()
                : activityRepository.findByCreatedAtRangeWithEmployee(
                        (startDate == null ? LocalDate.of(1970, 1, 1) : startDate).atStartOfDay(),
                        (endDate == null ? LocalDate.now() : endDate.plusDays(1)).atStartOfDay());
        return activities.stream().limit(safeLimit)
                .map(activity -> {
                    Employee emp = activity.getEmployee();
                    return new RecentActivityView(
                            activity.getId(), emp.getId(),
                            emp.getFirstName() + " " + emp.getLastName(),
                            emp.getProfilePicture(),
                            activity.getActivityType().name(),
                            activity.getDescription(),
                            activity.getCreatedAt() != null ? activity.getCreatedAt().toString() : null
                    );
                })
                .toList();
    }

    public Activity recordActivity(Long employeeId, ActivityType type, String description) {
        Employee employee = findEmployee(employeeId);
        Activity activity = new Activity();
        activity.setEmployee(employee);
        activity.setActivityType(type);
        activity.setDescription(description);
        activity.setCreatedAt(LocalDateTime.now());
        return activityRepository.save(activity);
    }

    // -------------------------------------------------------------------------
    // Notification methods
    // -------------------------------------------------------------------------

    public List<NotificationView> employeeNotifications(Long employeeId) {
        return notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(this::toNotificationView)
                .toList();
    }

    public long unreadNotificationCount(Long employeeId) {
        return notificationRepository.countByEmployeeIdAndIsReadFalse(employeeId);
    }

    public NotificationView markNotificationRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setRead(true);
        return toNotificationView(notificationRepository.save(notification));
    }

    public Notification createNotification(Long employeeId, String title, String message, NotificationType type) {
        Employee employee = findEmployee(employeeId);
        Notification notification = new Notification();
        notification.setEmployee(employee);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    private NotificationView toNotificationView(Notification n) {
        return new NotificationView(
                n.getId(), n.getTitle(), n.getMessage(),
                n.getType().name(), n.isRead(),
                n.getCreatedAt() != null ? n.getCreatedAt().toString() : null
        );
    }

    // -------------------------------------------------------------------------
    // Leave management methods
    // -------------------------------------------------------------------------

    public LeaveView applyLeave(LeaveRequest request, String actorEmail) {
        requireEmployeeAccess(actorEmail, request.employeeId());
        if (request.startDate().isAfter(request.endDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date");
        }
        if (request.startDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave cannot start in the past");
        }
        if (leaveRepository.existsByEmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                request.employeeId(), LeaveStatus.PENDING, request.endDate(), request.startDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An overlapping pending leave request already exists");
        }
        Employee employee = findEmployee(request.employeeId());
        Leave leave = new Leave();
        leave.setEmployee(employee);
        try {
            leave.setLeaveType(LeaveType.valueOf(request.leaveType().trim().toUpperCase()));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid leave type");
        }
        leave.setStartDate(request.startDate());
        leave.setEndDate(request.endDate());
        leave.setReason(request.reason());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setAppliedAt(LocalDateTime.now());
        Leave saved = leaveRepository.saveAndFlush(leave);
        // Side-effects (activity + notification) must never roll back the saved leave.
        try {
            recordActivity(employee.getId(), ActivityType.LEAVE_APPLIED,
                    "Applied for " + request.leaveType() + " leave from " + request.startDate() + " to " + request.endDate());
            createNotification(employee.getId(), "Leave request submitted", "Your leave request is pending HR review.", NotificationType.LEAVE_APPLIED);
        } catch (Exception exception) {
            log.warn("Leave saved but activity/notification could not be recorded: {}", exception.getMessage());
        }
        return toLeaveView(saved);
    }

    public LeaveView approveLeave(Long leaveId, String actorEmail) {
        requireHrOrAdmin(actorEmail);
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave not found"));
        ensurePending(leave);
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setReviewedAt(LocalDateTime.now());
        Leave saved = leaveRepository.save(leave);
        createNotification(leave.getEmployee().getId(), "Leave Approved",
                "Your " + leave.getLeaveType() + " leave request has been approved.",
                NotificationType.LEAVE_APPROVED);
        recordActivity(leave.getEmployee().getId(), ActivityType.LEAVE_APPROVED,
                leave.getLeaveType() + " leave approved");
        return toLeaveView(saved);
    }

    public LeaveView rejectLeave(Long leaveId, String actorEmail) {
        requireHrOrAdmin(actorEmail);
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave not found"));
        ensurePending(leave);
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setReviewedAt(LocalDateTime.now());
        Leave saved = leaveRepository.save(leave);
        createNotification(leave.getEmployee().getId(), "Leave Rejected",
                "Your " + leave.getLeaveType() + " leave request has been rejected.",
                NotificationType.LEAVE_REJECTED);
        recordActivity(leave.getEmployee().getId(), ActivityType.LEAVE_REJECTED,
                leave.getLeaveType() + " leave rejected");
        return toLeaveView(saved);
    }

    public List<LeaveView> allLeaves(String actorEmail) {
        requireHrOrAdmin(actorEmail);
        return leaveRepository.findAllWithEmployee().stream()
                .map(this::toLeaveView)
                .toList();
    }

    public List<LeaveView> employeeLeaves(Long employeeId, String actorEmail) {
        requireEmployeeAccess(actorEmail, employeeId);
        return leaveRepository.findByEmployeeIdOrderByAppliedAtDesc(employeeId).stream()
                .map(this::toLeaveView)
                .toList();
    }

    public void cancelLeave(Long leaveId, String actorEmail) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave not found"));
        requireEmployeeAccess(actorEmail, leave.getEmployee().getId());
        ensurePending(leave);
        leaveRepository.delete(leave);
        recordActivity(leave.getEmployee().getId(), ActivityType.LEAVE_REJECTED, "Cancelled pending leave request");
    }

    private void ensurePending(Leave leave) {
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending leave requests can be changed");
        }
    }

    private void requireHrOrAdmin(String email) {
        Role role = user(email).getRole();
        if (role != Role.ADMIN && role != Role.HR) {
            throw new AccessDeniedException("HR or administrator access is required");
        }
    }

    private void requireEmployeeAccess(String email, Long employeeId) {
        User actor = user(email);
        if (actor.getRole() == Role.ADMIN || actor.getRole() == Role.HR) return;
        if (actor.getEmployee() == null || !Objects.equals(actor.getEmployee().getId(), employeeId)) {
            throw new AccessDeniedException("You can access only your own leave requests");
        }
    }

    private LeaveView toLeaveView(Leave leave) {
        Employee emp = leave.getEmployee();
        return new LeaveView(
                leave.getId(), emp.getId(),
                emp.getFirstName() + " " + emp.getLastName(),
                emp.getProfilePicture(),
                leave.getLeaveType().name(),
                leave.getStartDate(), leave.getEndDate(),
                leave.getReason(), leave.getStatus().name(),
                leave.getAppliedAt() != null ? leave.getAppliedAt().toString() : null,
                leave.getReviewedAt() != null ? leave.getReviewedAt().toString() : null
        );
    }

    // -------------------------------------------------------------------------
    // Attendance methods
    // -------------------------------------------------------------------------

    /** Records punch-in for an employee for today. Prevents duplicate punch-ins. */
    public AttendanceView punchIn(Long employeeId) {
        Employee employee = findEmployee(employeeId);
        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);
        if (existing.isPresent()) {
            Attendance current = existing.get();
            if (current.getCheckOut() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Already checked out for today");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already punched in for today");
        }
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(today);
        attendance.setCheckIn(LocalTime.now());
        attendance.setStatus(AttendanceStatus.ACTIVE);
        attendance.setWorkingHours(null);
        Attendance saved = attendanceRepository.save(attendance);
        recordActivity(employeeId, ActivityType.ATTENDANCE_PUNCH_IN, "Punched in at " + saved.getCheckIn());
        return toAttendanceView(saved);
    }

    /** Records punch-out for an employee for today. Punch-in required first. */
    public AttendanceView punchOut(Long employeeId) {
        findEmployee(employeeId);
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Punch in required before punch out"));
        if (attendance.getCheckOut() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already punched out for today");
        }
        attendance.setCheckOut(LocalTime.now());
        attendance.setStatus(AttendanceStatus.INACTIVE);
        attendance.setWorkingHours(calculateWorkingHours(attendance.getCheckIn(), attendance.getCheckOut()));
        Attendance saved = attendanceRepository.save(attendance);
        recordActivity(employeeId, ActivityType.ATTENDANCE_PUNCH_OUT, "Punched out at " + saved.getCheckOut());
        return toAttendanceView(saved);
    }

    /** Returns today's attendance records for the HR attendance sheet. */
    public List<AttendanceView> todayAttendance() {
        return attendanceRepository.findByDateWithEmployee(LocalDate.now()).stream()
                .map(this::toAttendanceView)
                .toList();
    }

/** Returns attendance history for all employees (HR view), optionally filtered by month/year. */
    public List<AttendanceView> attendanceHistory(Integer month, Integer year) {
        List<Attendance> records;
        if (month != null && year != null) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            records = attendanceRepository.findByDateRangeWithEmployee(start, end);
        } else {
            records = attendanceRepository.findAllWithEmployee();
        }
        return records.stream().map(this::toAttendanceView).toList();
    }

    /** Returns an employee's current attendance summary (today's record if present). */
    public AttendanceView attendanceSummary(Long employeeId) {
        findEmployee(employeeId);
        return attendanceRepository.findByEmployeeIdAndDate(employeeId, LocalDate.now())
                .map(this::toAttendanceView)
                .orElse(null);
    }

    /** Backward-compatible record method used by the legacy endpoint. */
    public AttendanceView recordAttendance(Long employeeId, AttendanceStatus status, LocalTime checkIn, LocalTime checkOut) {
        Employee employee = findEmployee(employeeId);
        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);
        Attendance attendance;
        if (existing.isPresent()) {
            attendance = existing.get();
            attendance.setCheckIn(checkIn != null ? checkIn : attendance.getCheckIn());
            attendance.setCheckOut(checkOut);
            attendance.setStatus(status);
            if (checkOut != null && attendance.getCheckIn() != null) {
                attendance.setWorkingHours(calculateWorkingHours(attendance.getCheckIn(), checkOut));
            }
        } else {
            attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setDate(today);
            attendance.setCheckIn(checkIn);
            attendance.setCheckOut(checkOut);
            attendance.setStatus(status);
            if (checkIn != null && checkOut != null) {
                attendance.setWorkingHours(calculateWorkingHours(checkIn, checkOut));
            }
        }
        Attendance saved = attendanceRepository.save(attendance);
        ActivityType activityType = checkOut != null ? ActivityType.ATTENDANCE_PUNCH_OUT : ActivityType.ATTENDANCE_PUNCH_IN;
        recordActivity(employeeId, activityType, "Attendance " + (checkOut != null ? "punched out" : "punched in") + " - " + status);
        return toAttendanceView(saved);
    }

    public List<AttendanceView> employeeAttendance(Long employeeId, Integer month, Integer year) {
        LocalDate start = LocalDate.of(year != null ? year : LocalDate.now().getYear(),
                month != null ? month : LocalDate.now().getMonthValue(), 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(employeeId, start, end).stream()
                .map(this::toAttendanceView)
                .toList();
    }

    private static BigDecimal calculateWorkingHours(LocalTime checkIn, LocalTime checkOut) {
        if (checkIn == null || checkOut == null) {
            return null;
        }
        long minutes = ChronoUnit.MINUTES.between(checkIn, checkOut);
        if (minutes < 0) {
            minutes += 24 * 60;
        }
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
    }

    private AttendanceView toAttendanceView(Attendance attendance) {
        Employee emp = attendance.getEmployee();
        Department dept = emp.getDepartment();
        return new AttendanceView(
                attendance.getId(), emp.getId(),
                emp.getEmployeeCode(),
                emp.getFirstName() + " " + emp.getLastName(),
                dept != null ? dept.getName() : "N/A",
                attendance.getDate() != null ? attendance.getDate().toString() : null,
                attendance.getCheckIn() != null ? attendance.getCheckIn().toString() : null,
                attendance.getCheckOut() != null ? attendance.getCheckOut().toString() : null,
                attendance.getWorkingHours() != null ? attendance.getWorkingHours().toString() : null,
                attendance.getStatus().name()
        );
    }

private List<AssetView> myAssetsForEmployee(Long employeeId) {
        return assetAssignmentRepository.findByEmployeeIdAndReturnedDateIsNull(employeeId)
                .stream()
                .map(assignment -> toAssetView(assignment.getAsset()))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Payroll methods
    // -------------------------------------------------------------------------

    /** Lists all payroll records across all months (HR view). */
    public List<PayrollView> payrollRecords(String month) {
        List<Payroll> records = month == null || month.isBlank()
                ? payrollRepository.findAllWithEmployee()
                : payrollRepository.findByMonthWithEmployee(month);
        return records.stream().map(this::toPayrollView).toList();
    }

    /** Returns payroll records for a single employee (self-service view). */
    public List<PayrollView> employeePayroll(Long employeeId) {
        return payrollRepository.findByEmployeeIdOrderByMonthDesc(employeeId).stream()
                .map(this::toPayrollView)
                .toList();
    }

    /** Creates a payroll record, preventing duplicates for the same employee/month. */
    public PayrollView createPayroll(PayrollRequest request) {
        findEmployee(request.employeeId());
        if (payrollRepository.findByEmployeeIdAndMonth(request.employeeId(), request.month()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Payroll already exists for this employee and month");
        }
        Payroll payroll = new Payroll();
        applyPayrollRequest(payroll, request);
        payroll.setNetSalary(calculateNetSalary(payroll));
        Payroll saved = payrollRepository.save(payroll);
        recordActivity(request.employeeId(), ActivityType.EMPLOYEE_UPDATED,
                "Payroll generated for " + request.month());
        return toPayrollView(saved);
    }

    /** Updates an existing payroll record. */
    public PayrollView updatePayroll(Long id, PayrollRequest request) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payroll record not found"));
        applyPayrollRequest(payroll, request);
        payroll.setNetSalary(calculateNetSalary(payroll));
        payroll.setUpdatedAt(LocalDateTime.now());
        Payroll saved = payrollRepository.save(payroll);
        recordActivity(payroll.getEmployee().getId(), ActivityType.EMPLOYEE_UPDATED,
                "Payroll updated for " + payroll.getMonth());
        return toPayrollView(saved);
    }

    /** Generates payroll records for all active employees for the given month.
     *  Salary components are derived from the employee's annual salary stored in the
     *  employee record; deductions include approved leave days beyond the allowed paid
     *  leave entitlement for the month. */
    public List<PayrollView> generatePayroll(String month) {
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .toList();
        List<PayrollView> generated = new java.util.ArrayList<>();
        for (Employee employee : activeEmployees) {
            if (payrollRepository.findByEmployeeIdAndMonth(employee.getId(), month).isPresent()) {
                continue;
            }
            // Build a payroll record from the employee's stored annual salary.
            Payroll payroll = new Payroll();
            payroll.setEmployee(employee);
            payroll.setMonth(month);
            BigDecimal annualSalary = nullSafe(employee.getSalary());
            BigDecimal basic = annualSalary.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(50)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal hra = annualSalary.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(20)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal allowances = annualSalary.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP)
                    .subtract(basic).subtract(hra);
            if (allowances.signum() < 0) allowances = BigDecimal.ZERO;
            payroll.setBasicSalary(basic);
            payroll.setHra(hra);
            payroll.setAllowances(allowances);
            payroll.setBonuses(BigDecimal.ZERO);
            payroll.setPf(basic.multiply(BigDecimal.valueOf(12)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
            // Leave-based deduction: unpaid leave days (approved leaves in month beyond allowed paid leave).
            long leaveDays = approvedLeaveDaysInMonth(employee.getId(), month);
            long allowedPaidLeaveDays = 2; // configurable monthly paid leave entitlement
            long unpaidLeaveDays = Math.max(0, leaveDays - allowedPaidLeaveDays);
            BigDecimal monthlyGross = annualSalary.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal leaveDeduction = monthlyGross
                    .divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(unpaidLeaveDays));
            payroll.setDeductions(leaveDeduction);
            payroll.setTax(BigDecimal.ZERO);
            payroll.setStatus(PayrollStatus.PENDING);
            payroll.setNetSalary(calculateNetSalary(payroll));
            Payroll saved = payrollRepository.save(payroll);
            recordActivity(employee.getId(), ActivityType.EMPLOYEE_UPDATED,
                    "Payroll generated for " + month + " (leave days: " + leaveDays + ")");
            generated.add(toPayrollView(saved));
        }
        return generated;
    }

    /** Creates one draft payroll only when that employee does not already have one. */
    private void createPayrollForEmployee(Employee employee, String month) {
        if (payrollRepository.findByEmployeeIdAndMonth(employee.getId(), month).isPresent()) {
            return;
        }
        Payroll payroll = new Payroll();
        payroll.setEmployee(employee);
        payroll.setMonth(month);
        payroll.setBonuses(BigDecimal.ZERO);
        payroll.setPf(BigDecimal.ZERO);
        payroll.setTax(BigDecimal.ZERO);
        payroll.setStatus(PayrollStatus.PENDING);
        recalculatePayrollFromEmployee(payroll, employee);
        payrollRepository.save(payroll);
    }

    /** Recalculates only a draft payroll from current employee salary and approved leave. */
    private void recalculatePayrollFromEmployee(Payroll payroll, Employee employee) {
        BigDecimal annualSalary = nullSafe(employee.getSalary());
        BigDecimal monthlyGross = annualSalary.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal basic = monthlyGross.multiply(BigDecimal.valueOf(50))
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal hra = monthlyGross.multiply(BigDecimal.valueOf(20))
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        payroll.setBasicSalary(basic);
        payroll.setHra(hra);
        payroll.setAllowances(monthlyGross.subtract(basic).subtract(hra).max(BigDecimal.ZERO));
        payroll.setPf(basic.multiply(BigDecimal.valueOf(12)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
        long unpaidLeaveDays = Math.max(0, approvedLeaveDaysInMonth(employee.getId(), payroll.getMonth()) - 2);
        payroll.setDeductions(monthlyGross.divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(unpaidLeaveDays)));
        payroll.setNetSalary(calculateNetSalary(payroll));
        payroll.setUpdatedAt(LocalDateTime.now());
    }

    /** Counts approved leave days that fall inside the given month for an employee. */
    private long approvedLeaveDaysInMonth(Long employeeId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return leaveRepository.findApprovedOverlapping(employeeId, start, end).stream()
                .mapToLong(leave -> {
                    LocalDate from = leave.getStartDate().isAfter(start) ? leave.getStartDate() : start;
                    LocalDate to = leave.getEndDate().isBefore(end) ? leave.getEndDate() : end;
                    return ChronoUnit.DAYS.between(from, to) + 1;
                })
                .sum();
    }

    /** Returns payroll summary cards for the HR dashboard. */
    public PayrollSummaryView payrollSummary(String month) {
        String effectiveMonth = month == null || month.isBlank()
                ? java.time.YearMonth.now().toString()
                : month;
List<Payroll> records = payrollRepository.findByMonthWithEmployee(effectiveMonth);
        BigDecimal totalPayroll = records.stream()
                .map(Payroll::getNetSalary)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PayrollSummaryView(totalPayroll, records.size(), effectiveMonth);
    }

    private void applyPayrollRequest(Payroll payroll, PayrollRequest request) {
        payroll.setEmployee(findEmployee(request.employeeId()));
        payroll.setMonth(request.month());
        payroll.setBasicSalary(request.basicSalary());
        payroll.setHra(request.hra());
        payroll.setAllowances(request.allowances());
        payroll.setBonuses(request.bonuses());
        payroll.setDeductions(request.deductions());
        payroll.setPf(request.pf());
        payroll.setTax(request.tax());
        if (request.status() != null) {
            payroll.setStatus(request.status());
        }
        if (request.status() == PayrollStatus.PAID) {
            payroll.setPayDate(LocalDate.now());
        }
    }

    private static BigDecimal calculateNetSalary(Payroll payroll) {
        BigDecimal gross = payroll.getBasicSalary()
                .add(nullSafe(payroll.getHra()))
                .add(nullSafe(payroll.getAllowances()))
                .add(nullSafe(payroll.getBonuses()));
        BigDecimal totalDeductions = nullSafe(payroll.getDeductions())
                .add(nullSafe(payroll.getPf()))
                .add(nullSafe(payroll.getTax()));
        return gross.subtract(totalDeductions);
    }

    private static BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private PayrollView toPayrollView(Payroll payroll) {
        Employee emp = payroll.getEmployee();
        Department dept = emp.getDepartment();
        return new PayrollView(
                payroll.getId(), emp.getId(), emp.getEmployeeCode(),
                emp.getFirstName() + " " + emp.getLastName(),
                dept != null ? dept.getName() : "N/A",
                payroll.getMonth(), payroll.getBasicSalary(),
                payroll.getHra(), payroll.getAllowances(), payroll.getBonuses(),
                payroll.getDeductions(), payroll.getPf(), payroll.getTax(),
                payroll.getNetSalary(), payroll.getStatus().name(),
                payroll.getPayDate() != null ? payroll.getPayDate().toString() : null
        );
    }

    // -------------------------------------------------------------------------
    // Project operations
    // -------------------------------------------------------------------------

    public List<ProjectView> projects() {
        return projectRepository.findAllByOrderByNameAsc().stream()
                .map(this::toProjectView)
                .toList();
    }

    public ProjectView createProject(ProjectRequest request) {
        Project project = new Project();
        applyProjectRequest(project, request);
        return toProjectView(projectRepository.save(project));
    }

    public ProjectView updateProject(Long id, ProjectRequest request) {
        Project project = findProject(id);
        applyProjectRequest(project, request);
        return toProjectView(project);
    }

    public void deleteProject(Long id) {
        projectRepository.delete(findProject(id));
    }

    /** Gets projects assigned to a specific employee (managed by them as project manager). */
    public List<ProjectView> employeeProjects(String email) {
        User user = user(email);
        if (user.getEmployee() == null) {
            throw new AccessDeniedException("Employee account required");
        }
        return projectRepository.findAllByOrderByNameAsc().stream()
                .filter(project -> project.getProjectManager() != null
                        && Objects.equals(project.getProjectManager().getId(), user.getEmployee().getId()))
                .map(this::toProjectView)
                .toList();
    }

    private void applyProjectRequest(Project project, ProjectRequest request) {
        project.setName(request.name());
        project.setDescription(request.description());
        project.setTechnology(request.technology());
        if (request.status() != null && !request.status().isBlank()) {
            project.setStatus(request.status().toUpperCase());
        }
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        if (request.projectManagerId() != null) {
            project.setProjectManager(findEmployee(request.projectManagerId()));
        } else {
            project.setProjectManager(null);
        }
    }

    private ProjectView toProjectView(Project project) {
        Employee manager = project.getProjectManager();
        return new ProjectView(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getTechnology(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                manager != null ? manager.getId() : null,
                manager != null ? manager.getFirstName() + " " + manager.getLastName() : null,
                0
        );
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    // -------------------------------------------------------------------------
    // Task workflow operations
    // -------------------------------------------------------------------------

    public List<TaskView> tasks() {
        return taskRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toTaskView)
                .toList();
    }

    public List<TaskView> myTasks(String email) {
        User user = user(email);
        if (user.getEmployee() == null) {
            throw new AccessDeniedException("Employee account required");
        }
        return taskRepository.findByAssignedToIdOrderByCreatedAtDesc(user.getEmployee().getId()).stream()
                .map(this::toTaskView)
                .toList();
    }

    public List<TaskView> tasksByStatus(TaskStatus status) {
        return taskRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::toTaskView)
                .toList();
    }

    public TaskView createTask(TaskRequest request, String actorEmail) {
        User actor = user(actorEmail);
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.priority() != null && !request.priority().isBlank()) {
            task.setPriority(request.priority().toUpperCase());
        }
        task.setDueDate(request.dueDate());
        task.setAssignedTo(findEmployee(request.assignedToId()));
        task.setCreatedBy(actor);
        task.setStatus(TaskStatus.TODO);
        task.setCreatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        createNotification(request.assignedToId(), "New task assigned",
                "You have been assigned a new task: " + request.title(),
                NotificationType.EMPLOYEE_CREATED);
        recordActivity(request.assignedToId(), ActivityType.EMPLOYEE_UPDATED,
                "Task '" + request.title() + "' assigned");
        return toTaskView(saved);
    }

    public TaskView updateTask(Long id, TaskRequest request) {
        Task task = findTask(id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.priority() != null && !request.priority().isBlank()) {
            task.setPriority(request.priority().toUpperCase());
        }
        task.setDueDate(request.dueDate());
        task.setAssignedTo(findEmployee(request.assignedToId()));
        task.setUpdatedAt(LocalDateTime.now());
        return toTaskView(taskRepository.save(task));
    }

    /** Transitions a task to the next workflow state with role-based guards. */
    public TaskView transitionTask(Long taskId, TaskStatus target, String actorEmail) {
        Task task = findTask(taskId);
        User actor = user(actorEmail);
        TaskStatus current = task.getStatus();
        boolean isHrOrAdmin = actor.getRole() == Role.ADMIN || actor.getRole() == Role.HR;
        boolean isAssignee = actor.getEmployee() != null
                && Objects.equals(actor.getEmployee().getId(), task.getAssignedTo().getId());

        if (!isHrOrAdmin && (!isAssignee || current != TaskStatus.IN_PROGRESS || target != TaskStatus.REVIEW)) {
            throw new AccessDeniedException("Only the assigned employee can submit an in-progress task for review");
        }
        if (isHrOrAdmin) {
            boolean validHrTransition = (current == TaskStatus.TODO && target == TaskStatus.IN_PROGRESS && task.getAcceptedAt() != null)
                    || (current == TaskStatus.IN_PROGRESS && target == TaskStatus.REVIEW)
                    || (current == TaskStatus.REVIEW && target == TaskStatus.DONE);
            if (!validHrTransition) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid task workflow transition");
            }
        }

        task.setStatus(target);
        task.setUpdatedAt(LocalDateTime.now());
        if (target == TaskStatus.REVIEW) {
            createNotification(task.getAssignedTo().getId(), "Task in review",
                    "Task '" + task.getTitle() + "' has been moved to review.",
                    NotificationType.EMPLOYEE_UPDATED);
        }
        if (target == TaskStatus.DONE) {
            createNotification(task.getAssignedTo().getId(), "Task completed",
                    "Task '" + task.getTitle() + "' has been completed.",
                    NotificationType.EMPLOYEE_UPDATED);
            recordActivity(task.getAssignedTo().getId(), ActivityType.EMPLOYEE_UPDATED,
                    "Task '" + task.getTitle() + "' completed");
        }
        return toTaskView(taskRepository.save(task));
    }

    /** Records acceptance without changing the workflow column; HR controls the start. */
    public TaskView acceptTask(Long taskId, String actorEmail) {
        Task task = findTask(taskId);
        User actor = user(actorEmail);
        if (actor.getEmployee() == null || !Objects.equals(actor.getEmployee().getId(), task.getAssignedTo().getId())) {
            throw new AccessDeniedException("Only the assigned employee can accept this task");
        }
        if (task.getStatus() != TaskStatus.TODO || task.getAcceptedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only an unaccepted To Do task can be accepted");
        }
        task.setAcceptedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        recordActivity(task.getAssignedTo().getId(), ActivityType.EMPLOYEE_UPDATED,
                "Task '" + task.getTitle() + "' accepted");
        return toTaskView(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        taskRepository.delete(findTask(id));
    }

    private TaskView toTaskView(Task task) {
        Employee assigned = task.getAssignedTo();
        User creator = task.getCreatedBy();
        return new TaskView(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus().name(),
                task.getDueDate(),
                assigned.getId(),
                assigned.getFirstName() + " " + assigned.getLastName(),
                assigned.getProfilePicture(),
                creator != null ? creator.getId() : null,
                creator != null ? creator.getEmail() : null,
                task.getCreatedAt() != null ? task.getCreatedAt().toString() : null,
                task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : null,
                task.getAcceptedAt() != null ? task.getAcceptedAt().toString() : null
        );
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    // -------------------------------------------------------------------------
    // Performance operations
    // -------------------------------------------------------------------------

    public List<PerformanceScoreView> performanceScores() {
        return performanceScoreRepository.findAllByOrderByReviewDateDesc().stream()
                .map(this::toPerformanceScoreView)
                .toList();
    }

    public List<PerformanceScoreView> employeePerformance(Long employeeId, String actorEmail) {
        User actor = user(actorEmail);
        if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.HR
                && (actor.getEmployee() == null || !Objects.equals(actor.getEmployee().getId(), employeeId))) {
            throw new AccessDeniedException("You can view only your own performance scores");
        }
        return performanceScoreRepository.findByEmployeeIdOrderByReviewDateDesc(employeeId).stream()
                .map(this::toPerformanceScoreView)
                .toList();
    }

    public PerformanceScoreView createPerformanceScore(PerformanceScoreRequest request, String actorEmail) {
        requireHrOrAdmin(actorEmail);
        PerformanceScore score = new PerformanceScore();
        applyPerformanceScore(score, request);
        score.setReviewedBy(user(actorEmail));
        score.setReviewDate(LocalDate.now());
        PerformanceScore saved = performanceScoreRepository.save(score);
        recordActivity(request.employeeId(), ActivityType.EMPLOYEE_UPDATED,
                "Performance review recorded");
        createNotification(request.employeeId(), "Performance review updated",
                "Your performance scores have been updated by HR.",
                NotificationType.EMPLOYEE_UPDATED);
        return toPerformanceScoreView(saved);
    }

    public PerformanceScoreView updatePerformanceScore(Long id, PerformanceScoreRequest request, String actorEmail) {
        requireHrOrAdmin(actorEmail);
        PerformanceScore score = performanceScoreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performance score not found"));
        applyPerformanceScore(score, request);
        score.setReviewedBy(user(actorEmail));
        PerformanceScore saved = performanceScoreRepository.save(score);
        recordActivity(request.employeeId(), ActivityType.EMPLOYEE_UPDATED,
                "Performance review updated");
        return toPerformanceScoreView(saved);
    }

    private void applyPerformanceScore(PerformanceScore score, PerformanceScoreRequest request) {
        score.setEmployee(findEmployee(request.employeeId()));
        score.setCommunication(request.communication());
        score.setAttendance(request.attendance());
        score.setProductivity(request.productivity());
        score.setTechnicalSkills(request.technicalSkills());
        score.setLeadership(request.leadership());
        score.setDiscipline(request.discipline());
        score.setOverallRating(request.overallRating());
    }

    private PerformanceScoreView toPerformanceScoreView(PerformanceScore score) {
        Employee emp = score.getEmployee();
        return new PerformanceScoreView(
                score.getId(),
                emp.getId(),
                emp.getFirstName() + " " + emp.getLastName(),
                emp.getProfilePicture(),
                score.getCommunication(),
                score.getAttendance(),
                score.getProductivity(),
                score.getTechnicalSkills(),
                score.getLeadership(),
                score.getDiscipline(),
                score.getOverallRating(),
                score.getReviewDate() != null ? score.getReviewDate().toString() : null
        );
    }

    // -------------------------------------------------------------------------
    // Recruitment operations
    // -------------------------------------------------------------------------

    public List<JobOpeningView> jobOpenings() {
        return jobOpeningRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toJobOpeningView)
                .toList();
    }

    public JobOpeningView createJobOpening(JobOpeningRequest request) {
        JobOpening opening = new JobOpening();
        applyJobOpening(opening, request);
        return toJobOpeningView(jobOpeningRepository.save(opening));
    }

    public JobOpeningView updateJobOpening(Long id, JobOpeningRequest request) {
        JobOpening opening = jobOpeningRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job opening not found"));
        applyJobOpening(opening, request);
        return toJobOpeningView(opening);
    }

    public void deleteJobOpening(Long id) {
        jobOpeningRepository.deleteById(id);
    }

    private void applyJobOpening(JobOpening opening, JobOpeningRequest request) {
        opening.setPosition(request.position());
        opening.setDepartment(request.department());
        opening.setVacancies(request.vacancies());
        opening.setLocation(request.location());
        opening.setEmploymentType(request.employmentType());
        opening.setExperience(request.experience());
        opening.setDescription(request.description());
    }

    private JobOpeningView toJobOpeningView(JobOpening opening) {
        return new JobOpeningView(
                opening.getId(),
                opening.getPosition(),
                opening.getDepartment(),
                opening.getVacancies(),
                opening.getLocation(),
                opening.getEmploymentType(),
                opening.getExperience(),
                opening.getDescription(),
                opening.isActive(),
                opening.getCreatedAt() != null ? opening.getCreatedAt().toString() : null
        );
    }

    public List<CandidateView> candidates() {
        return candidateRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toCandidateView)
                .toList();
    }

    public CandidateView createCandidate(CandidateRequest request) {
        Candidate candidate = new Candidate();
        applyCandidate(candidate, request);
        return toCandidateView(candidateRepository.save(candidate));
    }

    public CandidateView updateCandidate(Long id, CandidateRequest request) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        applyCandidate(candidate, request);
        return toCandidateView(candidateRepository.save(candidate));
    }

    public CandidateView updateCandidateStage(Long id, String stage) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        candidate.setStage(stage.toUpperCase());
        return toCandidateView(candidateRepository.save(candidate));
    }

    public void deleteCandidate(Long id) {
        candidateRepository.deleteById(id);
    }

    private void applyCandidate(Candidate candidate, CandidateRequest request) {
        candidate.setName(request.name());
        candidate.setRole(request.role());
        if (request.stage() != null && !request.stage().isBlank()) {
            candidate.setStage(request.stage().toUpperCase());
        }
        candidate.setRating(request.rating());
        candidate.setPhone(request.phone());
        candidate.setEmail(request.email());
        candidate.setResumeUrl(request.resumeUrl());
        candidate.setSource(request.source());
        if (request.jobOpeningId() != null) {
            candidate.setJobOpening(jobOpeningRepository.findById(request.jobOpeningId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job opening not found")));
        } else {
            candidate.setJobOpening(null);
        }
    }

    private CandidateView toCandidateView(Candidate candidate) {
        JobOpening opening = candidate.getJobOpening();
        return new CandidateView(
                candidate.getId(),
                candidate.getName(),
                candidate.getRole(),
                candidate.getStage(),
                candidate.getRating(),
                candidate.getPhone(),
                candidate.getEmail(),
                candidate.getResumeUrl(),
                candidate.getSource(),
                opening != null ? opening.getId() : null,
                opening != null ? opening.getPosition() : null,
                candidate.getCreatedAt() != null ? candidate.getCreatedAt().toString() : null
        );
    }

    public RecruitmentSummaryView recruitmentSummary() {
        long totalCandidates = candidateRepository.count();
        long interviewScheduled = candidateRepository.countByStage("INTERVIEW") + candidateRepository.countByStage("TECHNICAL") + candidateRepository.countByStage("HR_ROUND");
        long offers = candidateRepository.countByStage("OFFER");
        long hired = candidateRepository.countByStage("HIRED");
        long rejected = candidateRepository.countByStage("REJECTED");
        long openPositions = jobOpeningRepository.countByActiveTrue();
        return new RecruitmentSummaryView(
                totalCandidates, interviewScheduled, offers, hired, rejected, openPositions
        );
    }

    // -------------------------------------------------------------------------
    // Account lookup and DTO mapping helpers
    // -------------------------------------------------------------------------

    public User user(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    public UserView userView(User user) {
        Employee employee = user.getEmployee();
        String name = employee == null ? user.getEmail() : employee.getFirstName() + " " + employee.getLastName();
        return new UserView(user.getId(), user.getEmail(), user.getRole().name(),
                employee == null ? null : employee.getId(),
                employee == null ? null : employee.getEmployeeCode(), name);
    }

    private EmployeeView toEmployeeView(Employee employee) {
        Department department = employee.getDepartment();
        return new EmployeeView(employee.getId(), employee.getEmployeeCode(),
                employee.getFirstName(), employee.getLastName(), employee.getEmail(),
                employee.getPhone(), employee.getJobTitle(),
                department == null ? null : department.getId(),
                department == null ? null : department.getName(),
                employee.getLocation(), employee.getSalary(), employee.getHireDate(), employee.getDateOfBirth(),
                employee.getEmploymentType(), employee.getStatus(),
                employee.getProfilePicture(), employee.getAddress(), employee.getEmergencyContact());
    }

    private DepartmentView toDepartmentView(Department department) {
        return new DepartmentView(department.getId(), department.getName(),
                department.getDescription(), department.getColor(), department.getBudget());
    }

    private AssetView toAssetView(Asset asset) {
        Optional<AssetAssignment> assignment = assetAssignmentRepository.findFirstByAssetAndReturnedDateIsNull(asset);
        Long employeeId = assignment.map(value -> value.getEmployee().getId()).orElse(null);
        String assignedDate = assignment.map(value -> value.getAssignedDate().toString()).orElse(null);
        return new AssetView(asset.getId(), asset.getName(), asset.getCategory(),
                asset.getSerialNumber(), asset.getValue(), asset.getStatus(), employeeId, assignedDate);
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    private Asset findAsset(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
    }
}

package com.pavan.employeemanagement.service;

import com.pavan.employeemanagement.dto.Dtos.*;
import com.pavan.employeemanagement.entity.*;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;

/** Imports CSV rows through the same business methods used by the normal forms. */
@Service
public class BulkImportService {
    private final HrmsService hrms;
    public BulkImportService(HrmsService hrms) { this.hrms = hrms; }

    public BulkImportResult importFile(String module, MultipartFile file, String actorEmail) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Choose a non-empty CSV or Excel file");
        String normalized = module == null ? "" : module.trim().toLowerCase(Locale.ROOT);
        var errors = new ArrayList<String>(); int total = 0, imported = 0;
        try {
            for (Map<String, String> values : rows(file)) {
                total++;
                try { importRow(normalized, values, actorEmail); imported++; }
                catch (Exception e) { errors.add("Data row " + (total + 1) + ": " + readable(e)); }
            }
        } catch (Exception e) { throw new IllegalArgumentException("Cannot read import file: " + readable(e)); }
        return new BulkImportResult(normalized, total, imported, errors);
    }

    /** Creates an Excel workbook whose headers exactly match the importer for the selected module. */
    public ByteArrayResource template(String module) {
        String normalized = module == null ? "" : module.trim().toLowerCase(Locale.ROOT);
        String[] headers = headers(normalized);
        try (var workbook = new XSSFWorkbook(); var output = new java.io.ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Import template");
            var headerRow = sheet.createRow(0); var exampleRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) { headerRow.createCell(i).setCellValue(headers[i]); exampleRow.createCell(i).setCellValue(example(headers[i])); sheet.autoSizeColumn(i); }
            workbook.write(output);
            return new ByteArrayResource(output.toByteArray());
        } catch (Exception e) { throw new IllegalStateException("Cannot create Excel template", e); }
    }

    private static String[] headers(String module) {
        return switch (module) {
            case "employees" -> "firstName,lastName,email,phone,jobTitle,departmentId,location,salary,hireDate,dateOfBirth,employmentType,status,profilePicture,address,emergencyContact,initialPassword".split(",");
            case "departments" -> "name,description,color,budget".split(",");
            case "assets" -> "name,category,serial,value,status".split(",");
            case "projects" -> "name,description,technology,status,startDate,endDate,projectManagerId".split(",");
            case "tasks" -> "title,description,priority,dueDate,assignedToId".split(",");
            case "performance" -> "employeeId,communication,attendance,productivity,technicalSkills,leadership,discipline,overallRating".split(",");
            case "job-openings" -> "position,department,vacancies,location,employmentType,experience,description".split(",");
            case "candidates" -> "name,role,stage,rating,phone,email,resumeUrl,source,jobOpeningId".split(",");
            case "payroll" -> "employeeId,month,basicSalary,hra,allowances,bonuses,deductions,pf,tax,status".split(",");
            case "leaves" -> "employeeId,leaveType,startDate,endDate,reason".split(",");
            case "attendance" -> "employeeId,status".split(",");
            default -> throw new IllegalArgumentException("Unsupported import module");
        };
    }

    private static String example(String header) {
        return switch (header) { case "firstName" -> "Asha"; case "lastName" -> "Kumar"; case "email" -> "asha@example.com"; case "phone" -> "9876543210"; case "jobTitle", "role" -> "Developer"; case "department", "category" -> "Engineering"; case "departmentId", "employeeId", "assignedToId", "projectManagerId", "jobOpeningId" -> "1"; case "location" -> "Hyderabad"; case "salary", "value", "basicSalary" -> "60000"; case "hireDate", "dateOfBirth", "startDate", "endDate", "dueDate" -> "2026-01-01"; case "month" -> "2026-08"; case "status" -> "ACTIVE"; case "initialPassword" -> "Welcome@123"; case "serial" -> "ASSET-001"; case "priority" -> "HIGH"; case "communication", "attendance", "productivity", "technicalSkills", "leadership", "discipline", "overallRating", "rating" -> "80"; case "vacancies" -> "1"; case "hra", "allowances", "bonuses", "deductions", "pf", "tax" -> "0"; default -> ""; };
    }

    private Iterable<Map<String, String>> rows(MultipartFile file) throws Exception {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        var rows = new ArrayList<Map<String, String>>();
        if (name.endsWith(".xlsx")) {
            try (var workbook = WorkbookFactory.create(file.getInputStream())) {
                var sheet = workbook.getSheetAt(0); var header = sheet.getRow(sheet.getFirstRowNum());
                if (header == null) throw new IllegalArgumentException("Excel sheet must have a header row");
                var formatter = new DataFormatter();
                for (int i = header.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i); if (row == null) continue;
                    var values = new LinkedHashMap<String, String>(); boolean hasValue = false;
                    for (int c = 0; c < header.getLastCellNum(); c++) { String key = formatter.formatCellValue(header.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim(); String value = formatter.formatCellValue(row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim(); if (!key.isBlank()) values.put(key, value); hasValue |= !value.isBlank(); }
                    if (hasValue) rows.add(values);
                }
            }
        } else if (name.endsWith(".csv")) {
            try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).setIgnoreEmptyLines(true).build().parse(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                for (CSVRecord row : parser) rows.add(row.toMap());
            }
        } else throw new IllegalArgumentException("Only .xlsx and .csv files are supported");
        return rows;
    }

    private void importRow(String module, Map<String, String> r, String actor) {
        switch (module) {
            case "employees" -> hrms.createEmployee(new EmployeeRequest(req(r,"firstName"), req(r,"lastName"), req(r,"email"), req(r,"phone"), req(r,"jobTitle"), lng(r,"departmentId"), req(r,"location"), decimal(r,"salary"), date(r,"hireDate"), date(r,"dateOfBirth"), val(r,"employmentType"), enumVal(EmployeeStatus.class, val(r,"status"), EmployeeStatus.ACTIVE), val(r,"profilePicture"), val(r,"address"), val(r,"emergencyContact"), val(r,"initialPassword")));
            case "departments" -> hrms.createDepartment(new DepartmentRequest(req(r,"name"), val(r,"description"), val(r,"color"), decimalOrNull(r,"budget")));
            case "assets" -> hrms.createAsset(new AssetRequest(req(r,"name"), req(r,"category"), req(r,"serial"), decimal(r,"value"), enumVal(AssetStatus.class, val(r,"status"), AssetStatus.AVAILABLE)));
            case "projects" -> hrms.createProject(new ProjectRequest(req(r,"name"), req(r,"description"), val(r,"technology"), val(r,"status"), dateOrNull(r,"startDate"), dateOrNull(r,"endDate"), lng(r,"projectManagerId")));
            case "tasks" -> hrms.createTask(new TaskRequest(req(r,"title"), val(r,"description"), req(r,"priority"), date(r,"dueDate"), lngRequired(r,"assignedToId")), actor);
            case "performance" -> hrms.createPerformanceScore(new PerformanceScoreRequest(lngRequired(r,"employeeId"), decimal(r,"communication"), decimal(r,"attendance"), decimal(r,"productivity"), decimal(r,"technicalSkills"), decimal(r,"leadership"), decimal(r,"discipline"), decimal(r,"overallRating")), actor);
            case "job-openings" -> hrms.createJobOpening(new JobOpeningRequest(req(r,"position"), req(r,"department"), integer(r,"vacancies"), req(r,"location"), val(r,"employmentType"), val(r,"experience"), val(r,"description")));
            case "candidates" -> hrms.createCandidate(new CandidateRequest(req(r,"name"), req(r,"role"), val(r,"stage"), integerOrNull(r,"rating"), val(r,"phone"), val(r,"email"), val(r,"resumeUrl"), val(r,"source"), lng(r,"jobOpeningId")));
            case "payroll" -> hrms.createPayroll(new PayrollRequest(lngRequired(r,"employeeId"), req(r,"month"), decimal(r,"basicSalary"), decimal(r,"hra"), decimal(r,"allowances"), decimal(r,"bonuses"), decimal(r,"deductions"), decimal(r,"pf"), decimal(r,"tax"), enumVal(PayrollStatus.class, val(r,"status"), PayrollStatus.PENDING)));
            case "leaves" -> hrms.applyLeave(new LeaveRequest(lngRequired(r,"employeeId"), req(r,"leaveType"), date(r,"startDate"), date(r,"endDate"), req(r,"reason")), actor);
            case "attendance" -> hrms.recordAttendance(lngRequired(r,"employeeId"), enumVal(AttendanceStatus.class, req(r,"status"), null), null, null);
            default -> throw new IllegalArgumentException("Unsupported module. Use employees, departments, assets, projects, tasks, performance, job-openings, candidates, payroll, leaves, or attendance");
        }
    }
    private static String val(Map<String,String> r, String key) { return r.getOrDefault(key, "").trim(); }
    private static String req(Map<String,String> r, String key) { String v=val(r,key); if(v.isBlank()) throw new IllegalArgumentException("Missing '"+key+"'"); return v; }
    private static Long lng(Map<String,String> r,String k){String v=val(r,k);return v.isBlank()?null:Long.valueOf(v);}
    private static Long lngRequired(Map<String,String> r,String k){Long v=lng(r,k);if(v==null) throw new IllegalArgumentException("Missing '"+k+"'");return v;}
    private static Integer integer(Map<String,String> r,String k){return Integer.valueOf(req(r,k));}
    private static Integer integerOrNull(Map<String,String> r,String k){String v=val(r,k);return v.isBlank()?null:Integer.valueOf(v);}
    private static BigDecimal decimal(Map<String,String> r,String k){return new BigDecimal(req(r,k));}
    private static BigDecimal decimalOrNull(Map<String,String> r,String k){String v=val(r,k);return v.isBlank()?null:new BigDecimal(v);}
    private static LocalDate date(Map<String,String> r,String k){return LocalDate.parse(req(r,k));}
    private static LocalDate dateOrNull(Map<String,String> r,String k){String v=val(r,k);return v.isBlank()?null:LocalDate.parse(v);}
    private static <T extends Enum<T>> T enumVal(Class<T> type,String value,T fallback){return value==null||value.isBlank()?fallback:Enum.valueOf(type,value.trim().toUpperCase(Locale.ROOT));}
    private static String readable(Exception e){return e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();}
}

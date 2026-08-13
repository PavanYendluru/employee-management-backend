package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.BulkImportResult;
import com.pavan.employeemanagement.service.BulkImportService;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** HR/Admin-only CSV import endpoint for all supported data-entry modules. */
@RestController
@RequestMapping("/api/admin/import")
public class BulkImportController {
    private final BulkImportService imports;
    public BulkImportController(BulkImportService imports) { this.imports = imports; }
    @PostMapping(value = "/{module}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BulkImportResult importFile(@PathVariable String module, @RequestPart("file") MultipartFile file, Authentication authentication) {
        return imports.importFile(module, file, authentication.getName());
    }

    @GetMapping(value = "/{module}/template", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<org.springframework.core.io.ByteArrayResource> template(@PathVariable String module) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + module + "-import-template.xlsx").body(imports.template(module));
    }
}

package com.pavan.employeemanagement.controller;

import com.pavan.employeemanagement.dto.Dtos.*;
import com.pavan.employeemanagement.service.HrmsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Manages the asset inventory and employee assignments. */
@RestController
@RequestMapping("/api")
public class AssetController {
    private final HrmsService hrmsService;

    /** Injects the central HRMS service. */
    public AssetController(HrmsService hrmsService) { this.hrmsService = hrmsService; }

    /** Lists all company assets. */
    @GetMapping("/assets") public List<AssetView> list() { return hrmsService.assets(); }
    /** Lists only the signed-in employee's active assignments. */
    @GetMapping("/me/assets") public List<AssetView> mine(Authentication authentication) { return hrmsService.myAssets(authentication.getName()); }
    /** Creates an inventory asset. */
    @PostMapping("/admin/assets") public AssetView create(@Valid @RequestBody AssetRequest request) { return hrmsService.createAsset(request); }
    /** Updates an asset's inventory details. */
    @PutMapping("/admin/assets/{id}") public AssetView update(@PathVariable Long id, @Valid @RequestBody AssetRequest request) { return hrmsService.updateAsset(id, request); }
    /** Deletes an asset record. */
    @DeleteMapping("/admin/assets/{id}") public void delete(@PathVariable Long id) { hrmsService.deleteAsset(id); }
    /** Assigns an available asset to an employee. */
    @PostMapping("/admin/assets/{id}/assign") public AssetView assign(@PathVariable Long id, @Valid @RequestBody AssignAssetRequest request) { return hrmsService.assign(id, request.employeeId()); }
    /** Closes the active asset assignment and marks it returned. */
    @PostMapping("/admin/assets/{id}/return") public AssetView returnAsset(@PathVariable Long id) { return hrmsService.returnAsset(id); }
}

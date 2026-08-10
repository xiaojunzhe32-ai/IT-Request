package com.itop.api.controller;

import com.itop.api.dto.RoutingRuleDTO;
import com.itop.api.service.AuditService;
import com.itop.api.service.RoutingRuleService;
import com.itop.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Routing Rule", description = "Request routing rule management APIs")
@RestController
@RequestMapping("/routing-rules")
@RequiredArgsConstructor
public class RoutingRuleController {

    private final RoutingRuleService routingRuleService;
    private final AuditService auditService;

    @Operation(summary = "Get all routing rules", description = "Retrieve all routing rules ordered by priority")
    @GetMapping
    @PreAuthorize("@securityUtils.hasPermission('routing:read')")
    public ResponseEntity<ApiResponse<List<RoutingRuleDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(routingRuleService.listAll()));
    }

    @Operation(summary = "Get routing rule by ID", description = "Retrieve a single routing rule by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('routing:read')")
    public ResponseEntity<ApiResponse<RoutingRuleDTO>> getById(@PathVariable("id") Long id) {
        RoutingRuleDTO dto = routingRuleService.getById(id);
        if (dto == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Routing rule not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @Operation(summary = "Create routing rule", description = "Create a new routing rule")
    @PostMapping
    @PreAuthorize("@securityUtils.hasPermission('routing:write')")
    public ResponseEntity<ApiResponse<RoutingRuleDTO>> create(@Valid @RequestBody RoutingRuleDTO dto) {
        if (!routingRuleService.teamExists(dto.getTeamId())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Target team does not exist"));
        }
        RoutingRuleDTO created = routingRuleService.create(dto);
        auditService.logCreate("RoutingRule", created.getId(), "Created routing rule: " + created.getName());
        return ResponseEntity.ok(ApiResponse.success("Routing rule created", created));
    }

    @Operation(summary = "Update routing rule", description = "Update an existing routing rule")
    @PutMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('routing:write')")
    public ResponseEntity<ApiResponse<RoutingRuleDTO>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody RoutingRuleDTO dto) {
        if (dto.getTeamId() != null && !routingRuleService.teamExists(dto.getTeamId())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Target team does not exist"));
        }
        RoutingRuleDTO updated = routingRuleService.update(id, dto);
        if (updated == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Routing rule not found"));
        }
        auditService.logUpdate("RoutingRule", updated.getId(), "name", null, updated.getName(), "Updated routing rule");
        return ResponseEntity.ok(ApiResponse.success("Routing rule updated", updated));
    }

    @Operation(summary = "Delete routing rule", description = "Delete a routing rule by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('routing:write')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        try {
            if (!routingRuleService.delete(id)) {
                return ResponseEntity.ok(ApiResponse.error(404, "Routing rule not found"));
            }
            auditService.logDelete("RoutingRule", id, "Deleted routing rule");
            return ResponseEntity.ok(ApiResponse.success("Routing rule deleted", null));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(ApiResponse.error(400, ex.getMessage()));
        }
    }

    @Operation(summary = "Enable or disable a routing rule", description = "Toggle the enabled flag of a routing rule")
    @PatchMapping("/{id}/enabled")
    @PreAuthorize("@securityUtils.hasPermission('routing:write')")
    public ResponseEntity<ApiResponse<RoutingRuleDTO>> setEnabled(
            @PathVariable("id") Long id,
            @RequestBody EnabledRequest request) {
        RoutingRuleDTO updated = routingRuleService.setEnabled(id, request.getEnabled());
        if (updated == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Routing rule not found"));
        }
        auditService.logUpdate("RoutingRule", updated.getId(), "enabled", null,
                String.valueOf(updated.getEnabled()), "Toggled routing rule");
        return ResponseEntity.ok(ApiResponse.success("Routing rule updated", updated));
    }

    @Operation(summary = "Reorder routing rules", description = "Reorder routing rules by passing an ordered list of rule IDs")
    @PutMapping("/reorder")
    @PreAuthorize("@securityUtils.hasPermission('routing:write')")
    public ResponseEntity<ApiResponse<Void>> reorder(@RequestBody ReorderRequest request) {
        routingRuleService.reorder(request.getOrderedIds());
        return ResponseEntity.ok(ApiResponse.success("Routing rules reordered", null));
    }

    @lombok.Data
    public static class EnabledRequest {
        private Boolean enabled;
    }

    @Data
    public static class ReorderRequest {
        private List<Long> orderedIds;
    }
}

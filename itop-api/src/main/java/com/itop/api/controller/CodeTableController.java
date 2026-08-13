package com.itop.api.controller;

import com.itop.api.dto.CodeTableItemDTO;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.CodeTableItem;
import com.itop.core.repository.CodeTableItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Code Table", description = "Workflow dropdown code table management APIs")
@RestController
@RequestMapping("/code-tables")
@RequiredArgsConstructor
public class CodeTableController {

    private final CodeTableItemRepository repository;

    @Operation(summary = "List code table items", description = "Retrieve items from a named code table")
    @GetMapping("/{tableCode}/items")
    @PreAuthorize("@securityUtils.hasAnyPermission('request:create','request:read','routing:read','org:read') or @securityUtils.isAdmin()")
    public ResponseEntity<ApiResponse<List<CodeTableItemDTO>>> list(
            @PathVariable("tableCode") String tableCode,
            @RequestParam(name = "status", required = false) String status) {
        String normalized = normalizeTableCode(tableCode);
        List<CodeTableItemDTO> items = (status != null && !status.isBlank()
                ? repository.findByTableCodeAndStatusIgnoreCaseOrderBySortOrderAscNameAsc(normalized, status)
                : repository.findByTableCodeOrderBySortOrderAscNameAsc(normalized))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @Operation(summary = "Create code table item", description = "Create a new item in a code table")
    @PostMapping("/{tableCode}/items")
    @PreAuthorize("@securityUtils.isAdmin()")
    public ResponseEntity<ApiResponse<CodeTableItemDTO>> create(
            @PathVariable("tableCode") String tableCode,
            @Valid @RequestBody CodeTableItemDTO dto) {
        String normalized = normalizeTableCode(tableCode);
        if (!normalized.equalsIgnoreCase(normalizeTableCode(dto.getTableCode()))) {
            return ResponseEntity.ok(ApiResponse.error(400, "Table code mismatch"));
        }
        if (repository.findByTableCodeAndCodeIgnoreCase(normalized, normalizeCode(dto.getCode())).isPresent()) {
            return ResponseEntity.ok(ApiResponse.error(400, "Code already exists"));
        }
        CodeTableItem item = toEntity(dto);
        item.setTableCode(normalized);
        item = repository.save(item);
        return ResponseEntity.ok(ApiResponse.success("Code table item created", toDTO(item)));
    }

    @Operation(summary = "Update code table item", description = "Update an existing code table item")
    @PutMapping("/{tableCode}/items/{id}")
    @PreAuthorize("@securityUtils.isAdmin()")
    public ResponseEntity<ApiResponse<CodeTableItemDTO>> update(
            @PathVariable("tableCode") String tableCode,
            @PathVariable("id") Long id,
            @Valid @RequestBody CodeTableItemDTO dto) {
        String normalized = normalizeTableCode(tableCode);
        CodeTableItem existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Code table item not found"));
        }
        if (!normalized.equalsIgnoreCase(existing.getTableCode())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Table code mismatch"));
        }
        if (dto.getCode() != null) {
            Optional<CodeTableItem> duplicate = repository.findByTableCodeAndCodeIgnoreCase(normalized, normalizeCode(dto.getCode()));
            if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                return ResponseEntity.ok(ApiResponse.error(400, "Code already exists"));
            }
        }
        updateEntity(existing, dto);
        existing.setTableCode(normalized);
        CodeTableItem saved = repository.save(existing);
        return ResponseEntity.ok(ApiResponse.success("Code table item updated", toDTO(saved)));
    }

    @Operation(summary = "Delete code table item", description = "Delete a code table item by ID")
    @DeleteMapping("/{tableCode}/items/{id}")
    @PreAuthorize("@securityUtils.isAdmin()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("tableCode") String tableCode,
            @PathVariable("id") Long id) {
        CodeTableItem item = repository.findById(id).orElse(null);
        if (item == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Code table item not found"));
        }
        if (!normalizeTableCode(tableCode).equalsIgnoreCase(item.getTableCode())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Table code mismatch"));
        }
        repository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Code table item deleted", null));
    }

    private CodeTableItemDTO toDTO(CodeTableItem item) {
        return CodeTableItemDTO.builder()
                .id(item.getId())
                .tableCode(item.getTableCode())
                .code(item.getCode())
                .name(item.getName())
                .status(item.getStatus())
                .description(item.getDescription())
                .sortOrder(item.getSortOrder())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private CodeTableItem toEntity(CodeTableItemDTO dto) {
        CodeTableItem item = new CodeTableItem();
        updateEntity(item, dto);
        return item;
    }

    private void updateEntity(CodeTableItem item, CodeTableItemDTO dto) {
        item.setName(dto.getName());
        item.setCode(normalizeCode(dto.getCode()));
        item.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        item.setDescription(dto.getDescription());
        item.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
    }

    private String normalizeTableCode(String tableCode) {
        return tableCode == null ? null : tableCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().replaceAll("\\s+", "_").toUpperCase(Locale.ROOT);
    }
}

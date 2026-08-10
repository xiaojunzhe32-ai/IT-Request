package com.itop.api.controller;

import com.itop.common.dto.ApiResponse;
import com.itop.api.dto.CIDTO;
import com.itop.api.dto.PageResponse;
import com.itop.core.entity.ConfigurationItem;
import com.itop.core.repository.ConfigurationItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Configuration Item", description = "Configuration Item management APIs")
@RestController
@RequestMapping("/cis")
@RequiredArgsConstructor
public class CIController {

    private final ConfigurationItemRepository ciRepository;

    @Operation(summary = "Get all configuration items", description = "Retrieve a paginated list of all CIs")
    @GetMapping
    @PreAuthorize("@securityUtils.hasPermission('ci:read')")
    public ResponseEntity<ApiResponse<PageResponse<CIDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "name") String sort,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "status", required = false) String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<ConfigurationItem> ciPage;

        if (type != null && !type.isEmpty()) {
            ciPage = ciRepository.findByFinalClass(type, pageable);
        } else if (status != null && !status.isEmpty()) {
            ciPage = ciRepository.findByStatus(status, pageable);
        } else {
            ciPage = ciRepository.findAll(pageable);
        }

        List<CIDTO> dtos = ciPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<CIDTO> response = PageResponse.of(dtos, page, size, ciPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get CI by ID", description = "Retrieve a single CI by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('ci:read')")
    public ResponseEntity<ApiResponse<CIDTO>> getById(@PathVariable("id") Long id) {
        return ciRepository.findById(id)
                .map(ci -> ResponseEntity.ok(ApiResponse.success(toDTO(ci))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Configuration Item not found")));
    }

    @Operation(summary = "Get CI types", description = "Retrieve list of all CI types")
    @GetMapping("/types")
    @PreAuthorize("@securityUtils.hasPermission('ci:read')")
    public ResponseEntity<ApiResponse<List<String>>> getTypes() {
        List<String> types = ciRepository.findDistinctFinalClasses();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    private CIDTO toDTO(ConfigurationItem ci) {
        return CIDTO.builder()
                .id(ci.getId())
                .name(ci.getName())
                .organizationId(ci.getOrganizationId())
                .status(ci.getStatus())
                .description(ci.getDescription())
                .finalClass(ci.getFinalClass())
                .ciType(ci.getFinalClass())
                .assetNumber(ci.getAssetNumber())
                .move2Production(ci.getMove2Production())
                .obsolescenceDate(ci.getObsolescenceDate())
                .businessCriticity(ci.getBusinessCriticity())
                .redundancy(ci.getRedundancy())
                .createdAt(ci.getCreatedAt())
                .updatedAt(ci.getUpdatedAt())
                .build();
    }
}
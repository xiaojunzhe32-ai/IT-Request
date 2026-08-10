package com.itop.api.controller;

import com.itop.common.dto.ApiResponse;
import com.itop.api.dto.OrganizationDTO;
import com.itop.api.dto.PageResponse;
import com.itop.api.security.SecurityUtils;
import com.itop.api.service.AuditService;
import com.itop.core.entity.Organization;
import com.itop.core.repository.OrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Organization", description = "Organization management APIs")
@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationRepository organizationRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Get all organizations", description = "Retrieve a paginated list of organizations")
    @GetMapping
    @PreAuthorize("@securityUtils.hasPermission('org:read')")
    public ResponseEntity<ApiResponse<PageResponse<OrganizationDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "name") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Set<Long> accessibleOrgIds = securityUtils.getAccessibleOrgIds();
        Specification<Organization> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (accessibleOrgIds != null) {
                if (accessibleOrgIds.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("id").in(accessibleOrgIds));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Organization> orgPage = organizationRepository.findAll(spec, pageable);

        List<OrganizationDTO> dtos = orgPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<OrganizationDTO> response = PageResponse.of(dtos, page, size, orgPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get organization by ID", description = "Retrieve a single organization by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('org:read')")
    public ResponseEntity<ApiResponse<OrganizationDTO>> getById(@PathVariable("id") Long id) {
        return organizationRepository.findById(id)
                .filter(this::canAccess)
                .map(org -> ResponseEntity.ok(ApiResponse.success(toDTO(org))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Organization not found")));
    }

    @Operation(summary = "Create organization", description = "Create a new organization")
    @PostMapping
    @PreAuthorize("@securityUtils.hasPermission('org:write')")
    public ResponseEntity<ApiResponse<OrganizationDTO>> create(@Valid @RequestBody OrganizationDTO dto) {
        Organization org = toEntity(dto);
        org = organizationRepository.save(org);
        auditService.logCreate("Organization", org.getId(), "Created organization: " + org.getName());
        return ResponseEntity.ok(ApiResponse.success("Organization created", toDTO(org)));
    }

    @Operation(summary = "Update organization", description = "Update an existing organization")
    @PutMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('org:write')")
    public ResponseEntity<ApiResponse<OrganizationDTO>> update(@PathVariable("id") Long id, @Valid @RequestBody OrganizationDTO dto) {
        return organizationRepository.findById(id)
                .map(existing -> {
                    updateEntity(existing, dto);
                    Organization saved = organizationRepository.save(existing);
                    auditService.logUpdate("Organization", saved.getId(), "name", null, saved.getName(), "Updated organization");
                    return ResponseEntity.ok(ApiResponse.success("Organization updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Organization not found")));
    }

    @Operation(summary = "Delete organization", description = "Delete an organization by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('org:write')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        if (!organizationRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "Organization not found"));
        }
        auditService.logDelete("Organization", id, "Deleted organization");
        organizationRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Organization deleted", null));
    }

    private OrganizationDTO toDTO(Organization org) {
        return OrganizationDTO.builder()
                .id(org.getId())
                .name(org.getName())
                .code(org.getCode())
                .status(org.getStatus())
                .description(org.getDescription())
                .parentId(org.getParentId())
                .type(org.getType())
                .address(org.getAddress())
                .phone(org.getPhone())
                .email(org.getEmail())
                .website(org.getWebsite())
                .build();
    }

    private boolean canAccess(Organization org) {
        Set<Long> accessibleOrgIds = securityUtils.getAccessibleOrgIds();
        return accessibleOrgIds == null || accessibleOrgIds.contains(org.getId());
    }

    private Organization toEntity(OrganizationDTO dto) {
        Organization org = new Organization();
        updateEntity(org, dto);
        return org;
    }

    private void updateEntity(Organization org, OrganizationDTO dto) {
        org.setName(dto.getName());
        org.setCode(dto.getCode());
        org.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        org.setDescription(dto.getDescription());
        org.setParentId(dto.getParentId());
        org.setType(dto.getType() != null ? dto.getType() : "company");
        org.setAddress(dto.getAddress());
        org.setPhone(dto.getPhone());
        org.setEmail(dto.getEmail());
        org.setWebsite(dto.getWebsite());
    }
}

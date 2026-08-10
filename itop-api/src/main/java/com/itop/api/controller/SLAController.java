package com.itop.api.controller;

import com.itop.common.dto.ApiResponse;
import com.itop.api.dto.SLADTO;
import com.itop.api.dto.PageResponse;
import com.itop.core.entity.SLA;
import com.itop.core.entity.Organization;
import com.itop.core.repository.SLARepository;
import com.itop.core.repository.OrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "SLA", description = "Service Level Agreement management APIs")
@RestController
@RequestMapping("/slas")
@RequiredArgsConstructor
public class SLAController {

    private final SLARepository slaRepository;
    private final OrganizationRepository organizationRepository;

    @Operation(summary = "Get all SLAs", description = "Retrieve a paginated list of SLAs")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SLADTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "name") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<SLA> slaPage = slaRepository.findAll(pageable);

        List<SLADTO> dtos = slaPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<SLADTO> response = PageResponse.of(dtos, page, size, slaPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get SLA by ID", description = "Retrieve a single SLA by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SLADTO>> getById(@PathVariable("id") Long id) {
        return slaRepository.findById(id)
                .map(sla -> ResponseEntity.ok(ApiResponse.success(toDTO(sla))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "SLA not found")));
    }

    @Operation(summary = "Get default SLA", description = "Retrieve the default SLA")
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<SLADTO>> getDefault() {
        return slaRepository.findByIsDefaultTrue()
                .map(sla -> ResponseEntity.ok(ApiResponse.success(toDTO(sla))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "No default SLA found")));
    }

    @Operation(summary = "Create SLA", description = "Create a new SLA")
    @PostMapping
    public ResponseEntity<ApiResponse<SLADTO>> create(@Valid @RequestBody SLADTO dto) {
        if (slaRepository.existsByCode(dto.getCode())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Code already exists"));
        }

        Organization org = null;
        if (dto.getOrgId() != null) {
            org = organizationRepository.findById(dto.getOrgId()).orElse(null);
        }

        SLA sla = toEntity(dto, org);
        sla = slaRepository.save(sla);
        return ResponseEntity.ok(ApiResponse.success("SLA created", toDTO(sla)));
    }

    @Operation(summary = "Update SLA", description = "Update an existing SLA")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SLADTO>> update(@PathVariable("id") Long id, @Valid @RequestBody SLADTO dto) {
        return slaRepository.findById(id)
                .map(existing -> {
                    Organization org = null;
                    if (dto.getOrgId() != null) {
                        org = organizationRepository.findById(dto.getOrgId()).orElse(null);
                    }

                    updateEntity(existing, dto, org);
                    SLA saved = slaRepository.save(existing);
                    return ResponseEntity.ok(ApiResponse.success("SLA updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "SLA not found")));
    }

    @Operation(summary = "Delete SLA", description = "Delete an SLA by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        if (!slaRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "SLA not found"));
        }
        slaRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("SLA deleted", null));
    }

    private SLADTO toDTO(SLA sla) {
        return SLADTO.builder()
                .id(sla.getId())
                .name(sla.getName())
                .code(sla.getCode())
                .status(sla.getStatus())
                .description(sla.getDescription())
                .orgId(sla.getOrganization() != null ? sla.getOrganization().getId() : null)
                .orgName(sla.getOrganization() != null ? sla.getOrganization().getName() : null)
                .ttoHours(sla.getTtoHours())
                .ttrHours(sla.getTtrHours())
                .priority(sla.getPriority())
                .calendarId(sla.getCalendarId())
                .isDefault(sla.getIsDefault())
                .build();
    }

    private SLA toEntity(SLADTO dto, Organization org) {
        SLA sla = new SLA();
        updateEntity(sla, dto, org);
        return sla;
    }

    private void updateEntity(SLA sla, SLADTO dto, Organization org) {
        sla.setName(dto.getName());
        sla.setCode(dto.getCode());
        sla.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        sla.setDescription(dto.getDescription());
        sla.setOrganization(org);
        sla.setTtoHours(dto.getTtoHours() != null ? dto.getTtoHours() : 4);
        sla.setTtrHours(dto.getTtrHours() != null ? dto.getTtrHours() : 8);
        sla.setPriority(dto.getPriority() != null ? dto.getPriority() : "medium");
        sla.setCalendarId(dto.getCalendarId());
        sla.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
    }
}
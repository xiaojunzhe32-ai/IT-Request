package com.itop.api.controller;

import com.itop.common.dto.ApiResponse;
import com.itop.api.dto.ServiceFamilyDTO;
import com.itop.api.dto.PageResponse;
import com.itop.core.entity.ServiceFamily;
import com.itop.core.repository.ServiceFamilyRepository;
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

@Tag(name = "Service Family", description = "Service family management APIs")
@RestController
@RequestMapping("/service-families")
@RequiredArgsConstructor
public class ServiceFamilyController {

    private final ServiceFamilyRepository serviceFamilyRepository;

    @Operation(summary = "Get all service families", description = "Retrieve a paginated list of service families")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ServiceFamilyDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "sortOrder") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<ServiceFamily> familyPage = serviceFamilyRepository.findAll(pageable);

        List<ServiceFamilyDTO> dtos = familyPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<ServiceFamilyDTO> response = PageResponse.of(dtos, page, size, familyPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get service family by ID", description = "Retrieve a single service family by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceFamilyDTO>> getById(@PathVariable("id") Long id) {
        return serviceFamilyRepository.findById(id)
                .map(family -> ResponseEntity.ok(ApiResponse.success(toDTO(family))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Service family not found")));
    }

    @Operation(summary = "Create service family", description = "Create a new service family")
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceFamilyDTO>> create(@Valid @RequestBody ServiceFamilyDTO dto) {
        if (serviceFamilyRepository.existsByCode(dto.getCode())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Code already exists"));
        }
        ServiceFamily family = toEntity(dto);
        family = serviceFamilyRepository.save(family);
        return ResponseEntity.ok(ApiResponse.success("Service family created", toDTO(family)));
    }

    @Operation(summary = "Update service family", description = "Update an existing service family")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceFamilyDTO>> update(@PathVariable("id") Long id, @Valid @RequestBody ServiceFamilyDTO dto) {
        return serviceFamilyRepository.findById(id)
                .map(existing -> {
                    updateEntity(existing, dto);
                    ServiceFamily saved = serviceFamilyRepository.save(existing);
                    return ResponseEntity.ok(ApiResponse.success("Service family updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Service family not found")));
    }

    @Operation(summary = "Delete service family", description = "Delete a service family by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        if (!serviceFamilyRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "Service family not found"));
        }
        serviceFamilyRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Service family deleted", null));
    }

    private ServiceFamilyDTO toDTO(ServiceFamily family) {
        return ServiceFamilyDTO.builder()
                .id(family.getId())
                .name(family.getName())
                .code(family.getCode())
                .status(family.getStatus())
                .description(family.getDescription())
                .icon(family.getIcon())
                .sortOrder(family.getSortOrder())
                .build();
    }

    private ServiceFamily toEntity(ServiceFamilyDTO dto) {
        ServiceFamily family = new ServiceFamily();
        updateEntity(family, dto);
        return family;
    }

    private void updateEntity(ServiceFamily family, ServiceFamilyDTO dto) {
        family.setName(dto.getName());
        family.setCode(dto.getCode());
        family.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        family.setDescription(dto.getDescription());
        family.setIcon(dto.getIcon());
        family.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
    }
}
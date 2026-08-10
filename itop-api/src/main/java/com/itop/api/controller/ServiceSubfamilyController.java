package com.itop.api.controller;

import com.itop.common.dto.ApiResponse;
import com.itop.api.dto.ServiceSubfamilyDTO;
import com.itop.api.dto.PageResponse;
import com.itop.core.entity.ServiceSubfamily;
import com.itop.core.entity.ServiceFamily;
import com.itop.core.repository.ServiceSubfamilyRepository;
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

@Tag(name = "Service Subfamily", description = "Service subfamily management APIs")
@RestController
@RequestMapping("/service-subfamilies")
@RequiredArgsConstructor
public class ServiceSubfamilyController {

    private final ServiceSubfamilyRepository serviceSubfamilyRepository;
    private final ServiceFamilyRepository serviceFamilyRepository;

    @Operation(summary = "Get all service subfamilies", description = "Retrieve a paginated list of service subfamilies")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ServiceSubfamilyDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "sortOrder") String sort,
            @RequestParam(name = "familyId", required = false) Long familyId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<ServiceSubfamily> subfamilyPage;

        if (familyId != null) {
            subfamilyPage = serviceSubfamilyRepository.findByFamilyId(familyId, pageable);
        } else {
            subfamilyPage = serviceSubfamilyRepository.findAll(pageable);
        }

        List<ServiceSubfamilyDTO> dtos = subfamilyPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<ServiceSubfamilyDTO> response = PageResponse.of(dtos, page, size, subfamilyPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get service subfamily by ID", description = "Retrieve a single service subfamily by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceSubfamilyDTO>> getById(@PathVariable("id") Long id) {
        return serviceSubfamilyRepository.findById(id)
                .map(subfamily -> ResponseEntity.ok(ApiResponse.success(toDTO(subfamily))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Service subfamily not found")));
    }

    @Operation(summary = "Create service subfamily", description = "Create a new service subfamily")
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceSubfamilyDTO>> create(@Valid @RequestBody ServiceSubfamilyDTO dto) {
        if (serviceSubfamilyRepository.existsByCode(dto.getCode())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Code already exists"));
        }

        ServiceFamily family = serviceFamilyRepository.findById(dto.getFamilyId())
                .orElse(null);
        if (family == null) {
            return ResponseEntity.ok(ApiResponse.error(400, "Service family not found"));
        }

        ServiceSubfamily subfamily = toEntity(dto, family);
        subfamily = serviceSubfamilyRepository.save(subfamily);
        return ResponseEntity.ok(ApiResponse.success("Service subfamily created", toDTO(subfamily)));
    }

    @Operation(summary = "Update service subfamily", description = "Update an existing service subfamily")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceSubfamilyDTO>> update(@PathVariable("id") Long id, @Valid @RequestBody ServiceSubfamilyDTO dto) {
        return serviceSubfamilyRepository.findById(id)
                .map(existing -> {
                    ServiceFamily family = serviceFamilyRepository.findById(dto.getFamilyId())
                            .orElse(null);
                    if (family == null) {
                        return ResponseEntity.<ApiResponse<ServiceSubfamilyDTO>>ok(ApiResponse.error(400, "Service family not found"));
                    }
                    updateEntity(existing, dto, family);
                    ServiceSubfamily saved = serviceSubfamilyRepository.save(existing);
                    return ResponseEntity.ok(ApiResponse.success("Service subfamily updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Service subfamily not found")));
    }

    @Operation(summary = "Delete service subfamily", description = "Delete a service subfamily by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        if (!serviceSubfamilyRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "Service subfamily not found"));
        }
        serviceSubfamilyRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Service subfamily deleted", null));
    }

    private ServiceSubfamilyDTO toDTO(ServiceSubfamily subfamily) {
        return ServiceSubfamilyDTO.builder()
                .id(subfamily.getId())
                .name(subfamily.getName())
                .code(subfamily.getCode())
                .status(subfamily.getStatus())
                .description(subfamily.getDescription())
                .familyId(subfamily.getFamily() != null ? subfamily.getFamily().getId() : null)
                .familyName(subfamily.getFamily() != null ? subfamily.getFamily().getName() : null)
                .sortOrder(subfamily.getSortOrder())
                .build();
    }

    private ServiceSubfamily toEntity(ServiceSubfamilyDTO dto, ServiceFamily family) {
        ServiceSubfamily subfamily = new ServiceSubfamily();
        updateEntity(subfamily, dto, family);
        return subfamily;
    }

    private void updateEntity(ServiceSubfamily subfamily, ServiceSubfamilyDTO dto, ServiceFamily family) {
        subfamily.setName(dto.getName());
        subfamily.setCode(dto.getCode());
        subfamily.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        subfamily.setDescription(dto.getDescription());
        subfamily.setFamily(family);
        subfamily.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
    }
}
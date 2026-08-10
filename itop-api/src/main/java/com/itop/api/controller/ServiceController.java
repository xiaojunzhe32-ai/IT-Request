package com.itop.api.controller;

import com.itop.common.dto.ApiResponse;
import com.itop.api.dto.ServiceDTO;
import com.itop.api.dto.PageResponse;
import com.itop.core.entity.Service;
import com.itop.core.entity.ServiceSubfamily;
import com.itop.core.entity.Organization;
import com.itop.core.entity.SLA;
import com.itop.core.repository.ServiceRepository;
import com.itop.core.repository.ServiceSubfamilyRepository;
import com.itop.core.repository.OrganizationRepository;
import com.itop.core.repository.SLARepository;
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

@Tag(name = "Service", description = "Service management APIs")
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final ServiceSubfamilyRepository serviceSubfamilyRepository;
    private final OrganizationRepository organizationRepository;
    private final SLARepository slaRepository;

    @Operation(summary = "Get all services", description = "Retrieve a paginated list of services")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ServiceDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "sortOrder") String sort,
            @RequestParam(name = "subfamilyId", required = false) Long subfamilyId,
            @RequestParam(name = "serviceType", required = false) String serviceType) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<Service> servicePage;

        if (subfamilyId != null) {
            servicePage = serviceRepository.findBySubfamilyId(subfamilyId, pageable);
        } else if (serviceType != null) {
            servicePage = serviceRepository.findByServiceType(serviceType, pageable);
        } else {
            servicePage = serviceRepository.findAll(pageable);
        }

        List<ServiceDTO> dtos = servicePage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<ServiceDTO> response = PageResponse.of(dtos, page, size, servicePage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get service by ID", description = "Retrieve a single service by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDTO>> getById(@PathVariable("id") Long id) {
        return serviceRepository.findById(id)
                .map(service -> ResponseEntity.ok(ApiResponse.success(toDTO(service))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Service not found")));
    }

    @Operation(summary = "Create service", description = "Create a new service")
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceDTO>> create(@Valid @RequestBody ServiceDTO dto) {
        if (serviceRepository.existsByCode(dto.getCode())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Code already exists"));
        }

        ServiceSubfamily subfamily = serviceSubfamilyRepository.findById(dto.getSubfamilyId())
                .orElse(null);
        if (subfamily == null) {
            return ResponseEntity.ok(ApiResponse.error(400, "Service subfamily not found"));
        }

        Organization org = null;
        if (dto.getOrgId() != null) {
            org = organizationRepository.findById(dto.getOrgId()).orElse(null);
        }

        SLA sla = null;
        if (dto.getSlaId() != null) {
            sla = slaRepository.findById(dto.getSlaId()).orElse(null);
        }

        Service service = toEntity(dto, subfamily, org, sla);
        service = serviceRepository.save(service);
        return ResponseEntity.ok(ApiResponse.success("Service created", toDTO(service)));
    }

    @Operation(summary = "Update service", description = "Update an existing service")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDTO>> update(@PathVariable("id") Long id, @Valid @RequestBody ServiceDTO dto) {
        return serviceRepository.findById(id)
                .map(existing -> {
                    ServiceSubfamily subfamily = serviceSubfamilyRepository.findById(dto.getSubfamilyId())
                            .orElse(null);
                    if (subfamily == null) {
                        return ResponseEntity.<ApiResponse<ServiceDTO>>ok(ApiResponse.error(400, "Service subfamily not found"));
                    }

                    Organization org = null;
                    if (dto.getOrgId() != null) {
                        org = organizationRepository.findById(dto.getOrgId()).orElse(null);
                    }

                    SLA sla = null;
                    if (dto.getSlaId() != null) {
                        sla = slaRepository.findById(dto.getSlaId()).orElse(null);
                    }

                    updateEntity(existing, dto, subfamily, org, sla);
                    Service saved = serviceRepository.save(existing);
                    return ResponseEntity.ok(ApiResponse.success("Service updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Service not found")));
    }

    @Operation(summary = "Delete service", description = "Delete a service by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        if (!serviceRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "Service not found"));
        }
        serviceRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Service deleted", null));
    }

    private ServiceDTO toDTO(Service service) {
        return ServiceDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .code(service.getCode())
                .status(service.getStatus())
                .description(service.getDescription())
                .subfamilyId(service.getSubfamily() != null ? service.getSubfamily().getId() : null)
                .subfamilyName(service.getSubfamily() != null ? service.getSubfamily().getName() : null)
                .orgId(service.getOrganization() != null ? service.getOrganization().getId() : null)
                .orgName(service.getOrganization() != null ? service.getOrganization().getName() : null)
                .serviceType(service.getServiceType())
                .slaId(service.getSla() != null ? service.getSla().getId() : null)
                .slaName(service.getSla() != null ? service.getSla().getName() : null)
                .sortOrder(service.getSortOrder())
                .build();
    }

    private Service toEntity(ServiceDTO dto, ServiceSubfamily subfamily, Organization org, SLA sla) {
        Service service = new Service();
        updateEntity(service, dto, subfamily, org, sla);
        return service;
    }

    private void updateEntity(Service service, ServiceDTO dto, ServiceSubfamily subfamily, Organization org, SLA sla) {
        service.setName(dto.getName());
        service.setCode(dto.getCode());
        service.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        service.setDescription(dto.getDescription());
        service.setSubfamily(subfamily);
        service.setOrganization(org);
        service.setServiceType(dto.getServiceType() != null ? dto.getServiceType() : "USER_REQUEST");
        service.setSla(sla);
        service.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
    }
}
package com.itop.api.controller;

import com.itop.common.dto.ApiResponse;
import com.itop.api.dto.ServerDTO;
import com.itop.api.dto.PageResponse;
import com.itop.api.service.AuditService;
import com.itop.core.entity.Server;
import com.itop.core.repository.ConfigurationItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "Server", description = "Server management APIs")
@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ConfigurationItemRepository ciRepository;
    private final AuditService auditService;

    @Operation(summary = "Get all servers", description = "Retrieve a paginated list of servers")
    @GetMapping
    @PreAuthorize("@securityUtils.hasPermission('ci:read')")
    public ResponseEntity<ApiResponse<PageResponse<ServerDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "name") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        // TODO: Filter by Server class type
        Page<Server> serverPage = ciRepository.findByFinalClass("Server", pageable)
                .map(ci -> (Server) ci);

        List<ServerDTO> dtos = serverPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<ServerDTO> response = PageResponse.of(dtos, page, size, serverPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get server by ID", description = "Retrieve a single server by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('ci:read')")
    public ResponseEntity<ApiResponse<ServerDTO>> getById(@PathVariable("id") Long id) {
        return ciRepository.findById(id)
                .filter(ci -> ci instanceof Server)
                .map(ci -> ResponseEntity.ok(ApiResponse.success(toDTO((Server) ci))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Server not found")));
    }

    @Operation(summary = "Create server", description = "Create a new server")
    @PostMapping
    @PreAuthorize("@securityUtils.hasPermission('ci:write')")
    public ResponseEntity<ApiResponse<ServerDTO>> create(@Valid @RequestBody ServerDTO dto) {
        Server server = toEntity(dto);
        server = (Server) ciRepository.save(server);
        auditService.logCreate("Server", server.getId(), "Created server: " + server.getName());
        return ResponseEntity.ok(ApiResponse.success("Server created", toDTO(server)));
    }

    @Operation(summary = "Update server", description = "Update an existing server")
    @PutMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('ci:write')")
    public ResponseEntity<ApiResponse<ServerDTO>> update(@PathVariable("id") Long id, @Valid @RequestBody ServerDTO dto) {
        return ciRepository.findById(id)
                .filter(ci -> ci instanceof Server)
                .map(ci -> {
                    Server existing = (Server) ci;
                    updateEntity(existing, dto);
                    Server saved = (Server) ciRepository.save(existing);
                    auditService.logUpdate("Server", saved.getId(), "attributes", null, null, "Updated server: " + saved.getName());
                    return ResponseEntity.ok(ApiResponse.success("Server updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Server not found")));
    }

    @Operation(summary = "Delete server", description = "Delete a server by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('ci:delete')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        if (!ciRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "Server not found"));
        }
        auditService.logDelete("Server", id, "Deleted server");
        ciRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Server deleted", null));
    }

    private ServerDTO toDTO(Server server) {
        return ServerDTO.builder()
                .id(server.getId())
                .name(server.getName())
                .organizationId(server.getOrganizationId())
                .status(server.getStatus())
                .description(server.getDescription())
                .assetNumber(server.getAssetNumber())
                .brandName(server.getBrandName())
                .modelName(server.getModelName())
                .serialNumber(server.getSerialNumber())
                .cpu(server.getCpu())
                .ram(server.getRam())
                .disk(server.getDisk())
                .osFamily(server.getOsFamily())
                .osVersion(server.getOsVersion())
                .ipAddress(server.getIpAddress())
                .macAddress(server.getMacAddress())
                .managementIp(server.getManagementIp())
                .isVirtual(server.getIsVirtual())
                .serverType(server.getServerType() != null ? server.getServerType().name() : null)
                .move2Production(server.getMove2Production())
                .obsolescenceDate(server.getObsolescenceDate())
                .businessCriticity(server.getBusinessCriticity())
                .build();
    }

    private Server toEntity(ServerDTO dto) {
        Server server = new Server(dto.getName(), dto.getOrganizationId());
        updateEntity(server, dto);
        return server;
    }

    private void updateEntity(Server server, ServerDTO dto) {
        server.setAssetNumber(dto.getAssetNumber());
        server.setBrandName(dto.getBrandName());
        server.setModelName(dto.getModelName());
        server.setSerialNumber(dto.getSerialNumber());
        server.setCpu(dto.getCpu());
        server.setRam(dto.getRam());
        server.setDisk(dto.getDisk());
        server.setOsFamily(dto.getOsFamily());
        server.setOsVersion(dto.getOsVersion());
        server.setIpAddress(dto.getIpAddress());
        server.setMacAddress(dto.getMacAddress());
        server.setManagementIp(dto.getManagementIp());
        server.setIsVirtual(dto.getIsVirtual() != null ? dto.getIsVirtual() : false);
        if (dto.getServerType() != null) {
            server.setServerType(Server.ServerType.valueOf(dto.getServerType()));
        }
        server.setMove2Production(dto.getMove2Production());
        server.setObsolescenceDate(dto.getObsolescenceDate());
        server.setBusinessCriticity(dto.getBusinessCriticity());
        server.setDescription(dto.getDescription());
        server.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
    }
}

package com.itop.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itop.common.dto.ApiResponse;
import com.itop.api.dto.PageResponse;
import com.itop.api.dto.RoleDTO;
import com.itop.core.entity.Role;
import com.itop.core.repository.RoleRepository;
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

@Tag(name = "Role", description = "Role management APIs")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Get all roles", description = "Retrieve a paginated list of roles")
    @GetMapping
    @PreAuthorize("@securityUtils.hasAnyPermission('role:read', 'user:read')")
    public ResponseEntity<ApiResponse<PageResponse<RoleDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "name") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<Role> rolePage = roleRepository.findAll(pageable);

        List<RoleDTO> dtos = rolePage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<RoleDTO> response = PageResponse.of(dtos, page, size, rolePage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get role by ID", description = "Retrieve a single role by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasAnyPermission('role:read', 'user:read')")
    public ResponseEntity<ApiResponse<RoleDTO>> getById(@PathVariable("id") Long id) {
        return roleRepository.findById(id)
                .map(role -> ResponseEntity.ok(ApiResponse.success(toDTO(role))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Role not found")));
    }

    @Operation(summary = "Create role", description = "Create a new role")
    @PostMapping
    @PreAuthorize("@securityUtils.isAdmin()")
    public ResponseEntity<ApiResponse<RoleDTO>> create(@Valid @RequestBody RoleDTO dto) {
        if (roleRepository.existsByRoleCode(dto.getRoleCode())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Role code already exists"));
        }

        Role role = toEntity(dto);
        role = roleRepository.save(role);
        return ResponseEntity.ok(ApiResponse.success("Role created", toDTO(role)));
    }

    @Operation(summary = "Update role", description = "Update an existing role")
    @PutMapping("/{id}")
    @PreAuthorize("@securityUtils.isAdmin()")
    public ResponseEntity<ApiResponse<RoleDTO>> update(@PathVariable("id") Long id, @Valid @RequestBody RoleDTO dto) {
        return roleRepository.findById(id)
                .map(existing -> {
                    updateEntity(existing, dto);
                    Role saved = roleRepository.save(existing);
                    return ResponseEntity.ok(ApiResponse.success("Role updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Role not found")));
    }

    @Operation(summary = "Delete role", description = "Delete a role by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtils.isAdmin()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Role not found"));
        }
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Cannot delete system role"));
        }
        roleRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted", null));
    }

    @Operation(summary = "Get all permissions", description = "Get list of available permissions")
    @GetMapping("/permissions")
    @PreAuthorize("@securityUtils.hasPermission('role:read')")
    public ResponseEntity<ApiResponse<List<String>>> getPermissions() {
        return ResponseEntity.ok(ApiResponse.success(List.of(
                "*",
                "request:*",
                "request:create",
                "request:read",
                "request:write",
                "request:delete",
                "request:assign",
                "request:transfer",
                "request:transition",
                "request:test",
                "request:comment",
                "user:*",
                "user:read",
                "user:write",
                "user:delete",
                "role:*",
                "role:read",
                "role:write",
                "org:*",
                "org:read",
                "org:write",
                "team:*",
                "team:read",
                "team:write",
                "audit:read",
                "admin:*"
        )));
    }

    private RoleDTO toDTO(Role role) {
        List<String> permissions = null;
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            try {
                permissions = objectMapper.readValue(role.getPermissions(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                permissions = List.of();
            }
        }

        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .roleCode(role.getRoleCode())
                .description(role.getDescription())
                .status(role.getStatus())
                .isSystem(role.getIsSystem())
                .permissions(permissions)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    private Role toEntity(RoleDTO dto) {
        Role role = new Role(dto.getName(), dto.getRoleCode());
        updateEntity(role, dto);
        return role;
    }

    private void updateEntity(Role role, RoleDTO dto) {
        role.setDescription(dto.getDescription());
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        role.setIsSystem(dto.getIsSystem() != null ? dto.getIsSystem() : false);

        if (dto.getPermissions() != null) {
            try {
                role.setPermissions(objectMapper.writeValueAsString(dto.getPermissions()));
            } catch (Exception e) {
                role.setPermissions("[]");
            }
        }
    }
}
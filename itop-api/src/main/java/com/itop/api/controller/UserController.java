package com.itop.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itop.api.dto.PageResponse;
import com.itop.api.dto.UserDTO;
import com.itop.api.security.SecurityUtils;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.Organization;
import com.itop.core.entity.Role;
import com.itop.core.entity.User;
import com.itop.core.repository.OrganizationRepository;
import com.itop.core.repository.RoleRepository;
import com.itop.core.repository.TeamRepository;
import com.itop.core.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "User", description = "User management APIs")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Get all users", description = "Retrieve a paginated list of users with optional filtering")
    @GetMapping
    @PreAuthorize("@securityUtils.hasPermission('user:read')")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "username") String sort,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "orgId", required = false) Long orgId) {

        Set<Long> accessibleOrgIds = securityUtils.getAccessibleOrgIds();
        if (orgId != null && accessibleOrgIds != null && !accessibleOrgIds.contains(orgId)) {
            return ResponseEntity.ok(ApiResponse.error(403, "Organization is outside your access scope"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (accessibleOrgIds != null) {
                predicates.add(root.get("organizationId").in(accessibleOrgIds));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like)
                ));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (orgId != null) {
                predicates.add(cb.equal(root.get("organizationId"), orgId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<UserDTO> dtos = userPage.getContent().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(dtos, page, size, userPage.getTotalElements())));
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a single user by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('user:read')")
    public ResponseEntity<ApiResponse<UserDTO>> getById(@PathVariable("id") Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "User not found"));
        }
        if (!canAccess(user)) {
            return ResponseEntity.ok(ApiResponse.error(403, "User is outside your access scope"));
        }
        return ResponseEntity.ok(ApiResponse.success(toDTO(user)));
    }

    @Operation(summary = "Create user", description = "Create a new user")
    @PostMapping
    @PreAuthorize("@securityUtils.hasPermission('user:write')")
    public ResponseEntity<ApiResponse<UserDTO>> create(@Valid @RequestBody UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Username already exists"));
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.ok(ApiResponse.error(400, "Email already exists"));
        }

        String organizationError = validateOrganization(dto.getOrganizationId());
        if (organizationError != null) {
            return ResponseEntity.ok(ApiResponse.error(400, organizationError));
        }

        List<Role> roles;
        try {
            roles = resolveAssignableRoles(dto.getRoleIds());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(ApiResponse.error(400, ex.getMessage()));
        }

        User.AuthMethod authMethod;
        try {
            authMethod = parseAuthMethod(dto.getAuthMethod());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(ApiResponse.error(400, ex.getMessage()));
        }
        if (authMethod == User.AuthMethod.LOCAL && (dto.getPassword() == null || dto.getPassword().length() < 6)) {
            return ResponseEntity.ok(ApiResponse.error(400, "Local accounts require a password of at least 6 characters"));
        }

        User user = new User(dto.getUsername(), dto.getEmail());
        applyEditableFields(user, dto, roles, authMethod);
        String password = authMethod == User.AuthMethod.LOCAL ? dto.getPassword() : UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(password));

        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User created", toDTO(saved)));
    }

    @Operation(summary = "Update user", description = "Update an existing user")
    @PutMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('user:write')")
    public ResponseEntity<ApiResponse<UserDTO>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserDTO dto) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "User not found"));
        }
        if (!canAccess(existing)) {
            return ResponseEntity.ok(ApiResponse.error(403, "User is outside your access scope"));
        }
        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            return ResponseEntity.ok(ApiResponse.error(400, "Email already exists"));
        }

        String organizationError = validateOrganization(dto.getOrganizationId());
        if (organizationError != null) {
            return ResponseEntity.ok(ApiResponse.error(400, organizationError));
        }

        List<Role> roles;
        try {
            roles = resolveAssignableRoles(dto.getRoleIds());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(ApiResponse.error(400, ex.getMessage()));
        }

        User.AuthMethod authMethod;
        try {
            authMethod = parseAuthMethod(dto.getAuthMethod());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(ApiResponse.error(400, ex.getMessage()));
        }

        applyEditableFields(existing, dto, roles, authMethod);
        User saved = userRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.success("User updated", toDTO(saved)));
    }

    @Operation(summary = "Set user status", description = "Enable or disable a user account")
    @PatchMapping("/{id}/status")
    @PreAuthorize("@securityUtils.hasPermission('user:write')")
    public ResponseEntity<ApiResponse<UserDTO>> setStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "User not found"));
        }
        if (!canAccess(user)) {
            return ResponseEntity.ok(ApiResponse.error(403, "User is outside your access scope"));
        }
        if (id.equals(securityUtils.getCurrentUserId()) && "inactive".equals(request.getStatus())) {
            return ResponseEntity.ok(ApiResponse.error(400, "You cannot disable your own account"));
        }

        user.setStatus(request.getStatus());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Status updated", toDTO(saved)));
    }

    @Operation(summary = "Delete user", description = "Delete a user by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('user:write')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "User not found"));
        }
        if (!canAccess(user)) {
            return ResponseEntity.ok(ApiResponse.error(403, "User is outside your access scope"));
        }
        if (id.equals(securityUtils.getCurrentUserId())) {
            return ResponseEntity.ok(ApiResponse.error(400, "You cannot delete your own account"));
        }
        userRepository.delete(user);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }

    @Operation(summary = "Reset password", description = "Reset a local user's password")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("@securityUtils.hasPermission('user:write')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable("id") Long id,
            @Valid @RequestBody PasswordResetRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "User not found"));
        }
        if (!canAccess(user)) {
            return ResponseEntity.ok(ApiResponse.error(403, "User is outside your access scope"));
        }
        if (user.getAuthMethod() != null && user.getAuthMethod() != User.AuthMethod.LOCAL) {
            return ResponseEntity.ok(ApiResponse.error(400, "Password reset is only available for local accounts"));
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    @Operation(summary = "Unlock user", description = "Unlock a locked user account")
    @PostMapping("/{id}/unlock")
    @PreAuthorize("@securityUtils.hasPermission('user:write')")
    public ResponseEntity<ApiResponse<Void>> unlock(@PathVariable("id") Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "User not found"));
        }
        if (!canAccess(user)) {
            return ResponseEntity.ok(ApiResponse.error(403, "User is outside your access scope"));
        }
        user.setLocked(false);
        user.setLockedUntil(null);
        user.setFailedLogins(0);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User unlocked", null));
    }

    private UserDTO toDTO(User user) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .organizationId(user.getOrganizationId())
                .status(user.getStatus())
                .language(user.getLanguage())
                .authMethod(user.getAuthMethod() != null ? user.getAuthMethod().name() : "LOCAL")
                .lastLogin(user.getLastLogin())
                .locked(user.getLocked())
                .failedLogins(user.getFailedLogins())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (user.getOrganization() != null) {
            builder.organizationName(user.getOrganization().getName());
        }
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            builder.roleIds(user.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
            builder.roleCodes(user.getRoles().stream().map(Role::getRoleCode).collect(Collectors.toList()));
        }
        builder.teamNames(teamRepository.findDistinctByMemberUsersIdOrLeaderUserId(user.getId(), user.getId())
                .stream().map(com.itop.core.entity.Team::getName).collect(Collectors.toList()));
        return builder.build();
    }

    private void applyEditableFields(User user, UserDTO dto, List<Role> roles, User.AuthMethod authMethod) {
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setLanguage(dto.getLanguage() != null ? dto.getLanguage() : "zh_CN");
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        user.setAuthMethod(authMethod);
        Organization organization = organizationRepository.findById(dto.getOrganizationId()).orElseThrow();
        user.setOrganization(organization);
        user.setRoles(roles);
    }

    private User.AuthMethod parseAuthMethod(String value) {
        try {
            return value == null || value.isBlank() ? User.AuthMethod.LOCAL : User.AuthMethod.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported authentication method");
        }
    }

    private String validateOrganization(Long organizationId) {
        if (organizationId == null) {
            return "Organization is required";
        }
        if (!organizationRepository.existsById(organizationId)) {
            return "Organization not found";
        }
        Set<Long> accessibleOrgIds = securityUtils.getAccessibleOrgIds();
        if (accessibleOrgIds != null && !accessibleOrgIds.contains(organizationId)) {
            return "Organization is outside your access scope";
        }
        return null;
    }

    private List<Role> resolveAssignableRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
        Set<Long> distinctIds = new LinkedHashSet<>(roleIds);
        List<Role> roles = roleRepository.findAllById(distinctIds);
        if (roles.size() != distinctIds.size()) {
            throw new IllegalArgumentException("One or more roles do not exist");
        }
        if (!securityUtils.isAdmin()) {
            for (Role role : roles) {
                if ("ADMIN".equals(role.getRoleCode()) || !canGrantPermissions(role)) {
                    throw new IllegalArgumentException("You cannot assign role " + role.getRoleCode());
                }
            }
        }
        return roles;
    }

    private boolean canGrantPermissions(Role role) {
        if (role.getPermissions() == null || role.getPermissions().isBlank()) {
            return true;
        }
        try {
            List<String> permissions = objectMapper.readValue(role.getPermissions(), new TypeReference<List<String>>() {});
            return permissions.stream().noneMatch("*"::equals) && permissions.stream().allMatch(securityUtils::hasPermission);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean canAccess(User user) {
        Set<Long> accessibleOrgIds = securityUtils.getAccessibleOrgIds();
        return accessibleOrgIds == null
                || (user.getOrganizationId() != null && accessibleOrgIds.contains(user.getOrganizationId()));
    }

    @Data
    public static class StatusUpdateRequest {
        @NotBlank
        @Pattern(regexp = "active|inactive")
        private String status;
    }

    @Data
    public static class PasswordResetRequest {
        @NotBlank
        @Size(min = 6, max = 100)
        private String newPassword;
    }
}

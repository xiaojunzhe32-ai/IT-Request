package com.itop.api.controller;

import com.itop.api.dto.CurrentUserDTO;
import com.itop.api.dto.JwtResponse;
import com.itop.api.dto.LoginRequest;
import com.itop.api.security.JwtService;
import com.itop.api.security.SecurityUtils;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.Organization;
import com.itop.core.entity.Role;
import com.itop.core.entity.User;
import com.itop.core.entity.UserAccessibleOrg;
import com.itop.core.repository.OrganizationRepository;
import com.itop.core.repository.UserAccessibleOrgRepository;
import com.itop.core.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityUtils securityUtils;
    private final UserAccessibleOrgRepository userAccessibleOrgRepository;
    private final OrganizationRepository organizationRepository;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            SecurityUtils securityUtils,
            UserAccessibleOrgRepository userAccessibleOrgRepository,
            OrganizationRepository organizationRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.securityUtils = securityUtils;
        this.userAccessibleOrgRepository = userAccessibleOrgRepository;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .filter(user -> Boolean.FALSE.equals(user.getLocked()))
                .filter(user -> "active".equalsIgnoreCase(user.getStatus()))
                .filter(user -> user.getAuthMethod() == null || user.getAuthMethod() == User.AuthMethod.LOCAL)
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(this::loginSuccess)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "用户名或密码错误")));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("退出成功", null));
    }

    /**
     * 获取当前登录用户信息（角色、权限、可访问组织）。
     * 前端登录后及刷新页面时调用，用于菜单过滤、按钮控制与数据隔离。
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserDTO>> me() {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "未登录"));
        }
        return ResponseEntity.ok(ApiResponse.success(buildCurrentUserDTO(user)));
    }

    private CurrentUserDTO buildCurrentUserDTO(User user) {
        boolean isAdmin = securityUtils.isAdmin();
        Set<String> permissions = securityUtils.getCurrentPermissions();

        // 角色
        List<String> roleCodes = new ArrayList<>();
        if (user.getRoles() != null) {
            roleCodes = user.getRoles().stream()
                    .map(Role::getRoleCode)
                    .collect(Collectors.toList());
        }

        // 可访问组织
        List<CurrentUserDTO.AccessibleOrg> accessibleOrgs = new ArrayList<>();
        if (!isAdmin) {
            List<UserAccessibleOrg> accessors = userAccessibleOrgRepository.findByUserId(user.getId());
            for (UserAccessibleOrg uao : accessors) {
                String orgName = organizationRepository.findById(uao.getOrgId())
                        .map(Organization::getName).orElse(null);
                accessibleOrgs.add(CurrentUserDTO.AccessibleOrg.builder()
                        .orgId(uao.getOrgId())
                        .orgName(orgName)
                        .includeChildren(uao.getIncludeChildren())
                        .build());
            }
        }

        return CurrentUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .language(user.getLanguage())
                .organizationId(user.getOrganizationId())
                .organizationName(user.getOrganization() != null ? user.getOrganization().getName() : null)
                .roles(roleCodes)
                .permissions(new ArrayList<>(permissions))
                .globalAccess(isAdmin)
                .accessibleOrgs(accessibleOrgs)
                .build();
    }

    private ResponseEntity<ApiResponse<JwtResponse>> loginSuccess(User user) {
        user.setLastLogin(LocalDateTime.now());
        user.setFailedLogins(0);
        userRepository.save(user);

        JwtResponse response = new JwtResponse(
                jwtService.generateToken(user.getUsername()),
                user.getUsername(),
                user.getEmail(),
                user.getId()
        );

        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }
}

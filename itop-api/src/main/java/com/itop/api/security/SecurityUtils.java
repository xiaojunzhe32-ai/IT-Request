package com.itop.api.security;

import com.itop.core.entity.Organization;
import com.itop.core.entity.Role;
import com.itop.core.entity.User;
import com.itop.core.entity.UserAccessibleOrg;
import com.itop.core.repository.OrganizationRepository;
import com.itop.core.repository.UserAccessibleOrgRepository;
import com.itop.core.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限/角色/数据隔离工具，供 @PreAuthorize 表达式和业务代码使用。
 * <p>
 * 用法：
 * <pre>
 *   &#64;PreAuthorize("@securityUtils.hasPermission('ticket:read')")
 *   &#64;PreAuthorize("@securityUtils.hasAnyPermission('ticket:read','ticket:write')")
 * </pre>
 * <p>
 * ADMIN 角色（permissions 含 "*"）在 {@link JwtAuthenticationFilter} 中被授予
 * ALL_PERMISSIONS 权限，本类的所有 has* 方法对该权限直接放行。
 */
@Component("securityUtils")
public class SecurityUtils {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityUtils.class);

    public static final String ALL_PERMISSIONS = "ALL_PERMISSIONS";
    public static final String ROLE_PREFIX = "ROLE_";

    private final UserRepository userRepository;
    private final UserAccessibleOrgRepository userAccessibleOrgRepository;
    private final OrganizationRepository organizationRepository;

    public SecurityUtils(UserRepository userRepository,
                         UserAccessibleOrgRepository userAccessibleOrgRepository,
                         OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.userAccessibleOrgRepository = userAccessibleOrgRepository;
        this.organizationRepository = organizationRepository;
    }

    // ------------------------------------------------------------------
    // 权限检查（@PreAuthorize 使用）
    // ------------------------------------------------------------------

    /** 当前用户是否拥有指定权限（ALL_PERMISSIONS 直接放行；支持 ci:* 这类通配权限） */
    public boolean hasPermission(String permission) {
        return matchesAnyAuthority(permission, getAuthorities());
    }

    /** 当前用户是否拥有任意一个指定权限 */
    public boolean hasAnyPermission(String... permissions) {
        Collection<? extends GrantedAuthority> authorities = getAuthorities();
        for (String permission : permissions) {
            if (matchesAnyAuthority(permission, authorities)) {
                return true;
            }
        }
        return false;
    }

    /** 当前用户是否拥有指定角色（传 roleCode，如 ADMIN、SERVICE_DESK） */
    public boolean hasRole(String roleCode) {
        Collection<? extends GrantedAuthority> authorities = getAuthorities();
        if (authorities == null) return false;
        String roleName = roleCode.startsWith(ROLE_PREFIX) ? roleCode : ROLE_PREFIX + roleCode;
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(ALL_PERMISSIONS) || a.equals(roleName));
    }

    private Collection<? extends GrantedAuthority> getAuthorities() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return List.of();
        }
        return auth.getAuthorities();
    }

    /**
     * 权限匹配：ALL_PERMISSIONS 放行；精确匹配；通配匹配（ci:* 命中 ci:write）。
     */
    private boolean matchesAnyAuthority(String permission, Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null) return false;
        log.info("Checking permission '{}' against authorities: {}", permission, authorities);
        for (GrantedAuthority ga : authorities) {
            String a = ga.getAuthority();
            log.debug("Checking against authority: {}", a);
            if (a.equals(ALL_PERMISSIONS) || a.equals("*")) {
                log.info("Permission '{}' granted by ALL_PERMISSIONS wildcard", permission);
                return true;
            }
            if (a.equals(permission)) {
                log.info("Permission '{}' granted by exact match", permission);
                return true;
            }
            // 通配：ci:* 命中 ci:read / ci:write / ci:delete 等
            if (a.endsWith(":*") && permission.startsWith(a.substring(0, a.length() - 1))) {
                log.info("Permission '{}' granted by wildcard '{}'", permission, a);
                return true;
            }
        }
        log.warn("Permission '{}' NOT granted. Authorities: {}", permission, authorities);
        return false;
    }

    /** 当前用户是否为全局管理员（拥有 ALL_PERMISSIONS 或 ADMIN 角色） */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isRequester() {
        return hasRole("REQUESTER");
    }

    public boolean isTechnician() {
        return hasRole("TECHNICIAN");
    }

    public boolean isTester() {
        return hasRole("TESTER");
    }

    public boolean isTeamLead() {
        return hasRole("TEAM_LEAD");
    }

    /** IT staff: Technician, Tester, Team Lead, or Admin */
    public boolean isITStaff() {
        return isTechnician() || isTester() || isTeamLead() || isAdmin();
    }

    // ------------------------------------------------------------------
    // 当前用户信息
    // ------------------------------------------------------------------

    /** 获取当前登录用户名 */
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }
        return auth.getName();
    }

    /** 加载当前登录用户实体（含角色） */
    public User getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }
        return userRepository.findByUsernameWithRoles(username).orElse(null);
    }

    /** 当前用户 ID */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /** 汇总当前用户所有角色的权限（扁平化去重），ADMIN 返回 ["*"] */
    public Set<String> getCurrentPermissions() {
        User user = getCurrentUser();
        if (user == null || user.getRoles() == null) {
            return Collections.emptySet();
        }
        Set<String> all = new HashSet<>();
        for (Role role : user.getRoles()) {
            if (role.getPermissions() == null || role.getPermissions().isBlank()) {
                continue;
            }
            // 解析 JSON 数组，简单处理去除外层括号与引号
            String json = role.getPermissions().trim();
            if (json.startsWith("[")) {
                json = json.substring(1, json.length() - 1);
            }
            for (String p : json.split(",")) {
                String perm = p.replaceAll("\"", "").trim();
                if (!perm.isEmpty()) {
                    all.add(perm);
                }
            }
        }
        return all;
    }

    // ------------------------------------------------------------------
    // 数据隔离：可访问组织
    // ------------------------------------------------------------------

    /**
     * 当前用户可访问的组织 ID 集合（已展开 include_children 的所有子组织）。
     * <p>
     * - 全局管理员（ALL_PERMISSIONS/ADMIN）返回 null，表示不做组织过滤（可见全部）。
     * - 普通用户：返回 user_accessible_org 中配置的组织及其子组织。
     * - 若未配置任何可访问组织，回退到用户所属组织。
     */
    public Set<Long> getAccessibleOrgIds() {
        if (isAdmin()) {
            return null; // null = 全局可见
        }
        User user = getCurrentUser();
        if (user == null) {
            return Collections.emptySet();
        }

        List<UserAccessibleOrg> accessors = userAccessibleOrgRepository.findByUserId(user.getId());
        Set<Long> result = new LinkedHashSet<>();
        for (UserAccessibleOrg uao : accessors) {
            result.add(uao.getOrgId());
            if (Boolean.TRUE.equals(uao.getIncludeChildren())) {
                result.addAll(getAllDescendants(uao.getOrgId()));
            }
        }
        // 未配置任何可访问组织时，回退到用户所属组织
        if (result.isEmpty() && user.getOrganizationId() != null) {
            result.add(user.getOrganizationId());
        }
        return result;
    }

    /** 递归获取某组织的所有后代组织 ID（广度优先） */
    private Set<Long> getAllDescendants(Long rootId) {
        Set<Long> descendants = new LinkedHashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            List<Organization> children = organizationRepository.findByParentId(current);
            for (Organization child : children) {
                if (descendants.add(child.getId())) {
                    queue.add(child.getId());
                }
            }
        }
        return descendants;
    }
}

package com.itop.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 当前登录用户信息（/auth/me 返回），包含角色、权限与可访问组织，
 * 供前端做菜单过滤、按钮控制与数据隔离。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String status;
    private String language;

    private Long organizationId;
    private String organizationName;

    /** 角色代码列表，如 ["ADMIN"]、["SERVICE_DESK"] */
    private List<String> roles;

    /** 扁平化权限列表，如 ["ticket:read","ticket:assign"]；ADMIN 为 ["*"] */
    private List<String> permissions;

    /** 是否全局管理员（不做组织过滤） */
    private boolean globalAccess;

    /** 可访问组织列表（仅 globalAccess=false 时有意义） */
    private List<AccessibleOrg> accessibleOrgs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessibleOrg {
        private Long orgId;
        private String orgName;
        private Boolean includeChildren;
    }
}

package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户可访问组织 - 数据隔离核心表。
 * 定义用户可以查看哪些组织的数据，include_children=true 表示包含所有子组织。
 */
@Entity
@Table(name = "user_accessible_org")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserAccessibleOrg.PK.class)
public class UserAccessibleOrg {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "include_children")
    private Boolean includeChildren = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements java.io.Serializable {
        private Long userId;
        private Long orgId;
    }
}

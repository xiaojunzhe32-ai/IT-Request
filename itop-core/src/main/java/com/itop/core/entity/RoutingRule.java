package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RoutingRule - 请求自动路由规则
 * <p>
 * 新建请求时按 sortOrder 顺序匹配第一条命中的规则，将请求分配到 rule.teamId 指向的团队。
 * 匹配维度：组织、请求类型、优先级（均为可空，空表示不限）。
 * 若无规则命中，回退到 isFallback=true 的默认规则。
 */
@Entity
@Table(name = "routing_rule")
@Getter
@Setter
@NoArgsConstructor
public class RoutingRule extends BaseEntity {

    /** 作用组织 ID，null 表示对所有组织生效 */
    @Column(name = "org_id")
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organization organization;

    /** 受影响服务，如 "REPORT-AAID"；null 表示不限服务 */
    @Column(name = "affected_service", length = 100)
    private String affectedService;

    /** 请求类型，如 "Application Issue"、"Network Issue"；null 表示不限类型 */
    @Column(name = "request_type", length = 100)
    private String requestType;

    /** 优先级，如 "Low"/"Medium"/"High"/"Critical"；null 表示不限优先级 */
    @Column(name = "priority", length = 20)
    private String priority;

    /** 命中后分配的团队 ID */
    @Column(name = "team_id")
    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;

    /** 是否启用 */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /** 排序权重，升序；数值越小越优先匹配 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 100;

    /** 是否为兜底规则（无任何规则命中时使用） */
    @Column(name = "is_fallback", nullable = false)
    private Boolean isFallback = false;

    public RoutingRule(String name) {
        this.setName(name);
    }
}

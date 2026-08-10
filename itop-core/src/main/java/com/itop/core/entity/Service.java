package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 服务项
 * Service - Specific service offering
 */
@Getter
@Setter
@Entity
@Table(name = "service")
public class Service extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subfamily_id", nullable = false)
    private ServiceSubfamily subfamily;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name = "service_type", length = 50)
    private String serviceType = "USER_REQUEST"; // USER_REQUEST, INCIDENT

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_id")
    private SLA sla;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
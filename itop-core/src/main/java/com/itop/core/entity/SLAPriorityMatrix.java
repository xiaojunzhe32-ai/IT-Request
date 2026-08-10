package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * SLA 优先级矩阵
 * SLA Priority Matrix - Maps urgency and impact to priority
 */
@Getter
@Setter
@Entity
@Table(name = "sla_priority_matrix")
public class SLAPriorityMatrix extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_id", nullable = false)
    private SLA sla;

    @Column(nullable = false, length = 20)
    private String urgency; // low, medium, high, critical

    @Column(nullable = false, length = 20)
    private String impact; // low, medium, high, critical

    @Column(nullable = false, length = 20)
    private String priority; // low, medium, high, critical

    @Column(name = "tto_hours")
    private Integer ttoHours;

    @Column(name = "ttr_hours")
    private Integer ttrHours;
}
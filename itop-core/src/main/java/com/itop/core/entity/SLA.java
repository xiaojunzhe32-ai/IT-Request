package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 服务级别协议
 * SLA - Service Level Agreement
 */
@Getter
@Setter
@Entity
@Table(name = "sla")
public class SLA extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name = "tto_hours")
    private Integer ttoHours = 4; // Time to Own (hours)

    @Column(name = "ttr_hours")
    private Integer ttrHours = 8; // Time to Resolve (hours)

    @Column(length = 20)
    private String priority = "medium"; // low, medium, high, critical

    @Column(name = "calendar_id", length = 50)
    private String calendarId;

    @Column(name = "is_default")
    private Boolean isDefault = false;
}
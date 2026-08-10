package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 服务子类
 * Service Subfamily - Second level service categorization
 */
@Getter
@Setter
@Entity
@Table(name = "service_subfamily")
public class ServiceSubfamily extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private ServiceFamily family;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
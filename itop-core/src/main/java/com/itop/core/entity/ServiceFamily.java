package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 服务系列
 * Service Family - Top level service categorization
 */
@Getter
@Setter
@Entity
@Table(name = "service_family")
public class ServiceFamily extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @Column(length = 50)
    private String icon;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
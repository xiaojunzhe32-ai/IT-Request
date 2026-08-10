package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Base entity for all CMDB objects
 * Provides common fields: id, name, status, created/updated timestamps, optimistic locking
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100)
    private String status = "active";

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @Column(length = 100, updatable = false)
    private String createdBy;

    @Column(length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "active";
        }
    }
}
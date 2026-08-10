package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private Action action;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    public enum Action {
        CREATE,
        UPDATE,
        DELETE,
        STATUS_CHANGE,
        ASSIGN,
        APPROVE,
        RESOLVE,
        CLOSE,
        START,
        COMPLETE,
        FAIL
    }

    public AuditLog(String entityType, Long entityId, Action action) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
    }
}
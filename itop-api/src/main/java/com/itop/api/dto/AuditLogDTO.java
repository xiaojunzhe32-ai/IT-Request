package com.itop.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogDTO {

    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private Long userId;
    private String username;
    private String ipAddress;
    private String userAgent;
    private String description;
    private LocalDateTime createdAt;
}
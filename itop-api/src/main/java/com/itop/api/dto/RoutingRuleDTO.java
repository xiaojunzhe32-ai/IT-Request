package com.itop.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRuleDTO {

    private Long id;
    private String name;
    private String description;
    private Long organizationId;
    private String organizationName;
    private String affectedService;
    private String requestType;
    private String priority;
    private Long teamId;
    private String teamName;
    private Boolean enabled;
    private Integer sortOrder;
    private Boolean isFallback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

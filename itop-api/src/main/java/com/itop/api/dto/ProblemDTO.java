package com.itop.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProblemDTO {

    private Long id;
    private String problemNumber;
    private String problemType;
    private String title;
    private String description;
    private Long organizationId;
    private Long callerId;
    private Long agentId;
    private Long teamId;
    private String impact;
    private String urgency;
    private String priority;
    private String status;
    private String rootCause;
    private String workAround;
    private String impactAnalysis;
    private String solution;
    private Long relatedChangeId;
    private LocalDateTime ttoDeadline;
    private LocalDateTime ttrDeadline;
    private Long slaId;
    private LocalDateTime startDate;
    private LocalDateTime lastUpdateDate;
    private LocalDateTime closeDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
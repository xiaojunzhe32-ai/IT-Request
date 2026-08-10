package com.itop.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChangeRequestDTO {

    private Long id;
    private String changeNumber;
    private String changeType;
    private String changeCategory;
    private String title;
    private String description;
    private String changeReason;
    private String riskAssessment;
    private String rollbackPlan;
    private String implementationPlan;
    private String testPlan;
    private Long organizationId;
    private Long changeOwnerId;
    private String changeOwnerName;
    private Long approverId;
    private String approverName;
    private Long teamId;
    private LocalDateTime approvalDate;
    private LocalDateTime plannedStartDate;
    private LocalDateTime plannedEndDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;
    private Long parentChangeId;
    private Long relatedProblemId;
    private String impact;
    private String urgency;
    private String priority;
    private String status;
    private String solution;
    private LocalDateTime ttoDeadline;
    private LocalDateTime ttrDeadline;
    private Long slaId;
    private LocalDateTime startDate;
    private LocalDateTime lastUpdateDate;
    private LocalDateTime closeDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
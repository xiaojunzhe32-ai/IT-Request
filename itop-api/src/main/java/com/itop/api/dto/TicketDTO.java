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
public class TicketDTO {
    private Long id;
    private String title;
    private String description;
    private Long organizationId;
    private String organizationName;
    private Long callerId;
    private String callerName;
    private Long agentId;
    private String agentName;
    private Long teamId;
    private String teamName;
    private String impact;
    private String urgency;
    private String priority;
    private String status;
    private String resolution;
    private String finalClass;
    private String ticketType;
    private LocalDateTime startDate;
    private LocalDateTime lastUpdateDate;
    private LocalDateTime closeDate;
    private String solution;
    private LocalDateTime ttoDeadline;
    private LocalDateTime ttrDeadline;
    private Long slaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
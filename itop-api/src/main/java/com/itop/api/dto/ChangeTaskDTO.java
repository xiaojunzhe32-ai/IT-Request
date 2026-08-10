package com.itop.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChangeTaskDTO {

    private Long id;
    private Long changeId;
    private String changeNumber;
    private String taskType;
    private String taskStatus;
    private Long assigneeId;
    private String assigneeName;
    private LocalDateTime plannedStartDate;
    private LocalDateTime plannedEndDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;
    private String instructions;
    private String result;
    private Integer sortOrder;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
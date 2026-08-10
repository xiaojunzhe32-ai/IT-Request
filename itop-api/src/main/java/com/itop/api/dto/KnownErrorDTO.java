package com.itop.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class KnownErrorDTO {

    private Long id;
    private String errorCode;
    private Long problemId;
    private String problemName;
    private String symptoms;
    private String cause;
    private String workaround;
    private String solution;
    private String errorType;
    private String severity;
    private Boolean applyToAll;
    private LocalDateTime firstDetected;
    private LocalDateTime lastOccurrence;
    private Integer occurrenceCount;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
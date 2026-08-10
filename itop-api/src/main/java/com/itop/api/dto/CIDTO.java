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
public class CIDTO {
    private Long id;
    private String name;
    private Long organizationId;
    private String organizationName;
    private String status;
    private String description;
    private String finalClass;
    private String ciType;
    private String assetNumber;
    private LocalDateTime move2Production;
    private LocalDateTime obsolescenceDate;
    private String businessCriticity;
    private String redundancy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
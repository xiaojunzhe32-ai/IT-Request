package com.itop.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CISummaryDTO {

    private Long id;
    private String name;
    private String finalClass;
    private String status;
    private Long orgId;
    private String relationType;
}
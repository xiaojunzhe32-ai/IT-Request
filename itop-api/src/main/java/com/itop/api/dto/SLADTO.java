package com.itop.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLADTO {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Code is required")
    @Size(max = 100, message = "Code must not exceed 100 characters")
    private String code;

    private String status;

    private String description;

    private Long orgId;

    private String orgName;

    private Integer ttoHours;

    private Integer ttrHours;

    @Size(max = 20, message = "Priority must not exceed 20 characters")
    private String priority;

    @Size(max = 50, message = "Calendar ID must not exceed 50 characters")
    private String calendarId;

    private Boolean isDefault;
}
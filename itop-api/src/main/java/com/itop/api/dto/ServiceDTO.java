package com.itop.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Code is required")
    @Size(max = 100, message = "Code must not exceed 100 characters")
    private String code;

    private String status;

    private String description;

    @NotNull(message = "Subfamily ID is required")
    private Long subfamilyId;

    private String subfamilyName;

    private Long orgId;

    private String orgName;

    @Size(max = 50, message = "Service type must not exceed 50 characters")
    private String serviceType;

    private Long slaId;

    private String slaName;

    private Integer sortOrder;
}
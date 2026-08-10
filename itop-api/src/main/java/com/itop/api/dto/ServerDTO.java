package com.itop.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    private String status;
    private String description;
    private String assetNumber;

    private String brandName;
    private String modelName;
    private String serialNumber;

    private String cpu;
    private String ram;
    private String disk;

    private String osFamily;
    private String osVersion;

    private String ipAddress;
    private String macAddress;
    private String managementIp;

    private Boolean isVirtual;
    private String serverType;

    private LocalDateTime move2Production;
    private LocalDateTime obsolescenceDate;

    private String businessCriticity;
}
package com.itop.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String name;
    private String roleCode;
    private String description;
    private String status;
    private Boolean isSystem;
    private List<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
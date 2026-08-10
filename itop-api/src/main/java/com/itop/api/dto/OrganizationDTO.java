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
public class OrganizationDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Code must not exceed 255 characters")
    private String code;

    private String status;

    private String description;

    private Long parentId;

    @Size(max = 100, message = "Type must not exceed 100 characters")
    private String type;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;

    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 50, message = "Website must not exceed 50 characters")
    private String website;
}
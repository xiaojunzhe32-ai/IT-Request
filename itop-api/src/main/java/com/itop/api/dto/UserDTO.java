package com.itop.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class UserDTO {
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @Size(max = 100, message = "Password must not exceed 100 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 50)
    private String phone;

    @NotNull(message = "Organization is required")
    private Long organizationId;
    private String organizationName;

    @Pattern(regexp = "active|inactive", message = "Status must be active or inactive")
    private String status;

    @Size(max = 10)
    private String language;

    @Pattern(regexp = "LOCAL|LDAP|OAUTH|SAML", message = "Authentication method is invalid")
    private String authMethod;

    private LocalDateTime lastLogin;
    private Boolean locked;
    private Integer failedLogins;

    @NotNull(message = "Roles are required")
    @Size(min = 1, message = "At least one role is required")
    private List<Long> roleIds;

    private List<String> roleCodes;
    private List<String> teamNames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

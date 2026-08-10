package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Role entity for RBAC
 */
@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity {

    @Column(name = "role_code", unique = true, nullable = false, length = 50)
    private String roleCode;

    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions; // JSON array of permissions

    @Column(name = "is_system")
    private Boolean isSystem = false;

    @ManyToMany(mappedBy = "roles")
    private List<User> users = new ArrayList<>();

    public Role(String name, String roleCode) {
        this.setName(name);
        this.roleCode = roleCode;
    }
}
package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity for authentication and authorization
 */
@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone", length = 50)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name = "org_id", insertable = false, updatable = false)
    private Long organizationId;

    @Column(name = "language", length = 10)
    private String language = "zh_CN";

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_method", length = 20)
    private AuthMethod authMethod = AuthMethod.LOCAL;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "failed_logins")
    private Integer failedLogins = 0;

    @Column(name = "locked")
    private Boolean locked = false;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.setName(username);
    }

    public enum AuthMethod {
        LOCAL,
        LDAP,
        OAUTH,
        SAML
    }
}

package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Application - Software applications and services
 */
@Entity
@Table(name = "application")
@DiscriminatorValue("application")
@Getter
@Setter
@NoArgsConstructor
public class Application extends ConfigurationItem {

    @Column(name = "app_code", length = 50)
    private String appCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "app_type", length = 50)
    private ApplicationType appType = ApplicationType.WEBAPP;

    @Column(name = "version", length = 50)
    private String appVersion;

    @Column(name = "vendor", length = 100)
    private String vendor;

    @Column(name = "license_type", length = 50)
    private String licenseType;

    @Column(name = "license_expire")
    private LocalDateTime licenseExpire;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "database_server_id")
    private Long databaseServerId;

    @Column(name = "web_server_id")
    private Long webServerId;

    @Column(name = "documentation_url", length = 255)
    private String documentationUrl;

    public Application(String name, Long organizationId) {
        super(name, organizationId);
    }

    public enum ApplicationType {
        WEBAPP,
        DESKTOP,
        MOBILE,
        SERVICE,
        DATABASE,
        MIDDLEWARE
    }
}
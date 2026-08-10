package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Server - Represents physical or virtual servers
 */
@Entity
@Table(name = "server")
@DiscriminatorValue("server")
@Getter
@Setter
@NoArgsConstructor
public class Server extends PhysicalDevice {

    @Column(name = "cpu", length = 100)
    private String cpu;

    @Column(name = "ram", length = 50)
    private String ram;

    @Column(name = "disk", length = 50)
    private String disk;

    @Column(name = "os_family", length = 50)
    private String osFamily;

    @Column(name = "os_version", length = 50)
    private String osVersion;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "mac_address", length = 50)
    private String macAddress;

    @Column(name = "management_ip", length = 50)
    private String managementIp;

    @Column(name = "virtual_host_id")
    private Long virtualHostId;

    @Column(name = "is_virtual")
    private Boolean isVirtual = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "server_type", length = 50)
    private ServerType serverType = ServerType.PHYSICAL;

    public Server(String name, Long organizationId) {
        super(name, organizationId);
    }

    public enum ServerType {
        PHYSICAL,
        VIRTUAL,
        HYPERVISOR
    }
}
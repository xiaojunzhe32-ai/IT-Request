package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Network Device - Routers, Switches, Firewalls, etc.
 */
@Entity
@Table(name = "network_device")
@DiscriminatorValue("networkdevice")
@Getter
@Setter
@NoArgsConstructor
public class NetworkDevice extends PhysicalDevice {

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 50)
    private NetworkDeviceType deviceType = NetworkDeviceType.SWITCH;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "mac_address", length = 50)
    private String macAddress;

    @Column(name = "management_ip", length = 50)
    private String managementIp;

    @Column(name = "vlan_list", columnDefinition = "TEXT")
    private String vlanList;

    @Column(name = "port_count")
    private Integer portCount;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    public NetworkDevice(String name, Long organizationId) {
        super(name, organizationId);
    }

    public enum NetworkDeviceType {
        ROUTER,
        SWITCH,
        FIREWALL,
        LOADBALANCER,
        ACCESSPOINT,
        VPNCONCENTRATOR
    }
}

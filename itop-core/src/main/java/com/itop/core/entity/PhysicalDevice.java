package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Physical Device - Abstract base for physical hardware
 */
@Entity
@Table(name = "physical_device")
@Getter
@Setter
@NoArgsConstructor
public abstract class PhysicalDevice extends ConfigurationItem {

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "brand_name", length = 100)
    private String brandName;

    @Column(name = "model_id")
    private Long modelId;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "asset_tag", length = 100)
    private String assetTag;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "warranty_end")
    private LocalDateTime warrantyEnd;

    @Column(name = "power", length = 50)
    private String power;

    @Column(name = "rack_id")
    private Long rackId;

    @Column(name = "rack_unit")
    private Integer rackUnit;

    public PhysicalDevice(String name, Long organizationId) {
        super(name, organizationId);
    }
}

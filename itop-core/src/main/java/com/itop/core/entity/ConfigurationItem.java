package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Configuration Item (CI) - Abstract base class for all configuration items
 * This is the core of CMDB - represents any component that needs to be managed
 */
@Entity
@Table(name = "configuration_item")
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "ci_type", discriminatorType = DiscriminatorType.STRING, length = 50)
public abstract class ConfigurationItem extends BaseEntity {

    @Column(name = "org_id", nullable = false)
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organization organization;

    @Column(name = "final_class", length = 100, nullable = false)
    private String finalClass;

    @Column(name = "asset_number", length = 100)
    private String assetNumber;

    @Column(name = "move2production")
    private LocalDateTime move2Production;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "contact_id")
    private Long contactId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", insertable = false, updatable = false)
    private Contact contact;

    @Column(name = "obsolescence_date")
    private LocalDateTime obsolescenceDate;

    @Column(name = "business_criticity", length = 50)
    private String businessCriticity = "medium";

    @Column(length = 50)
    private String redundancy = "no";

    @Column(name = "documents_list", columnDefinition = "TEXT")
    private String documentsList;

    @Column(name = "services_list", columnDefinition = "TEXT")
    private String servicesList;

    @Column(name = "tickets_list", columnDefinition = "TEXT")
    private String ticketsList;

    @Override
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (finalClass == null) {
            finalClass = this.getClass().getSimpleName();
        }
    }

    public ConfigurationItem(String name, Long organizationId) {
        this.setName(name);
        this.organizationId = organizationId;
    }
}

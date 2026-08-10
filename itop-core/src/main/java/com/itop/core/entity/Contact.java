package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Contact entity (abstract base for Person, Team, etc.)
 */
@Entity
@Table(name = "contact")
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "contact_type", discriminatorType = DiscriminatorType.STRING, length = 50)
public abstract class Contact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name = "org_id", insertable = false, updatable = false)
    private Long organizationId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "notify_email")
    private Boolean notifyEmail = true;

    @Column(name = "notify_sms")
    private Boolean notifySms = false;

    public Contact(String name) {
        this.setName(name);
    }
}
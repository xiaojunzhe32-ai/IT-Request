package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Incident - An unplanned interruption or quality reduction of a service
 * Usually created by service desk or monitoring system
 */
@Entity
@Table(name = "incident")
@DiscriminatorValue("incident")
@Getter
@Setter
@NoArgsConstructor
public class Incident extends Ticket {

    @Column(name = "incident_number", unique = true, length = 50)
    private String incidentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", length = 50)
    private IncidentType incidentType = IncidentType.INCIDENT;

    @Column(name = "affected_ci_id")
    private Long affectedCiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affected_ci_id", insertable = false, updatable = false)
    private ConfigurationItem affectedCI;

    @Column(name = "workgroup_id")
    private Long workgroupId;

    @Column(name = "known_error_id")
    private Long knownErrorId;

    @Column(name = "related_problem_id")
    private Long relatedProblemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", length = 50)
    private Origin origin = Origin.MONITORING;

    public Incident(String title, Long organizationId) {
        super(title, organizationId);
    }

    public enum IncidentType {
        INCIDENT,
        MAJOR_INCIDENT
    }

    public enum Origin {
        MONITORING,
        HELPDESK,
        SELF_SERVICE,
        PHONE,
        EMAIL,
        CHAT
    }
}
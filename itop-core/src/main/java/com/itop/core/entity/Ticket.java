package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Ticket - Abstract base class for all ticket types
 * Includes UserRequest, Incident, Problem, Change, etc.
 */
@Entity
@Table(name = "ticket")
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "ticket_type", discriminatorType = DiscriminatorType.STRING, length = 50)
public abstract class Ticket extends BaseEntity {

    @Column(name = "org_id")
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organization organization;

    @Column(name = "caller_id")
    private Long callerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", insertable = false, updatable = false)
    private Contact caller;

    @Column(name = "agent_id")
    private Long agentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", insertable = false, updatable = false)
    private Person agent;

    @Column(name = "team_id")
    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;

    @Column(name = "tester_id")
    private Long testerId;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "request_type_id")
    private Long requestTypeId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "impact", length = 20)
    private String impact = "2"; // 1=High, 2=Medium, 3=Low

    @Column(name = "urgency", length = 20)
    private String urgency = "2"; // 1=High, 2=Medium, 3=Low

    @Column(name = "priority", length = 20)
    private String priority = "2"; // 1=High, 2=Medium, 3=Low

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", length = 50)
    private TicketStatus ticketStatus = TicketStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution", length = 50)
    private ResolutionStatus resolution = ResolutionStatus.NONE;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "close_date")
    private LocalDateTime closeDate;

    @Column(name = "solution", columnDefinition = "TEXT")
    private String solution;

    @Column(name = "tto_deadline")
    private LocalDateTime ttoDeadline;

    @Column(name = "ttr_deadline")
    private LocalDateTime ttrDeadline;

    @Column(name = "sla_id")
    private Long slaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_id", insertable = false, updatable = false)
    private SLA sla;

    @Column(name = "final_class", length = 100, nullable = false)
    private String finalClass;

    @Column(name = "ticket_type", insertable = false, updatable = false)
    private String ticketType;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (finalClass == null) {
            finalClass = this.getClass().getSimpleName();
        }
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        if (lastUpdateDate == null) {
            lastUpdateDate = LocalDateTime.now();
        }
    }

    public enum TicketStatus {
        NEW,
        ASSIGNED,
        IN_PROGRESS,
        TO_BE_TEST,
        TESTING,
        RESOLVED,
        USER_TEST_FAILED,
        CLOSED
    }

    public enum ResolutionStatus {
        NONE,
        ASSIGNED,
        IN_PROGRESS,
        RESOLVED,
        CLOSED
    }

    public Ticket(String title, Long organizationId) {
        this.setTitle(title);
        this.organizationId = organizationId;
    }
}
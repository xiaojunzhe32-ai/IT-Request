package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ChangeRequest - Formal proposal for changes to IT services or infrastructure
 * Manages the change lifecycle from request to implementation
 */
@Entity
@Table(name = "change_request")
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("CHANGE")
public class ChangeRequest extends Ticket {

    @Column(name = "change_number", unique = true, length = 50)
    private String changeNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", length = 50)
    private ChangeType changeType = ChangeType.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_category", length = 50)
    private ChangeCategory changeCategory = ChangeCategory.OTHER;

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    @Column(name = "risk_assessment", columnDefinition = "TEXT")
    private String riskAssessment;

    @Column(name = "rollback_plan", columnDefinition = "TEXT")
    private String rollbackPlan;

    @Column(name = "implementation_plan", columnDefinition = "TEXT")
    private String implementationPlan;

    @Column(name = "test_plan", columnDefinition = "TEXT")
    private String testPlan;

    @Column(name = "change_owner_id")
    private Long changeOwnerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_owner_id", insertable = false, updatable = false)
    private Person changeOwner;

    @Column(name = "approver_id")
    private Long approverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", insertable = false, updatable = false)
    private Person approver;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "planned_start_date")
    private LocalDateTime plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDateTime plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    @Column(name = "parent_change_id")
    private Long parentChangeId;

    @Column(name = "related_problem_id")
    private Long relatedProblemId;

    public ChangeRequest(String title, Long organizationId) {
        super(title, organizationId);
    }

    public enum ChangeType {
        NORMAL,      // Standard change requiring full approval
        STANDARD,    // Pre-approved standard change
        EMERGENCY    // Emergency change
    }

    public enum ChangeCategory {
        APPLICATION,
        INFRASTRUCTURE,
        DOCUMENTATION,
        OTHER
    }
}
package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Problem - Root cause analysis and resolution management
 * Tracks underlying issues causing incidents
 */
@Entity
@Table(name = "problem")
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("PROBLEM")
public class Problem extends Ticket {

    @Column(name = "problem_number", unique = true, length = 50)
    private String problemNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_type", length = 50)
    private ProblemType problemType = ProblemType.SOFTWARE;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "work_around", columnDefinition = "TEXT")
    private String workAround;

    @Column(name = "impact_analysis", columnDefinition = "TEXT")
    private String impactAnalysis;

    @Column(name = "related_change_id")
    private Long relatedChangeId;

    public Problem(String title, Long organizationId) {
        super(title, organizationId);
    }

    public enum ProblemType {
        SOFTWARE,
        HARDWARE,
        NETWORK,
        PROCESS,
        DOCUMENTATION,
        OTHER
    }
}
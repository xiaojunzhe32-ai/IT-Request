package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * KnownError - Documented problems with workarounds and solutions
 * Links to Problem management and helps resolve incidents faster
 */
@Entity
@Table(name = "known_error")
@Getter
@Setter
@NoArgsConstructor
public class KnownError extends BaseEntity {

    @Column(name = "error_code", unique = true, length = 100, nullable = false)
    private String errorCode;

    @Column(name = "problem_id")
    private Long problemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", insertable = false, updatable = false)
    private Problem problem;

    @Column(name = "symptoms", columnDefinition = "TEXT", nullable = false)
    private String symptoms;

    @Column(name = "cause", columnDefinition = "TEXT")
    private String cause;

    @Column(name = "workaround", columnDefinition = "TEXT")
    private String workaround;

    @Column(name = "solution", columnDefinition = "TEXT")
    private String solution;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_type", length = 50)
    private ErrorType errorType = ErrorType.SOFTWARE;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20)
    private Severity severity = Severity.MEDIUM;

    @Column(name = "apply_to_all")
    private Boolean applyToAll = false;

    @Column(name = "first_detected")
    private LocalDateTime firstDetected;

    @Column(name = "last_occurrence")
    private LocalDateTime lastOccurrence;

    @Column(name = "occurrence_count")
    private Integer occurrenceCount = 0;

    public KnownError(String errorCode, String symptoms) {
        this.errorCode = errorCode;
        this.symptoms = symptoms;
    }

    public enum ErrorType {
        SOFTWARE,
        HARDWARE,
        NETWORK,
        CONFIGURATION,
        PROCESS,
        OTHER
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
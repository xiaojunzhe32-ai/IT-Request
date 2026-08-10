package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ChangeTask - Individual tasks within a change request
 * Tracks specific implementation steps
 */
@Entity
@Table(name = "change_task")
@Getter
@Setter
@NoArgsConstructor
public class ChangeTask extends BaseEntity {

    @Column(name = "change_id", nullable = false)
    private Long changeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_id", insertable = false, updatable = false)
    private ChangeRequest changeRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", length = 50)
    private TaskType taskType = TaskType.IMPLEMENTATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", length = 50)
    private TaskStatus taskStatus = TaskStatus.PENDING;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", insertable = false, updatable = false)
    private Person assignee;

    @Column(name = "planned_start_date")
    private LocalDateTime plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDateTime plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public ChangeTask(Long changeId, String name) {
        this.changeId = changeId;
        this.setName(name);
    }

    public enum TaskType {
        PLANNING,
        REVIEW,
        IMPLEMENTATION,
        TESTING,
        ROLLBACK
    }

    public enum TaskStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        SKIPPED
    }
}
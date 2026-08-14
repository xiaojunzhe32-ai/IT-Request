package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * UserRequest - Customer request for a service
 * Typically created by end users through the portal
 */
@Entity
@Table(name = "user_request")
@DiscriminatorValue("request")
@Getter
@Setter
@NoArgsConstructor
public class UserRequest extends Ticket {

    @Column(name = "origin", length = 50)
    private String origin = "portal"; // portal, email, phone, chat

    /** 人类可读请求编号，如 REQ-10001 */
    @Column(name = "request_no", length = 50, unique = true)
    private String requestNo;

    /** 受影响的服务/系统（请求人填写） */
    @Column(name = "affected_service", length = 255)
    private String affectedService;

    @Column(name = "description_html", columnDefinition = "TEXT")
    private String descriptionHtml;

    /** 问题发生时间（请求人填写） */
    @Column(name = "occurrence_time")
    private java.time.LocalDateTime occurrenceTime;

    /** 期望解决时间（请求人填写） */
    @Column(name = "requested_resolution_time")
    private java.time.LocalDateTime requestedResolutionTime;

    @Column(name = "approver_id")
    private Long approverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", insertable = false, updatable = false)
    private Person approver;

    @Column(name = "approval_date")
    private java.time.LocalDateTime approvalDate;

    @Column(name = "expected_date")
    private java.time.LocalDateTime expectedDate;

    @Column(name = "related_problem_id")
    private Long relatedProblemId;

    @Column(name = "related_change_id")
    private Long relatedChangeId;

    public UserRequest(String title, Long organizationId) {
        super(title, organizationId);
    }
}

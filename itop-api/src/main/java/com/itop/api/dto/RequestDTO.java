package com.itop.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 请求 DTO，对齐前端 mockRequests 模型。
 * status 取值: New / Assigned / In Progress / Testing / Resolved / User Test Failed / Closed
 * priority 取值: Low / Medium / High / Critical
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {

    private Long id;
    private String requestNo;
    private String title;
    private String description;
    private String descriptionHtml;
    /** 请求类型，如 "Application Issue" */
    private String type;
    private String affectedService;
    private String priority;
    private String status;
    private String origin;

    // 解析后的名称（供前端直接展示）
    private String requester;
    private String requesterOrg;
    private String assignedTeam;
    private String assignee;
    private String tester;

    // 原始 ID（供前端操作）
    private Long organizationId;
    private Long callerId;
    private Long agentId;
    private Long teamId;
    private Long testerId;

    // 时间线
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startDate;
    private LocalDateTime lastUpdateDate;
    private LocalDateTime submittedToTestingAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime occurrenceTime;
    private LocalDateTime requestedResolutionTime;

    // SLA
    private LocalDateTime ttoDeadline;
    private LocalDateTime ttrDeadline;
    private Long slaId;

    private List<RequestCommentDTO> comments;
    private List<RequestHistoryDTO> history;
    private List<AttachmentDTO> attachments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestCommentDTO {
        private Long id;
        private String author;
        private String role;
        private LocalDateTime time;
        private String message;
        private Boolean internal;
        private List<AttachmentDTO> attachments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestHistoryDTO {
        private Long id;
        private LocalDateTime time;
        private String actor;
        private String action;
        private String detail;
        private Boolean internal;
    }
}

package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * RequestComment - 请求留言/工作备注，映射 ticket_log 表。
 * log_type: PUBLIC（公开留言，请求人可见）/ INTERNAL（内部工作备注，仅 IT 可见）/ SYSTEM（系统日志）
 */
@Entity
@Table(name = "ticket_log")
@Getter
@Setter
@NoArgsConstructor
public class RequestComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "ticket_type", length = 50)
    private String ticketType = "ticket";

    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", length = 20, nullable = false)
    private LogType logType = LogType.PUBLIC;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (ticketType == null) {
            ticketType = "ticket";
        }
    }

    public enum LogType {
        PUBLIC,
        INTERNAL,
        SYSTEM
    }

    public RequestComment(Long ticketId, String message, LogType logType, Long userId, String username) {
        this.ticketId = ticketId;
        this.message = message;
        this.logType = logType != null ? logType : LogType.PUBLIC;
        this.userId = userId;
        this.username = username;
    }
}

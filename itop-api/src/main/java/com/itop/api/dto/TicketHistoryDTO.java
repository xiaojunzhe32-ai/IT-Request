package com.itop.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketHistoryDTO {
    private Long id;
    private Long ticketId;
    private String action;
    private String oldStatus;
    private String newStatus;
    private Long oldAgentId;
    private Long newAgentId;
    private Long oldTeamId;
    private Long newTeamId;
    private String comment;
    private Long userId;
    private LocalDateTime createdAt;
}
package com.itop.api.service;

import com.itop.core.entity.Ticket;
import com.itop.core.entity.TicketHistory;
import com.itop.core.repository.TicketHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketHistoryService {

    private final TicketHistoryRepository historyRepository;

    @Transactional
    public void logCreation(Ticket ticket, Long userId) {
        TicketHistory history = TicketHistory.builder()
                .ticketId(ticket.getId())
                .action("created")
                .newStatus(ticket.getTicketStatus() != null ? ticket.getTicketStatus().name() : null)
                .userId(userId)
                .build();
        historyRepository.save(history);
    }

    @Transactional
    public void logAssignment(Ticket ticket, Long oldAgentId, Long oldTeamId, Long userId) {
        TicketHistory history = TicketHistory.builder()
                .ticketId(ticket.getId())
                .action(oldAgentId == null && oldTeamId == null ? "assigned" : "reassigned")
                .oldStatus(ticket.getTicketStatus() != null ? ticket.getTicketStatus().name() : null)
                .newStatus(Ticket.TicketStatus.ASSIGNED.name())
                .oldAgentId(oldAgentId)
                .newAgentId(ticket.getAgentId())
                .oldTeamId(oldTeamId)
                .newTeamId(ticket.getTeamId())
                .userId(userId)
                .build();
        historyRepository.save(history);
    }

    @Transactional
    public void logStatusChange(Ticket ticket, String oldStatus, String newStatus, Long userId) {
        TicketHistory history = TicketHistory.builder()
                .ticketId(ticket.getId())
                .action("status_changed")
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .userId(userId)
                .build();
        historyRepository.save(history);
    }

    @Transactional
    public void logResolution(Ticket ticket, Long userId) {
        TicketHistory history = TicketHistory.builder()
                .ticketId(ticket.getId())
                .action("resolved")
                .oldStatus(Ticket.TicketStatus.ASSIGNED.name())
                .newStatus(Ticket.TicketStatus.RESOLVED.name())
                .userId(userId)
                .build();
        historyRepository.save(history);
    }

    @Transactional
    public void logClosure(Ticket ticket, Long userId) {
        TicketHistory history = TicketHistory.builder()
                .ticketId(ticket.getId())
                .action("closed")
                .oldStatus(Ticket.TicketStatus.RESOLVED.name())
                .newStatus(Ticket.TicketStatus.CLOSED.name())
                .userId(userId)
                .build();
        historyRepository.save(history);
    }

    @Transactional
    public void logReopen(Ticket ticket, Long userId) {
        TicketHistory history = TicketHistory.builder()
                .ticketId(ticket.getId())
                .action("reopened")
                .oldStatus(Ticket.TicketStatus.CLOSED.name())
                .newStatus(Ticket.TicketStatus.NEW.name())
                .userId(userId)
                .build();
        historyRepository.save(history);
    }
}
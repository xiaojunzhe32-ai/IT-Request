package com.itop.core.repository;

import com.itop.core.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>,
        JpaSpecificationExecutor<Ticket> {

    List<Ticket> findByOrganizationId(Long organizationId);

    List<Ticket> findByStatus(String status);

    Page<Ticket> findByStatus(String status, Pageable pageable);

    List<Ticket> findByCallerId(Long callerId);

    List<Ticket> findByAgentId(Long agentId);

    List<Ticket> findByTeamId(Long teamId);

    Page<Ticket> findByTeamId(Long teamId, Pageable pageable);

    List<Ticket> findByFinalClass(String finalClass);

    Page<Ticket> findByFinalClass(String finalClass, Pageable pageable);

    Page<Ticket> findByTicketStatus(Ticket.TicketStatus ticketStatus, Pageable pageable);
}
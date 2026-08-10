package com.itop.core.repository;

import com.itop.core.entity.TicketCI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketCIRepository extends JpaRepository<TicketCI, TicketCI.TicketCIId> {

    /**
     * Find all CI associations for a ticket
     */
    List<TicketCI> findByTicketId(Long ticketId);

    /**
     * Find all ticket associations for a CI
     */
    List<TicketCI> findByCiId(Long ciId);

    /**
     * Find specific ticket-CI association
     */
    TicketCI findByTicketIdAndCiId(Long ticketId, Long ciId);

    /**
     * Delete all CI associations for a ticket
     */
    @Modifying
    @Query("DELETE FROM TicketCI t WHERE t.ticketId = :ticketId")
    void deleteByTicketId(@Param("ticketId") Long ticketId);

    /**
     * Delete all ticket associations for a CI
     */
    @Modifying
    @Query("DELETE FROM TicketCI t WHERE t.ciId = :ciId")
    void deleteByCiId(@Param("ciId") Long ciId);

    /**
     * Count CI associations for a ticket
     */
    long countByTicketId(Long ticketId);

    /**
     * Count ticket associations for a CI
     */
    long countByCiId(Long ciId);
}
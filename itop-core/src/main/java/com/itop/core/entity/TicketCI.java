package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Ticket-CI Association Entity
 * Links tickets to configuration items with relation type
 */
@Entity
@Table(name = "ticket_ci")
@IdClass(TicketCI.TicketCIId.class)
@Getter
@Setter
@NoArgsConstructor
public class TicketCI {

    @Id
    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Id
    @Column(name = "ci_id", nullable = false)
    private Long ciId;

    @Column(name = "relation_type", length = 50)
    private String relationType = "affects";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ci_id", insertable = false, updatable = false)
    private ConfigurationItem configurationItem;

    public TicketCI(Long ticketId, Long ciId, String relationType) {
        this.ticketId = ticketId;
        this.ciId = ciId;
        this.relationType = relationType;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Composite primary key class for TicketCI
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class TicketCIId implements java.io.Serializable {
        private Long ticketId;
        private Long ciId;

        public TicketCIId(Long ticketId, Long ciId) {
            this.ticketId = ticketId;
            this.ciId = ciId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TicketCIId that = (TicketCIId) o;

            if (ticketId != null ? !ticketId.equals(that.ticketId) : that.ticketId != null) return false;
            return ciId != null ? ciId.equals(that.ciId) : that.ciId == null;
        }

        @Override
        public int hashCode() {
            int result = ticketId != null ? ticketId.hashCode() : 0;
            result = 31 * result + (ciId != null ? ciId.hashCode() : 0);
            return result;
        }
    }
}
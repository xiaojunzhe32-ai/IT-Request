package com.itop.api.service;

import com.itop.core.entity.SLA;
import com.itop.core.entity.SLAPriorityMatrix;
import com.itop.core.entity.Ticket;
import com.itop.core.repository.SLAPriorityMatrixRepository;
import com.itop.core.repository.SLARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SLAService {

    private final SLARepository slaRepository;
    private final SLAPriorityMatrixRepository priorityMatrixRepository;

    @Transactional
    public void applySLA(Ticket ticket) {
        SLA sla = resolveSLA(ticket.getOrganizationId(), ticket.getPriority());
        if (sla == null) {
            return;
        }

        ticket.setSlaId(sla.getId());

        LocalDateTime now = LocalDateTime.now();
        Integer ttoHours = sla.getTtoHours() != null ? sla.getTtoHours() : 4;
        Integer ttrHours = sla.getTtrHours() != null ? sla.getTtrHours() : 8;

        SLAPriorityMatrix matrix = resolvePriorityMatrix(sla, ticket.getUrgency(), ticket.getImpact());
        if (matrix != null) {
            if (matrix.getTtoHours() != null) {
                ttoHours = matrix.getTtoHours();
            }
            if (matrix.getTtrHours() != null) {
                ttrHours = matrix.getTtrHours();
            }
        }

        ticket.setTtoDeadline(calculateDeadline(now, ttoHours));
        ticket.setTtrDeadline(calculateDeadline(now, ttrHours));
    }

    private SLA resolveSLA(Long orgId, String priority) {
        SLA sla = null;
        if (orgId != null) {
            sla = slaRepository.findByOrganizationIdAndIsDefaultTrue(orgId).stream()
                    .findFirst()
                    .orElse(null);
        }
        if (sla == null) {
            sla = slaRepository.findByIsDefaultTrue().stream().findFirst().orElse(null);
        }
        if (sla == null) {
            sla = slaRepository.findAll().stream().findFirst().orElse(null);
        }
        return sla;
    }

    private SLAPriorityMatrix resolvePriorityMatrix(SLA sla, String urgency, String impact) {
        if (urgency == null || impact == null) {
            return null;
        }
        return priorityMatrixRepository.findBySlaIdAndUrgencyAndImpact(sla.getId(), urgency, impact).orElse(null);
    }

    private LocalDateTime calculateDeadline(LocalDateTime start, Integer hours) {
        if (hours == null || hours <= 0) {
            return null;
        }
        return start.plus(hours, ChronoUnit.HOURS);
    }

    public boolean isOverdue(LocalDateTime deadline) {
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }

    public long getRemainingMinutes(LocalDateTime deadline) {
        if (deadline == null) {
            return -1;
        }
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), deadline);
    }
}
package com.itop.api.controller;

import com.itop.api.security.SecurityUtils;
import com.itop.api.service.AuditService;
import com.itop.api.service.SLAService;
import com.itop.api.service.TicketHistoryService;
import com.itop.common.dto.ApiResponse;
import com.itop.api.dto.PageResponse;
import com.itop.api.dto.TicketDTO;
import com.itop.api.dto.TicketHistoryDTO;
import com.itop.core.entity.Incident;
import com.itop.core.entity.Ticket;
import com.itop.core.entity.TicketHistory;
import com.itop.core.entity.UserRequest;
import com.itop.core.repository.TicketHistoryRepository;
import com.itop.core.repository.TicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Ticket", description = "Ticket management APIs")
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository historyRepository;
    private final SecurityUtils securityUtils;
    private final TicketHistoryService historyService;
    private final AuditService auditService;
    private final SLAService slaService;

    @Operation(summary = "Get all tickets", description = "Retrieve a paginated list of all tickets")
    @GetMapping
    @PreAuthorize("@securityUtils.hasPermission('ticket:read')")
    public ResponseEntity<ApiResponse<PageResponse<TicketDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "status", required = false) String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));

        // 数据隔离：非全局管理员仅可见其可访问组织下的工单
        Set<Long> orgIds = securityUtils.getAccessibleOrgIds();

        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (orgIds != null) {
                predicates.add(root.get("organizationId").in(orgIds));
            }
            if (type != null && !type.isEmpty()) {
                predicates.add(cb.equal(root.get("finalClass"), type));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("ticketStatus"), Ticket.TicketStatus.valueOf(status)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Ticket> ticketPage = ticketRepository.findAll(spec, pageable);

        List<TicketDTO> dtos = ticketPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<TicketDTO> response = PageResponse.of(dtos, page, size, ticketPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get ticket by ID", description = "Retrieve a single ticket by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('ticket:read')")
    public ResponseEntity<ApiResponse<TicketDTO>> getById(@PathVariable("id") Long id) {
        return ticketRepository.findById(id)
                .map(ticket -> ResponseEntity.ok(ApiResponse.success(toDTO(ticket))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Ticket not found")));
    }

    @Operation(summary = "Create user request", description = "Create a new user request")
    @PostMapping("/user-requests")
    @PreAuthorize("@securityUtils.hasPermission('ticket:create')")
    public ResponseEntity<ApiResponse<TicketDTO>> createUserRequest(@RequestBody TicketDTO dto) {
        UserRequest request = new UserRequest(dto.getTitle(), dto.getOrganizationId());
        request.setDescription(dto.getDescription());
        request.setImpact(dto.getImpact() != null ? dto.getImpact() : "2");
        request.setUrgency(dto.getUrgency() != null ? dto.getUrgency() : "2");
        request.setPriority(dto.getPriority() != null ? dto.getPriority() : "2");
        Long callerId = securityUtils.getCurrentUserId();
        if (callerId != null) {
            request.setCallerId(callerId);
        }

        slaService.applySLA(request);
        request = (UserRequest) ticketRepository.save(request);
        Long userId = securityUtils.getCurrentUserId();
        historyService.logCreation(request, userId);
        auditService.logCreate("Ticket", request.getId(), "Created UserRequest: " + request.getTitle());
        return ResponseEntity.ok(ApiResponse.success("User request created", toDTO(request)));
    }

    @Operation(summary = "Create incident", description = "Create a new incident")
    @PostMapping("/incidents")
    @PreAuthorize("@securityUtils.hasPermission('ticket:create')")
    public ResponseEntity<ApiResponse<TicketDTO>> createIncident(@RequestBody TicketDTO dto) {
        Incident incident = new Incident(dto.getTitle(), dto.getOrganizationId());
        incident.setDescription(dto.getDescription());
        incident.setImpact(dto.getImpact() != null ? dto.getImpact() : "1");
        incident.setUrgency(dto.getUrgency() != null ? dto.getUrgency() : "1");
        incident.setPriority(dto.getPriority() != null ? dto.getPriority() : "1");
        Long callerId = securityUtils.getCurrentUserId();
        if (callerId != null) {
            incident.setCallerId(callerId);
        }

        slaService.applySLA(incident);
        incident = (Incident) ticketRepository.save(incident);
        Long userId = securityUtils.getCurrentUserId();
        historyService.logCreation(incident, userId);
        auditService.logCreate("Ticket", incident.getId(), "Created Incident: " + incident.getTitle());
        return ResponseEntity.ok(ApiResponse.success("Incident created", toDTO(incident)));
    }

    @Operation(summary = "Update ticket status", description = "Update ticket status")
    @PutMapping("/{id}/status")
    @PreAuthorize("@securityUtils.hasAnyPermission('ticket:resolve','ticket:close','ticket:reopen')")
    public ResponseEntity<ApiResponse<TicketDTO>> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody StatusUpdateRequest request) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    String oldStatus = ticket.getTicketStatus() != null ? ticket.getTicketStatus().name() : null;
                    if (request.getStatus() != null) {
                        ticket.setTicketStatus(Ticket.TicketStatus.valueOf(request.getStatus()));
                    }
                    ticket.setLastUpdateDate(java.time.LocalDateTime.now());
                    Ticket saved = ticketRepository.save(ticket);
                    Long userId = securityUtils.getCurrentUserId();
                    historyService.logStatusChange(saved, oldStatus, saved.getTicketStatus().name(), userId);
                    auditService.logStatusChange("Ticket", saved.getId(), oldStatus, saved.getTicketStatus().name(), "Status changed");
                    return ResponseEntity.ok(ApiResponse.success("Status updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Ticket not found")));
    }

    @Operation(summary = "Assign ticket", description = "Assign ticket to an agent or team")
    @PutMapping("/{id}/assign")
    @PreAuthorize("@securityUtils.hasPermission('ticket:assign')")
    public ResponseEntity<ApiResponse<TicketDTO>> assign(
            @PathVariable("id") Long id,
            @RequestBody AssignmentRequest request) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    Long oldAgentId = ticket.getAgentId();
                    Long oldTeamId = ticket.getTeamId();
                    if (request.getAgentId() != null) {
                        ticket.setAgentId(request.getAgentId());
                    }
                    if (request.getTeamId() != null) {
                        ticket.setTeamId(request.getTeamId());
                    }
                    ticket.setTicketStatus(Ticket.TicketStatus.ASSIGNED);
                    ticket.setLastUpdateDate(java.time.LocalDateTime.now());
                    Ticket saved = ticketRepository.save(ticket);
                    Long userId = securityUtils.getCurrentUserId();
                    historyService.logAssignment(saved, oldAgentId, oldTeamId, userId);
                    auditService.logAssign("Ticket", saved.getId(), oldAgentId, saved.getAgentId(), "Ticket assigned");
                    return ResponseEntity.ok(ApiResponse.success("Ticket assigned", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Ticket not found")));
    }

    @Operation(summary = "Resolve ticket", description = "Mark ticket as resolved")
    @PutMapping("/{id}/resolve")
    @PreAuthorize("@securityUtils.hasPermission('ticket:resolve')")
    public ResponseEntity<ApiResponse<TicketDTO>> resolve(
            @PathVariable("id") Long id,
            @RequestBody ResolutionRequest request) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    ticket.setSolution(request.getSolution());
                    ticket.setTicketStatus(Ticket.TicketStatus.RESOLVED);
                    ticket.setResolution(Ticket.ResolutionStatus.RESOLVED);
                    ticket.setLastUpdateDate(java.time.LocalDateTime.now());
                    Ticket saved = ticketRepository.save(ticket);
                    Long userId = securityUtils.getCurrentUserId();
                    historyService.logResolution(saved, userId);
                    auditService.logResolve("Ticket", saved.getId(), request.getSolution(), "Ticket resolved");
                    return ResponseEntity.ok(ApiResponse.success("Ticket resolved", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Ticket not found")));
    }

    @Operation(summary = "Close ticket", description = "Close a ticket")
    @PutMapping("/{id}/close")
    @PreAuthorize("@securityUtils.hasPermission('ticket:close')")
    public ResponseEntity<ApiResponse<TicketDTO>> close(@PathVariable("id") Long id) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    ticket.setTicketStatus(Ticket.TicketStatus.CLOSED);
                    ticket.setCloseDate(java.time.LocalDateTime.now());
                    ticket.setLastUpdateDate(java.time.LocalDateTime.now());
                    Ticket saved = ticketRepository.save(ticket);
                    Long userId = securityUtils.getCurrentUserId();
                    historyService.logClosure(saved, userId);
                    auditService.logClose("Ticket", saved.getId(), "Ticket closed");
                    return ResponseEntity.ok(ApiResponse.success("Ticket closed", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Ticket not found")));
    }

    @Operation(summary = "Get ticket history", description = "Get history log for a ticket")
    @GetMapping("/{id}/history")
    @PreAuthorize("@securityUtils.hasPermission('ticket:read')")
    public ResponseEntity<ApiResponse<List<TicketHistoryDTO>>> getHistory(@PathVariable("id") Long id) {
        List<TicketHistory> history = historyRepository.findByTicketIdOrderByCreatedAtDesc(id);
        List<TicketHistoryDTO> dtos = history.stream()
                .map(this::toHistoryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    private TicketDTO toDTO(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .organizationId(ticket.getOrganizationId())
                .callerId(ticket.getCallerId())
                .agentId(ticket.getAgentId())
                .teamId(ticket.getTeamId())
                .impact(ticket.getImpact())
                .urgency(ticket.getUrgency())
                .priority(ticket.getPriority())
                .status(ticket.getTicketStatus() != null ? ticket.getTicketStatus().name() : null)
                .resolution(ticket.getResolution() != null ? ticket.getResolution().name() : null)
                .finalClass(ticket.getFinalClass())
                .ticketType(ticket.getTicketType())
                .startDate(ticket.getStartDate())
                .lastUpdateDate(ticket.getLastUpdateDate())
                .closeDate(ticket.getCloseDate())
                .solution(ticket.getSolution())
                .ttoDeadline(ticket.getTtoDeadline())
                .ttrDeadline(ticket.getTtrDeadline())
                .slaId(ticket.getSlaId())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private TicketHistoryDTO toHistoryDTO(TicketHistory history) {
        return TicketHistoryDTO.builder()
                .id(history.getId())
                .ticketId(history.getTicketId())
                .action(history.getAction())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .oldAgentId(history.getOldAgentId())
                .newAgentId(history.getNewAgentId())
                .oldTeamId(history.getOldTeamId())
                .newTeamId(history.getNewTeamId())
                .comment(history.getComment())
                .userId(history.getUserId())
                .createdAt(history.getCreatedAt())
                .build();
    }

    @lombok.Data
    public static class StatusUpdateRequest {
        private String status;
    }

    @lombok.Data
    public static class AssignmentRequest {
        private Long agentId;
        private Long teamId;
    }

    @lombok.Data
    public static class ResolutionRequest {
        private String solution;
    }
}
package com.itop.api.controller;

import com.itop.api.dto.ChangeRequestDTO;
import com.itop.api.dto.PageResponse;
import com.itop.api.security.SecurityUtils;
import com.itop.api.service.AuditService;
import com.itop.api.service.SLAService;
import com.itop.api.service.TicketHistoryService;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.ChangeRequest;
import com.itop.core.entity.Ticket;
import com.itop.core.repository.ChangeRequestRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "ChangeRequest", description = "Change Request management APIs")
@RestController
@RequestMapping("/changes")
@RequiredArgsConstructor
public class ChangeRequestController {

    private final ChangeRequestRepository changeRequestRepository;
    private final SecurityUtils securityUtils;
    private final TicketHistoryService historyService;
    private final AuditService auditService;
    private final SLAService slaService;

    @Operation(summary = "Get all changes", description = "Retrieve a paginated list of all change requests")
    @GetMapping
    @PreAuthorize("@securityUtils.hasPermission('ticket:read')")
    public ResponseEntity<ApiResponse<PageResponse<ChangeRequestDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(name = "orgId", required = false) Long orgId,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "category", required = false) String category) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));

        Set<Long> orgIds = securityUtils.getAccessibleOrgIds();

        Specification<ChangeRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (orgIds != null) {
                predicates.add(root.get("organizationId").in(orgIds));
            }
            if (orgId != null) {
                predicates.add(cb.equal(root.get("organizationId"), orgId));
            }
            if (type != null && !type.isEmpty()) {
                predicates.add(cb.equal(root.get("changeType"), ChangeRequest.ChangeType.valueOf(type)));
            }
            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(root.get("changeCategory"), ChangeRequest.ChangeCategory.valueOf(category)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ChangeRequest> changePage = changeRequestRepository.findAll(spec, pageable);

        List<ChangeRequestDTO> dtos = changePage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<ChangeRequestDTO> response = PageResponse.of(dtos, page, size, changePage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get change by ID", description = "Retrieve a single change request by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('ticket:read')")
    public ResponseEntity<ApiResponse<ChangeRequestDTO>> getById(@PathVariable("id") Long id) {
        return changeRequestRepository.findById(id)
                .map(change -> ResponseEntity.ok(ApiResponse.success(toDTO(change))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Change request not found")));
    }

    @Operation(summary = "Create change", description = "Create a new change request")
    @PostMapping
    @PreAuthorize("@securityUtils.hasPermission('ticket:create')")
    public ResponseEntity<ApiResponse<ChangeRequestDTO>> create(@RequestBody ChangeRequestDTO dto) {
        ChangeRequest change = new ChangeRequest(dto.getTitle(), dto.getOrganizationId());
        change.setChangeNumber(generateChangeNumber());
        change.setDescription(dto.getDescription());

        if (dto.getChangeType() != null) {
            change.setChangeType(ChangeRequest.ChangeType.valueOf(dto.getChangeType()));
        }
        if (dto.getChangeCategory() != null) {
            change.setChangeCategory(ChangeRequest.ChangeCategory.valueOf(dto.getChangeCategory()));
        }
        if (dto.getChangeReason() != null) {
            change.setChangeReason(dto.getChangeReason());
        }
        if (dto.getRiskAssessment() != null) {
            change.setRiskAssessment(dto.getRiskAssessment());
        }
        if (dto.getRollbackPlan() != null) {
            change.setRollbackPlan(dto.getRollbackPlan());
        }
        if (dto.getImplementationPlan() != null) {
            change.setImplementationPlan(dto.getImplementationPlan());
        }
        if (dto.getTestPlan() != null) {
            change.setTestPlan(dto.getTestPlan());
        }
        if (dto.getChangeOwnerId() != null) {
            change.setChangeOwnerId(dto.getChangeOwnerId());
        }
        if (dto.getPlannedStartDate() != null) {
            change.setPlannedStartDate(dto.getPlannedStartDate());
        }
        if (dto.getPlannedEndDate() != null) {
            change.setPlannedEndDate(dto.getPlannedEndDate());
        }
        if (dto.getImpact() != null) {
            change.setImpact(dto.getImpact());
        }
        if (dto.getUrgency() != null) {
            change.setUrgency(dto.getUrgency());
        }
        if (dto.getPriority() != null) {
            change.setPriority(dto.getPriority());
        }
        if (dto.getTeamId() != null) {
            change.setTeamId(dto.getTeamId());
        }

        slaService.applySLA(change);
        change = changeRequestRepository.save(change);
        Long userId = securityUtils.getCurrentUserId();
        historyService.logCreation(change, userId);
        auditService.logCreate("ChangeRequest", change.getId(), "Created Change: " + change.getTitle());
        return ResponseEntity.ok(ApiResponse.success("Change request created", toDTO(change)));
    }

    @Operation(summary = "Update change", description = "Update an existing change request")
    @PutMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('ticket:write')")
    public ResponseEntity<ApiResponse<ChangeRequestDTO>> update(
            @PathVariable("id") Long id,
            @RequestBody ChangeRequestDTO dto) {
        return changeRequestRepository.findById(id)
                .map(change -> {
                    if (dto.getTitle() != null) {
                        change.setTitle(dto.getTitle());
                    }
                    if (dto.getDescription() != null) {
                        change.setDescription(dto.getDescription());
                    }
                    if (dto.getChangeReason() != null) {
                        change.setChangeReason(dto.getChangeReason());
                    }
                    if (dto.getRiskAssessment() != null) {
                        change.setRiskAssessment(dto.getRiskAssessment());
                    }
                    if (dto.getRollbackPlan() != null) {
                        change.setRollbackPlan(dto.getRollbackPlan());
                    }
                    if (dto.getImplementationPlan() != null) {
                        change.setImplementationPlan(dto.getImplementationPlan());
                    }
                    if (dto.getTestPlan() != null) {
                        change.setTestPlan(dto.getTestPlan());
                    }
                    if (dto.getActualStartDate() != null) {
                        change.setActualStartDate(dto.getActualStartDate());
                    }
                    if (dto.getActualEndDate() != null) {
                        change.setActualEndDate(dto.getActualEndDate());
                    }
                    if (dto.getSolution() != null) {
                        change.setSolution(dto.getSolution());
                    }

                    change.setLastUpdateDate(LocalDateTime.now());
                    ChangeRequest saved = changeRequestRepository.save(change);
                    auditService.logUpdate("ChangeRequest", saved.getId(), "attributes", null, null, "Updated Change: " + saved.getTitle());
                    return ResponseEntity.ok(ApiResponse.success("Change request updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Change request not found")));
    }

    @Operation(summary = "Approve change", description = "Approve a change request")
    @PostMapping("/{id}/approve")
    @PreAuthorize("@securityUtils.hasPermission('ticket:approve')")
    public ResponseEntity<ApiResponse<ChangeRequestDTO>> approve(
            @PathVariable("id") Long id,
            @RequestBody ApprovalRequest request) {
        return changeRequestRepository.findById(id)
                .map(change -> {
                    String oldStatus = change.getTicketStatus() != null ? change.getTicketStatus().name() : null;
                    change.setApproverId(request.getApproverId());
                    change.setApprovalDate(LocalDateTime.now());
                    change.setTicketStatus(Ticket.TicketStatus.ASSIGNED);
                    ChangeRequest saved = changeRequestRepository.save(change);
                    Long userId = securityUtils.getCurrentUserId();
                    historyService.logStatusChange(saved, oldStatus, saved.getTicketStatus().name(), userId);
                    auditService.logStatusChange("ChangeRequest", saved.getId(), oldStatus, saved.getTicketStatus().name(), "Change approved");
                    return ResponseEntity.ok(ApiResponse.success("Change request approved", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Change request not found")));
    }

    @Operation(summary = "Start implementation", description = "Mark change as in progress")
    @PostMapping("/{id}/start")
    @PreAuthorize("@securityUtils.hasPermission('ticket:resolve')")
    public ResponseEntity<ApiResponse<ChangeRequestDTO>> start(@PathVariable("id") Long id) {
        return changeRequestRepository.findById(id)
                .map(change -> {
                    String oldStatus = change.getTicketStatus() != null ? change.getTicketStatus().name() : null;
                    change.setTicketStatus(Ticket.TicketStatus.IN_PROGRESS);
                    change.setActualStartDate(LocalDateTime.now());
                    ChangeRequest saved = changeRequestRepository.save(change);
                    Long userId = securityUtils.getCurrentUserId();
                    historyService.logStatusChange(saved, oldStatus, saved.getTicketStatus().name(), userId);
                    auditService.logStatusChange("ChangeRequest", saved.getId(), oldStatus, saved.getTicketStatus().name(), "Change implementation started");
                    return ResponseEntity.ok(ApiResponse.success("Change implementation started", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Change request not found")));
    }

    @Operation(summary = "Complete change", description = "Mark change as completed")
    @PostMapping("/{id}/complete")
    @PreAuthorize("@securityUtils.hasPermission('ticket:resolve')")
    public ResponseEntity<ApiResponse<ChangeRequestDTO>> complete(@PathVariable("id") Long id) {
        return changeRequestRepository.findById(id)
                .map(change -> {
                    String oldStatus = change.getTicketStatus() != null ? change.getTicketStatus().name() : null;
                    change.setTicketStatus(Ticket.TicketStatus.RESOLVED);
                    change.setActualEndDate(LocalDateTime.now());
                    ChangeRequest saved = changeRequestRepository.save(change);
                    Long userId = securityUtils.getCurrentUserId();
                    historyService.logStatusChange(saved, oldStatus, saved.getTicketStatus().name(), userId);
                    auditService.logStatusChange("ChangeRequest", saved.getId(), oldStatus, saved.getTicketStatus().name(), "Change completed");
                    return ResponseEntity.ok(ApiResponse.success("Change completed", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Change request not found")));
    }

    @Operation(summary = "Delete change", description = "Delete a change request by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('ticket:delete')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        if (!changeRequestRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "Change request not found"));
        }
        auditService.logDelete("ChangeRequest", id, "Deleted change request");
        changeRequestRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Change request deleted", null));
    }

    private String generateChangeNumber() {
        return "CHG-" + System.currentTimeMillis();
    }

    private ChangeRequestDTO toDTO(ChangeRequest change) {
        return ChangeRequestDTO.builder()
                .id(change.getId())
                .changeNumber(change.getChangeNumber())
                .changeType(change.getChangeType() != null ? change.getChangeType().name() : null)
                .changeCategory(change.getChangeCategory() != null ? change.getChangeCategory().name() : null)
                .title(change.getTitle())
                .description(change.getDescription())
                .changeReason(change.getChangeReason())
                .riskAssessment(change.getRiskAssessment())
                .rollbackPlan(change.getRollbackPlan())
                .implementationPlan(change.getImplementationPlan())
                .testPlan(change.getTestPlan())
                .organizationId(change.getOrganizationId())
                .changeOwnerId(change.getChangeOwnerId())
                .changeOwnerName(change.getChangeOwner() != null ? change.getChangeOwner().getName() : null)
                .approverId(change.getApproverId())
                .approverName(change.getApprover() != null ? change.getApprover().getName() : null)
                .approvalDate(change.getApprovalDate())
                .plannedStartDate(change.getPlannedStartDate())
                .plannedEndDate(change.getPlannedEndDate())
                .actualStartDate(change.getActualStartDate())
                .actualEndDate(change.getActualEndDate())
                .parentChangeId(change.getParentChangeId())
                .relatedProblemId(change.getRelatedProblemId())
                .impact(change.getImpact())
                .urgency(change.getUrgency())
                .priority(change.getPriority())
                .status(change.getTicketStatus() != null ? change.getTicketStatus().name() : null)
                .solution(change.getSolution())
                .ttoDeadline(change.getTtoDeadline())
                .ttrDeadline(change.getTtrDeadline())
                .slaId(change.getSlaId())
                .startDate(change.getStartDate())
                .lastUpdateDate(change.getLastUpdateDate())
                .closeDate(change.getCloseDate())
                .createdAt(change.getCreatedAt())
                .updatedAt(change.getUpdatedAt())
                .build();
    }

    @lombok.Data
    public static class ApprovalRequest {
        private Long approverId;
    }
}
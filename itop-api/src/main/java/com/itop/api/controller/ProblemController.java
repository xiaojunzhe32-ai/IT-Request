package com.itop.api.controller;

import com.itop.api.dto.PageResponse;
import com.itop.api.dto.ProblemDTO;
import com.itop.api.security.SecurityUtils;
import com.itop.api.service.AuditService;
import com.itop.api.service.SLAService;
import com.itop.api.service.TicketHistoryService;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.Problem;
import com.itop.core.repository.ProblemRepository;
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

@Tag(name = "Problem", description = "Problem management APIs")
@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemRepository problemRepository;
    private final SecurityUtils securityUtils;
    private final TicketHistoryService historyService;
    private final AuditService auditService;
    private final SLAService slaService;

    @Operation(summary = "Get all problems", description = "Retrieve a paginated list of all problems")
    @GetMapping
    @PreAuthorize("@securityUtils.hasPermission('ticket:read')")
    public ResponseEntity<ApiResponse<PageResponse<ProblemDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(name = "orgId", required = false) Long orgId,
            @RequestParam(name = "type", required = false) String type) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));

        Set<Long> orgIds = securityUtils.getAccessibleOrgIds();

        Specification<Problem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (orgIds != null) {
                predicates.add(root.get("organizationId").in(orgIds));
            }
            if (orgId != null) {
                predicates.add(cb.equal(root.get("organizationId"), orgId));
            }
            if (type != null && !type.isEmpty()) {
                predicates.add(cb.equal(root.get("problemType"), Problem.ProblemType.valueOf(type)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Problem> problemPage = problemRepository.findAll(spec, pageable);

        List<ProblemDTO> dtos = problemPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<ProblemDTO> response = PageResponse.of(dtos, page, size, problemPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get problem by ID", description = "Retrieve a single problem by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('ticket:read')")
    public ResponseEntity<ApiResponse<ProblemDTO>> getById(@PathVariable("id") Long id) {
        return problemRepository.findById(id)
                .map(problem -> ResponseEntity.ok(ApiResponse.success(toDTO(problem))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Problem not found")));
    }

    @Operation(summary = "Create problem", description = "Create a new problem")
    @PostMapping
    @PreAuthorize("@securityUtils.hasPermission('ticket:create')")
    public ResponseEntity<ApiResponse<ProblemDTO>> create(@RequestBody ProblemDTO dto) {
        Problem problem = new Problem(dto.getTitle(), dto.getOrganizationId());
        problem.setProblemNumber(generateProblemNumber());
        problem.setDescription(dto.getDescription());

        if (dto.getProblemType() != null) {
            problem.setProblemType(Problem.ProblemType.valueOf(dto.getProblemType()));
        }
        if (dto.getImpact() != null) {
            problem.setImpact(dto.getImpact());
        }
        if (dto.getUrgency() != null) {
            problem.setUrgency(dto.getUrgency());
        }
        if (dto.getPriority() != null) {
            problem.setPriority(dto.getPriority());
        }
        if (dto.getAgentId() != null) {
            problem.setAgentId(dto.getAgentId());
        }
        if (dto.getTeamId() != null) {
            problem.setTeamId(dto.getTeamId());
        }
        if (dto.getRootCause() != null) {
            problem.setRootCause(dto.getRootCause());
        }
        if (dto.getWorkAround() != null) {
            problem.setWorkAround(dto.getWorkAround());
        }

        slaService.applySLA(problem);
        problem = problemRepository.save(problem);
        Long userId = securityUtils.getCurrentUserId();
        historyService.logCreation(problem, userId);
        auditService.logCreate("Problem", problem.getId(), "Created Problem: " + problem.getTitle());
        return ResponseEntity.ok(ApiResponse.success("Problem created", toDTO(problem)));
    }

    @Operation(summary = "Update problem", description = "Update an existing problem")
    @PutMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('ticket:write')")
    public ResponseEntity<ApiResponse<ProblemDTO>> update(
            @PathVariable("id") Long id,
            @RequestBody ProblemDTO dto) {
        return problemRepository.findById(id)
                .map(problem -> {
                    if (dto.getTitle() != null) {
                        problem.setTitle(dto.getTitle());
                    }
                    if (dto.getDescription() != null) {
                        problem.setDescription(dto.getDescription());
                    }
                    if (dto.getProblemType() != null) {
                        problem.setProblemType(Problem.ProblemType.valueOf(dto.getProblemType()));
                    }
                    if (dto.getRootCause() != null) {
                        problem.setRootCause(dto.getRootCause());
                    }
                    if (dto.getWorkAround() != null) {
                        problem.setWorkAround(dto.getWorkAround());
                    }
                    if (dto.getImpactAnalysis() != null) {
                        problem.setImpactAnalysis(dto.getImpactAnalysis());
                    }
                    if (dto.getSolution() != null) {
                        problem.setSolution(dto.getSolution());
                    }

                    problem.setLastUpdateDate(LocalDateTime.now());
                    Problem saved = problemRepository.save(problem);
                    auditService.logUpdate("Problem", saved.getId(), "attributes", null, null, "Updated Problem: " + saved.getTitle());
                    return ResponseEntity.ok(ApiResponse.success("Problem updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Problem not found")));
    }

    @Operation(summary = "Delete problem", description = "Delete a problem by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('ticket:delete')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        if (!problemRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "Problem not found"));
        }
        auditService.logDelete("Problem", id, "Deleted problem");
        problemRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Problem deleted", null));
    }

    private String generateProblemNumber() {
        return "PRB-" + System.currentTimeMillis();
    }

    private ProblemDTO toDTO(Problem problem) {
        return ProblemDTO.builder()
                .id(problem.getId())
                .problemNumber(problem.getProblemNumber())
                .problemType(problem.getProblemType() != null ? problem.getProblemType().name() : null)
                .title(problem.getTitle())
                .description(problem.getDescription())
                .organizationId(problem.getOrganizationId())
                .callerId(problem.getCallerId())
                .agentId(problem.getAgentId())
                .teamId(problem.getTeamId())
                .impact(problem.getImpact())
                .urgency(problem.getUrgency())
                .priority(problem.getPriority())
                .status(problem.getTicketStatus() != null ? problem.getTicketStatus().name() : null)
                .rootCause(problem.getRootCause())
                .workAround(problem.getWorkAround())
                .impactAnalysis(problem.getImpactAnalysis())
                .solution(problem.getSolution())
                .relatedChangeId(problem.getRelatedChangeId())
                .ttoDeadline(problem.getTtoDeadline())
                .ttrDeadline(problem.getTtrDeadline())
                .slaId(problem.getSlaId())
                .startDate(problem.getStartDate())
                .lastUpdateDate(problem.getLastUpdateDate())
                .closeDate(problem.getCloseDate())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .build();
    }
}
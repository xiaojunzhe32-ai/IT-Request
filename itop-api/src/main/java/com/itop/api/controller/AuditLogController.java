package com.itop.api.controller;

import com.itop.common.dto.ApiResponse;
import com.itop.api.dto.AuditLogDTO;
import com.itop.api.dto.PageResponse;
import com.itop.core.entity.AuditLog;
import com.itop.core.repository.AuditLogRepository;
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
import java.util.stream.Collectors;

@Tag(name = "Audit Log", description = "Audit log APIs")
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @Operation(summary = "Get audit logs", description = "Retrieve paginated audit logs with optional filters")
    @GetMapping
    @PreAuthorize("@securityUtils.hasPermission('audit:read')")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "entityType", required = false) String entityType,
            @RequestParam(name = "entityId", required = false) Long entityId,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        LocalDateTime start = startDate != null && !startDate.isEmpty() ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null && !endDate.isEmpty() ? LocalDateTime.parse(endDate) : null;

        AuditLog.Action actionEnum = parseAction(action);

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (entityType != null && !entityType.isEmpty()) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            if (entityId != null) {
                predicates.add(cb.equal(root.get("entityId"), entityId));
            }
            if (actionEnum != null) {
                predicates.add(cb.equal(root.get("action"), actionEnum));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> logPage = auditLogRepository.findAll(spec, pageable);

        List<AuditLogDTO> dtos = logPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<AuditLogDTO> response = PageResponse.of(dtos, page, size, logPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private AuditLog.Action parseAction(String action) {
        if (action == null || action.isEmpty()) {
            return null;
        }
        try {
            return AuditLog.Action.valueOf(action);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private AuditLogDTO toDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction() != null ? log.getAction().name() : null)
                .fieldName(log.getFieldName())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .userId(log.getUserId())
                .username(log.getUsername())
                .ipAddress(log.getIpAddress())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .build();
    }
}

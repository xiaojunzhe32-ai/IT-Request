package com.itop.api.controller;

import com.itop.api.dto.PageResponse;
import com.itop.api.dto.RequestDTO;
import com.itop.api.security.SecurityUtils;
import com.itop.api.service.AuditService;
import com.itop.api.service.RequestService;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.Ticket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Request", description = "IT request workflow APIs")
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    @Operation(summary = "List requests", description = "Retrieve a paginated list of requests with optional filters")
    @GetMapping
    @PreAuthorize("@securityUtils.hasPermission('request:read')")
    public ResponseEntity<ApiResponse<PageResponse<RequestDTO>>> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "teamId", required = false) Long teamId,
            @RequestParam(name = "priority", required = false) String priority,
            @RequestParam(name = "orgId", required = false) Long orgId,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "assigneeId", required = false) Long assigneeId,
            @RequestParam(name = "callerId", required = false) Long callerId) {

        Page<RequestDTO> result = requestService.list(page, size, status, type, teamId, priority,
                orgId, search, assigneeId, callerId);
        PageResponse<RequestDTO> response = PageResponse.of(
                result.getContent(), page, size, result.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get request by ID", description = "Retrieve a single request with comments and history")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('request:read')")
    public ResponseEntity<ApiResponse<RequestDTO>> getById(@PathVariable("id") Long id) {
        if (!requestService.canView(id)) {
            return ResponseEntity.ok(ApiResponse.error(403, "Request is outside your access scope"));
        }
        RequestDTO dto = requestService.getById(id);
        if (dto == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Request not found"));
        }
        // 闂佽崵濮村ú顓㈠绩闁秵鍎戦柣妤€鐗忛々?闂?IT 濠电偛鐡ㄧ划宀勫箹椤愶箑绠熼柧蹇ｅ亞閳绘梻鈧箍鍎遍幊搴ｆ媼閺屻儲鍋℃繛鍡楁捣椤︼箓鏌涢悙瀛樺唉闁哄苯鐗撴俊鎼佸Ψ椤斿彨锝呪攽?
        boolean includeInternal = securityUtils.isITStaff();
        dto.setComments(requestService.getComments(id, includeInternal));
        dto.setHistory(requestService.getHistory(id));
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @Operation(summary = "Create request", description = "Create a new request; auto-routes to a team based on routing rules")
    @PostMapping
    @PreAuthorize("@securityUtils.hasPermission('request:create')")
    public ResponseEntity<ApiResponse<RequestDTO>> create(@RequestBody RequestDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(400, "Title is required"));
        }
        if (dto.getAffectedService() == null || dto.getAffectedService().isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(400, "Affected Service / System is required"));
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(400, "Description is required"));
        }
        if (dto.getOrganizationId() == null) {
            return ResponseEntity.ok(ApiResponse.error(400, "Organization is required"));
        }
        RequestDTO created = requestService.create(dto);
        auditService.logCreate("Request", created.getId(), "Created request: " + created.getTitle());
        return ResponseEntity.ok(ApiResponse.success("Request created", created));
    }

    @Operation(summary = "Assign request", description = "Assign or reassign a request to a team and/or agent")
    @PutMapping("/{id}/assign")
    @PreAuthorize("@securityUtils.hasAnyPermission('request:assign','request:write')")
    public ResponseEntity<ApiResponse<RequestDTO>> assign(
            @PathVariable("id") Long id,
            @RequestBody AssignRequest req) {
        RequestDTO updated = requestService.assign(id, req.getTeamId(), req.getAgentId());
        if (updated == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Request not found"));
        }
        auditService.logAssign("Request", id, null, req.getAgentId(), "Request assigned");
        return ResponseEntity.ok(ApiResponse.success("Request assigned", updated));
    }

    @Operation(summary = "Set tester", description = "Assign an IT tester to a request")
    @PutMapping("/{id}/tester")
    @PreAuthorize("@securityUtils.hasAnyPermission('request:assign','request:write')")
    public ResponseEntity<ApiResponse<RequestDTO>> setTester(
            @PathVariable("id") Long id,
            @RequestBody TesterRequest req) {
        RequestDTO updated = requestService.setTester(id, req.getTesterId());
        if (updated == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Request not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Tester set", updated));
    }

    @Operation(summary = "Transition status", description = "Change the workflow status of a request")
    @PutMapping("/{id}/status")
    @PreAuthorize("@securityUtils.hasAnyPermission('request:transition','request:write') or @securityUtils.isRequester()")
    public ResponseEntity<ApiResponse<RequestDTO>> transition(
            @PathVariable("id") Long id,
            @RequestBody TransitionRequest req) {
        try {
            if (!requestService.canView(id)) {
                return ResponseEntity.ok(ApiResponse.error(403, "Request is outside your access scope"));
            }
            if (securityUtils.isRequester()) {
                if (!requestService.isRequester(id)
                        || !("Closed".equals(req.getStatus()) || "Resolved".equals(req.getStatus()) || "User Test Failed".equals(req.getStatus()))) {
                    return ResponseEntity.ok(ApiResponse.error(403, "Requesters can only close or report failure on their own request"));
                }
                RequestDTO current = requestService.getById(id);
                if (current == null || (!"Resolved".equals(current.getStatus()) && !"Closed".equals(current.getStatus()))) {
                    return ResponseEntity.ok(ApiResponse.error(400, "Requester confirmation is only available for resolved requests"));
                }
            }
            RequestDTO updated = requestService.transition(id, req.getStatus(), req.getNote());
            if (updated == null) {
                return ResponseEntity.ok(ApiResponse.error(404, "Request not found"));
            }
            auditService.logStatusChange("Request", id, null, req.getStatus(),
                    req.getNote() != null ? req.getNote() : "Status changed");
            return ResponseEntity.ok(ApiResponse.success("Status updated", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(ApiResponse.error(400, ex.getMessage()));
        }
    }

    @Operation(summary = "Add comment", description = "Add a public comment or internal note to a request")
    @PostMapping("/{id}/comments")
    @PreAuthorize("@securityUtils.hasPermission('request:comment')")
    public ResponseEntity<ApiResponse<RequestDTO.RequestCommentDTO>> addComment(
            @PathVariable("id") Long id,
            @RequestBody CommentRequest req) {
        if (req.getMessage() == null || req.getMessage().isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(400, "Message is required"));
        }
        if (req.getInternal() != null && req.getInternal() && !securityUtils.isITStaff()) {
            return ResponseEntity.ok(ApiResponse.error(403, "Only IT staff can add internal notes"));
        }
        if (!requestService.canComment(id)) {
            return ResponseEntity.ok(ApiResponse.error(403, "You cannot comment on this request"));
        }
        RequestDTO.RequestCommentDTO comment = requestService.addComment(id, req.getMessage(),
                req.getInternal() != null && req.getInternal());
        return ResponseEntity.ok(ApiResponse.success("Comment added", comment));
    }

    @Operation(summary = "Get request history", description = "Retrieve the audit trail for a request")
    @GetMapping("/{id}/history")
    @PreAuthorize("@securityUtils.hasPermission('request:read')")
    public ResponseEntity<ApiResponse<List<RequestDTO.RequestHistoryDTO>>> getHistory(@PathVariable("id") Long id) {
        if (!requestService.canView(id)) {
            return ResponseEntity.ok(ApiResponse.error(403, "Request is outside your access scope"));
        }
        return ResponseEntity.ok(ApiResponse.success(requestService.getHistory(id)));
    }

    @Operation(summary = "Get request comments", description = "Retrieve comments for a request; internal notes hidden for non-IT users")
    @GetMapping("/{id}/comments")
    @PreAuthorize("@securityUtils.hasPermission('request:read')")
    public ResponseEntity<ApiResponse<List<RequestDTO.RequestCommentDTO>>> getComments(@PathVariable("id") Long id) {
        if (!requestService.canView(id)) {
            return ResponseEntity.ok(ApiResponse.error(403, "Request is outside your access scope"));
        }
        boolean includeInternal = securityUtils.isITStaff();
        return ResponseEntity.ok(ApiResponse.success(requestService.getComments(id, includeInternal)));
    }

    @Operation(summary = "Update request description", description = "Update the plain and sanitized rich request description")
    @PutMapping("/{id}/description")
    @PreAuthorize("@securityUtils.hasAnyPermission('request:create','request:write')")
    public ResponseEntity<ApiResponse<RequestDTO>> updateDescription(
            @PathVariable("id") Long id,
            @RequestBody DescriptionUpdateRequest req) {
        if (!requestService.canComment(id)) {
            return ResponseEntity.ok(ApiResponse.error(403, "You cannot edit this request description"));
        }
        if (req.getDescription() == null || req.getDescription().isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(400, "Description is required"));
        }
        RequestDTO updated = requestService.updateDescription(id, req.getDescription(), req.getDescriptionHtml());
        if (updated == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Request not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Request description updated", updated));
    }

    @Operation(summary = "Get available request statuses", description = "Returns the list of workflow statuses")
    @GetMapping("/statuses")
    @PreAuthorize("@securityUtils.hasPermission('request:read')")
    public ResponseEntity<ApiResponse<List<String>>> getStatuses() {
        return ResponseEntity.ok(ApiResponse.success(List.of(
                "New", "Assigned", "In Progress", "Testing", "Resolved", "User Test Failed", "Closed"
        )));
    }

    @lombok.Data
    public static class AssignRequest {
        private Long teamId;
        private Long agentId;
    }

    @lombok.Data
    public static class TesterRequest {
        private Long testerId;
    }

    @Data
    public static class TransitionRequest {
        private String status;
        private String note;
    }

    @Data
    public static class CommentRequest {
        private String message;
        private Boolean internal;
    }

    @Data
    public static class DescriptionUpdateRequest {
        private String description;
        private String descriptionHtml;
    }
}

package com.itop.api.service;

import com.itop.api.dto.AttachmentDTO;
import com.itop.api.dto.RequestDTO;
import com.itop.api.security.SecurityUtils;
import com.itop.core.entity.*;
import com.itop.core.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 请求工作流服务：列表、详情、创建（含自动路由）、分配、状态流转、留言、历史。
 * 状态模型对齐前端：New / Assigned / In Progress / To be test / Testing / Resolved / User Test Failed / Closed。
 */
@Service
@RequiredArgsConstructor
public class RequestService {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository historyRepository;
    private final RequestCommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;
    private final RoutingRuleService routingRuleService;
    private final SLAService slaService;
    private final SecurityUtils securityUtils;

    private static final Whitelist RICH_TEXT_WHITELIST = new Whitelist()
            .addTags("a", "b", "br", "code", "div", "em", "i", "img", "li", "ol", "p", "pre", "span", "strong", "ul")
            .addAttributes("a", "href")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addAttributes("img", "alt", "data-attachment-id");

    // ---------- 查询 ----------

    @Transactional(readOnly = true)
    public Page<RequestDTO> list(int page, int size, String status, String type, Long teamId,
                                  String priority, Long orgId, String search, Long assigneeId, Long callerId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 仅返回 UserRequest 类型
            predicates.add(cb.equal(root.get("finalClass"), "UserRequest"));

            // 团队数据隔离：非 Admin/ITMD 用户只能看到自己所属团队的工单，
            // 或同团队成员提单的工单（提单人所在团队均可见）
            Set<Long> accessibleTeamIds = securityUtils.getAccessibleTeamIds();
            if (accessibleTeamIds != null) {
                if (accessibleTeamIds.isEmpty()) {
                    // 用户不属于任何团队且非 Admin/ITMD，返回空结果
                    predicates.add(cb.disjunction());
                } else {
                    Set<Long> teamMemberUserIds = teamRepository.findMemberUserIdsByTeamIdIn(accessibleTeamIds);
                    predicates.add(cb.or(
                            root.get("teamId").in(accessibleTeamIds),
                            root.get("callerId").in(teamMemberUserIds)
                    ));
                }
            }

            if (status != null && !status.isEmpty()) {
                Ticket.TicketStatus statusEnum = parseStatusEnum(status);
                if (statusEnum != null) {
                    predicates.add(cb.equal(root.get("ticketStatus"), statusEnum));
                }
            }
            if (type != null && !type.isEmpty()) {
                predicates.add(cb.equal(root.get("name"), type));
            }
            if (teamId != null) {
                predicates.add(cb.equal(root.get("teamId"), teamId));
            }
            if (priority != null && !priority.isEmpty()) {
                predicates.add(cb.equal(root.get("priority"), normalizePriority(priority)));
            }
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("agentId"), assigneeId));
            }
            if (callerId != null) {
                predicates.add(cb.equal(root.get("callerId"), callerId));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                var userRequest = cb.treat(root, UserRequest.class);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(userRequest.get("requestNo")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Ticket> ticketPage = ticketRepository.findAll(spec, pageable);
        return ticketPage.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public RequestDTO getById(Long id) {
        return ticketRepository.findById(id)
                .filter(ticket -> securityUtils.canAccessTicket(ticket.getTeamId(), ticket.getCallerId()))
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean canView(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .filter(ticket -> ticket instanceof UserRequest)
                .map(ticket -> securityUtils.canAccessTicket(ticket.getTeamId(), ticket.getCallerId()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canComment(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .filter(ticket -> canView(ticketId))
                .map(ticket -> securityUtils.isITStaff()
                        || Objects.equals(ticket.getCallerId(), securityUtils.getCurrentUserId()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isRequester(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> Objects.equals(ticket.getCallerId(), securityUtils.getCurrentUserId()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<RequestDTO.RequestHistoryDTO> getHistory(Long ticketId) {
        if (!canView(ticketId)) {
            return Collections.emptyList();
        }
        return historyRepository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(this::toHistoryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequestDTO.RequestCommentDTO> getComments(Long ticketId, boolean includeInternal) {
        if (!canView(ticketId)) {
            return Collections.emptyList();
        }
        List<RequestComment> comments = includeInternal
                ? commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                : commentRepository.findByTicketIdAndLogTypeNotOrderByCreatedAtAsc(ticketId, RequestComment.LogType.INTERNAL);
        return comments.stream().map(c -> toCommentDTO(c, includeInternal)).collect(Collectors.toList());
    }

    // ---------- 创建 ----------

    @Transactional
    public RequestDTO create(RequestDTO dto) {
        UserRequest request = new UserRequest(dto.getTitle(), dto.getOrganizationId());
        request.setDescription(dto.getDescription());
        request.setDescriptionHtml(sanitizeRichText(dto.getDescriptionHtml()));
        request.setName(dto.getType() != null ? dto.getType() : "Other");
        request.setPriority(normalizePriority(dto.getPriority()));
        deriveImpactUrgency(request, request.getPriority());
        request.setAffectedService(dto.getAffectedService());
        request.setOccurrenceTime(dto.getOccurrenceTime());
        request.setRequestedResolutionTime(dto.getRequestedResolutionTime());
        request.setOrigin(dto.getOrigin() != null ? dto.getOrigin() : "portal");

        Long callerId = securityUtils.getCurrentUserId();
        if (callerId != null) {
            request.setCallerId(callerId);
        }

        // 团队分配：优先使用前端传入的 teamId，没有才走路由规则
        Long targetTeamId = dto.getTeamId();
        if (targetTeamId == null) {
            targetTeamId = routingRuleService.matchRequest(
                    dto.getOrganizationId(), dto.getType(), request.getPriority(), dto.getAffectedService());
        }
        request.setTicketStatus(Ticket.TicketStatus.NEW);
        if (targetTeamId != null) {
            request.setTeamId(targetTeamId);
        }

        slaService.applySLA(request);
        request = (UserRequest) ticketRepository.save(request);

        // 生成请求编号
        request.setRequestNo(generateRequestNo(request));
        request = (UserRequest) ticketRepository.save(request);

        Long userId = securityUtils.getCurrentUserId();
        String actor = resolveUserName(userId);
        logHistory(request.getId(), "REQUEST_CREATED", actor, "Created request from Portal.", false, userId,
                null, statusName(request.getTicketStatus()), null, null, null, null);
        if (targetTeamId != null) {
            String teamName = resolveTeamName(targetTeamId);
            logHistory(request.getId(), "REQUEST_ROUTED", "System",
                    "Routed to " + (teamName != null ? teamName : "team " + targetTeamId) + ".", false, null,
                    statusName(request.getTicketStatus()), statusName(request.getTicketStatus()),
                    null, null, null, targetTeamId);
        }

        return toDTO(request);
    }

    // ---------- 分配 ----------

    @Transactional
    public RequestDTO assign(Long id, Long teamId, Long agentId) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) {
            return null;
        }
        if (!securityUtils.canAccessTicket(ticket.getTeamId(), ticket.getCallerId())) {
            return null;
        }
        // Only check team transfer permission when actually changing teams
        if (teamId != null && !teamId.equals(ticket.getTeamId()) && !securityUtils.canTransferToTeam(teamId)) {
            return null;
        }
        Long oldAgentId = ticket.getAgentId();
        Long oldTeamId = ticket.getTeamId();
        Ticket.TicketStatus oldStatus = ticket.getTicketStatus();

        if (teamId != null) {
            ticket.setTeamId(teamId);
        }
        if (agentId != null) {
            ticket.setAgentId(agentId);
        }
        if (ticket.getTicketStatus() == Ticket.TicketStatus.NEW) {
            ticket.setTicketStatus(Ticket.TicketStatus.ASSIGNED);
        }
        ticket.setLastUpdateDate(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);

        Long userId = securityUtils.getCurrentUserId();
        String actor = resolveUserName(userId);
        String assigneeName = resolveUserName(agentId);
        String teamName = resolveTeamName(ticket.getTeamId());
        String detail = String.format("Assigned to %s in %s.",
                assigneeName != null ? assigneeName : "unassigned",
                teamName != null ? teamName : "team");
        logHistory(ticket.getId(), "REQUEST_ASSIGNED", actor, detail, true, userId,
                statusName(oldStatus), statusName(ticket.getTicketStatus()),
                oldAgentId, ticket.getAgentId(), oldTeamId, ticket.getTeamId());

        return toDTO(ticket);
    }

    /** 设置测试人员 */
    @Transactional
    public RequestDTO setTester(Long id, Long testerId) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) {
            return null;
        }
        if (!securityUtils.canAccessTicket(ticket.getTeamId(), ticket.getCallerId())) {
            return null;
        }
        ticket.setTesterId(testerId);
        ticket.setLastUpdateDate(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);
        return toDTO(ticket);
    }

    // ---------- 状态流转 ----------

    @Transactional
    public RequestDTO transition(Long id, String nextStatusLabel, String note) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) {
            return null;
        }
        if (!securityUtils.canAccessTicket(ticket.getTeamId(), ticket.getCallerId())) {
            return null;
        }
        Ticket.TicketStatus nextStatus = parseStatusEnum(nextStatusLabel);
        if (nextStatus == null) {
            throw new IllegalArgumentException("Invalid status: " + nextStatusLabel);
        }
        Ticket.TicketStatus oldStatus = ticket.getTicketStatus();
        String oldLabel = statusLabel(oldStatus);
        ticket.setTicketStatus(nextStatus);
        ticket.setLastUpdateDate(LocalDateTime.now());

        switch (nextStatus) {
            case TESTING -> ticket.setEndDate(LocalDateTime.now()); // 复用 end_date 作为提交测试时间
            case RESOLVED -> {
                ticket.setResolution(Ticket.ResolutionStatus.RESOLVED);
                ticket.setCloseDate(null);
            }
            case CLOSED -> ticket.setCloseDate(LocalDateTime.now());
            case USER_TEST_FAILED -> {
                // 用户测试失败，保留 assignee，回到处理中由技术人员继续
            }
            default -> { }
        }
        ticket = ticketRepository.save(ticket);

        Long userId = securityUtils.getCurrentUserId();
        String actor = resolveUserName(userId);
        String action = nextStatus == Ticket.TicketStatus.USER_TEST_FAILED ? "USER_TEST_FAILED" : "STATUS_CHANGED";
        String detail = note != null && !note.isBlank() ? note : oldLabel + " -> " + statusLabel(nextStatus) + ".";
        boolean internal = nextStatus != Ticket.TicketStatus.USER_TEST_FAILED
                && !Objects.equals(actor, resolveUserName(ticket.getCallerId()));
        logHistory(ticket.getId(), action, actor, detail, internal, userId,
                statusName(oldStatus), statusName(nextStatus), null, null, null, null);

        return toDTO(ticket);
    }

    @Transactional
    public RequestDTO updateDescription(Long id, String description, String descriptionHtml) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (!(ticket instanceof UserRequest request)) {
            return null;
        }
        if (!securityUtils.canAccessTicket(ticket.getTeamId(), ticket.getCallerId())) {
            return null;
        }
        request.setDescription(description);
        request.setDescriptionHtml(sanitizeRichText(descriptionHtml));
        request.setLastUpdateDate(LocalDateTime.now());
        request = (UserRequest) ticketRepository.save(request);

        Long userId = securityUtils.getCurrentUserId();
        logHistory(request.getId(), "DESCRIPTION_UPDATED", resolveUserName(userId),
                "Updated request description and inline evidence.", false, userId);
        return toDTO(request);
    }

    // ---------- 留言 ----------

    @Transactional
    public RequestDTO.RequestCommentDTO addComment(Long ticketId, String message, boolean internal) {
        if (!canView(ticketId)) {
            return null;
        }
        Long userId = securityUtils.getCurrentUserId();
        String username = securityUtils.getCurrentUsername();
        RequestComment.LogType logType = internal ? RequestComment.LogType.INTERNAL : RequestComment.LogType.PUBLIC;
        RequestComment comment = new RequestComment(ticketId, message, logType, userId, username);
        comment = commentRepository.save(comment);

        String actor = resolveUserName(userId);
        logHistory(ticketId, internal ? "INTERNAL_NOTE_ADDED" : "COMMENT_ADDED", actor, message, internal, userId);

        return toCommentDTO(comment, true);
    }

    @Transactional
    public void recordAttachmentAdded(Long ticketId, Long attachmentId, String originalName, boolean internal) {
        Long userId = securityUtils.getCurrentUserId();
        logHistory(ticketId, "ATTACHMENT_ADDED", resolveUserName(userId),
                "Attached " + originalName + " (attachment " + attachmentId + ").", internal, userId);
    }

    // ---------- DTO 映射 ----------

    private RequestDTO toDTO(Ticket ticket) {
        RequestDTO.RequestDTOBuilder b = RequestDTO.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .type(ticket.getName())
                .priority(normalizePriorityLabel(ticket.getPriority()))
                .status(statusLabel(ticket.getTicketStatus()))
                .organizationId(ticket.getOrganizationId())
                .callerId(ticket.getCallerId())
                .agentId(ticket.getAgentId())
                .teamId(ticket.getTeamId())
                .testerId(ticket.getTesterId())
                .requester(resolveUserName(ticket.getCallerId()))
                .requesterOrg(resolveOrgName(ticket.getOrganizationId()))
                .assignedTeam(resolveTeamName(ticket.getTeamId()))
                .assignee(resolveUserName(ticket.getAgentId()))
                .tester(resolveUserName(ticket.getTesterId()))
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .startDate(ticket.getStartDate())
                .lastUpdateDate(ticket.getLastUpdateDate())
                .resolvedAt(ticket.getResolution() == Ticket.ResolutionStatus.RESOLVED ? ticket.getLastUpdateDate() : null)
                .closedAt(ticket.getCloseDate())
                .ttoDeadline(ticket.getTtoDeadline())
                .ttrDeadline(ticket.getTtrDeadline())
                .slaId(ticket.getSlaId());

        if (ticket instanceof UserRequest ur) {
            b.requestNo(ur.getRequestNo())
                    .descriptionHtml(ur.getDescriptionHtml())
                    .affectedService(ur.getAffectedService())
                    .occurrenceTime(ur.getOccurrenceTime())
                    .requestedResolutionTime(ur.getRequestedResolutionTime())
                    .origin(ur.getOrigin());
            // submittedToTestingAt: 用 end_date 近似（提交测试时设置）
            b.submittedToTestingAt(ur.getEndDate());
        }
        b.attachments(attachmentRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("REQUEST", ticket.getId())
                .stream().map(this::toAttachmentDTO).collect(Collectors.toList()));
        return b.build();
    }

    private RequestDTO.RequestCommentDTO toCommentDTO(RequestComment c, boolean includeInternalFlag) {
        return RequestDTO.RequestCommentDTO.builder()
                .id(c.getId())
                .author(c.getUsername() != null ? resolveUserName(c.getUserId(), c.getUsername()) : null)
                .role(resolveUserRole(c.getUserId()))
                .time(c.getCreatedAt())
                .message(c.getMessage())
                .internal(includeInternalFlag && c.getLogType() == RequestComment.LogType.INTERNAL)
                .attachments(attachmentRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("REQUEST_COMMENT", c.getId())
                        .stream().map(this::toAttachmentDTO).collect(Collectors.toList()))
                .build();
    }

    private AttachmentDTO toAttachmentDTO(Attachment attachment) {
        return AttachmentDTO.builder()
                .id(attachment.getId())
                .entityType(attachment.getEntityType())
                .entityId(attachment.getEntityId())
                .fileName(attachment.getFileName())
                .originalName(attachment.getOriginalName())
                .fileSize(attachment.getFileSize())
                .contentType(attachment.getContentType())
                .description(attachment.getDescription())
                .uploaderId(attachment.getUploaderId())
                .uploaderName(attachment.getUploader() != null ? attachment.getUploader().getName() : null)
                .createdAt(attachment.getCreatedAt())
                .updatedAt(attachment.getUpdatedAt())
                .build();
    }

    private RequestDTO.RequestHistoryDTO toHistoryDTO(TicketHistory h) {
        String actor = resolveUserName(h.getUserId());
        if (actor == null) {
            actor = "System";
        }
        return RequestDTO.RequestHistoryDTO.builder()
                .id(h.getId())
                .time(h.getCreatedAt())
                .actor(actor)
                .action(h.getAction() != null ? h.getAction() : h.getNewStatus())
                .detail(h.getComment() != null ? h.getComment() : buildHistoryDetail(h))
                .internal(false)
                .build();
    }

    private String buildHistoryDetail(TicketHistory h) {
        if (h.getOldStatus() != null || h.getNewStatus() != null) {
            return statusLabelRaw(h.getOldStatus()) + " -> " + statusLabelRaw(h.getNewStatus());
        }
        return "";
    }

    // ---------- 历史记录 ----------

    private void logHistory(Long ticketId, String action, String actor, String detail, boolean internal, Long userId) {
        logHistory(ticketId, action, actor, detail, internal, userId, null, null, null, null, null, null);
    }

    private void logHistory(Long ticketId, String action, String actor, String detail, boolean internal, Long userId,
                            String oldStatus, String newStatus,
                            Long oldAgentId, Long newAgentId, Long oldTeamId, Long newTeamId) {
        TicketHistory h = TicketHistory.builder()
                .ticketId(ticketId)
                .action(action)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .oldAgentId(oldAgentId)
                .newAgentId(newAgentId)
                .oldTeamId(oldTeamId)
                .newTeamId(newTeamId)
                .comment(detail)
                .userId(userId)
                .build();
        historyRepository.save(h);
    }

    // ---------- 名称解析 ----------

    private String resolveUserName(Long userId) {
        return resolveUserName(userId, null);
    }

    private String resolveUserName(Long userId, String fallback) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(u -> {
                    if (u.getFirstName() != null && u.getLastName() != null
                            && !u.getFirstName().isBlank() && !u.getLastName().isBlank()) {
                        return u.getFirstName() + " " + u.getLastName();
                    }
                    return u.getUsername();
                })
                .orElse(fallback);
    }

    private String resolveUserRole(Long userId) {
        if (userId == null) return "System";
        return userRepository.findById(userId)
                .flatMap(u -> u.getRoles() == null ? java.util.Optional.empty() : u.getRoles().stream().findFirst())
                .map(Role::getRoleCode)
                .orElse("User");
    }

    private String resolveTeamName(Long teamId) {
        if (teamId == null) return null;
        return teamRepository.findById(teamId).map(Team::getName).orElse(null);
    }

    private String resolveOrgName(Long orgId) {
        if (orgId == null) return null;
        return organizationRepository.findById(orgId).map(Organization::getName).orElse(null);
    }

    // ---------- 状态/优先级工具 ----------

    private String generateRequestNo(Ticket ticket) {
        long sequence = ticket.getId() != null ? 10000 + ticket.getId() : 10000;
        return "REQ-" + sequence;
    }

    /** 前端标签 -> 枚举 */
    public Ticket.TicketStatus parseStatusEnum(String label) {
        if (label == null) return null;
        return switch (label) {
            case "New", "NEW" -> Ticket.TicketStatus.NEW;
            case "Assigned", "ASSIGNED" -> Ticket.TicketStatus.ASSIGNED;
            case "In Progress", "IN_PROGRESS" -> Ticket.TicketStatus.IN_PROGRESS;
            case "To be test", "TO_BE_TEST" -> Ticket.TicketStatus.TO_BE_TEST;
            case "Testing", "TESTING" -> Ticket.TicketStatus.TESTING;
            case "Resolved", "RESOLVED" -> Ticket.TicketStatus.RESOLVED;
            case "User Test Failed", "USER_TEST_FAILED" -> Ticket.TicketStatus.USER_TEST_FAILED;
            case "Closed", "CLOSED" -> Ticket.TicketStatus.CLOSED;
            default -> null;
        };
    }

    /** 枚举 -> 前端标签 */
    public String statusLabel(Ticket.TicketStatus status) {
        if (status == null) return null;
        return switch (status) {
            case NEW -> "New";
            case ASSIGNED -> "Assigned";
            case IN_PROGRESS -> "In Progress";
            case TO_BE_TEST -> "To be test";
            case TESTING -> "Testing";
            case RESOLVED -> "Resolved";
            case USER_TEST_FAILED -> "User Test Failed";
            case CLOSED -> "Closed";
        };
    }

    private String statusName(Ticket.TicketStatus status) {
        return status != null ? status.name() : null;
    }

    private String statusLabelRaw(String raw) {
        if (raw == null) return "";
        Ticket.TicketStatus s = parseStatusEnum(raw);
        return s != null ? statusLabel(s) : raw;
    }

    /** 优先级标准化为存储值（标签） */
    private String normalizePriority(String priority) {
        if (priority == null) return "Medium";
        return switch (priority) {
            case "1", "High" -> "High";
            case "2", "Medium" -> "Medium";
            case "3", "Low" -> "Low";
            case "Critical" -> "Critical";
            default -> priority;
        };
    }

    /** 存储值 -> 前端标签（兼容遗留数字） */
    private String normalizePriorityLabel(String stored) {
        return normalizePriority(stored);
    }

    /** 由优先级推导 impact/urgency，供 SLA 矩阵使用 */
    private void deriveImpactUrgency(Ticket ticket, String priority) {
        switch (priority != null ? priority : "Medium") {
            case "Critical" -> { ticket.setImpact("1"); ticket.setUrgency("1"); }
            case "High" -> { ticket.setImpact("1"); ticket.setUrgency("2"); }
            case "Low" -> { ticket.setImpact("3"); ticket.setUrgency("3"); }
            default -> { ticket.setImpact("2"); ticket.setUrgency("2"); }
        }
    }

    private String sanitizeRichText(String html) {
        if (html == null || html.isBlank()) return null;
        Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
        return Jsoup.clean(html, "", RICH_TEXT_WHITELIST, outputSettings);
    }
}

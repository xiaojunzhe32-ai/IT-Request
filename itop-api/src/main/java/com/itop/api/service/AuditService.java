package com.itop.api.service;

import com.itop.api.security.SecurityUtils;
import com.itop.core.entity.AuditLog;
import com.itop.core.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public void logCreate(String entityType, Long entityId, String description) {
        AuditLog log = new AuditLog(entityType, entityId, AuditLog.Action.CREATE);
        log.setDescription(description);
        populateUserInfo(log);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logUpdate(String entityType, Long entityId, String fieldName, String oldValue, String newValue, String description) {
        AuditLog log = new AuditLog(entityType, entityId, AuditLog.Action.UPDATE);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setDescription(description);
        populateUserInfo(log);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logDelete(String entityType, Long entityId, String description) {
        AuditLog log = new AuditLog(entityType, entityId, AuditLog.Action.DELETE);
        log.setDescription(description);
        populateUserInfo(log);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logStatusChange(String entityType, Long entityId, String oldStatus, String newStatus, String description) {
        AuditLog log = new AuditLog(entityType, entityId, AuditLog.Action.STATUS_CHANGE);
        log.setFieldName("status");
        log.setOldValue(oldStatus);
        log.setNewValue(newStatus);
        log.setDescription(description);
        populateUserInfo(log);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logAssign(String entityType, Long entityId, Long oldAgentId, Long newAgentId, String description) {
        AuditLog log = new AuditLog(entityType, entityId, AuditLog.Action.ASSIGN);
        log.setFieldName("agentId");
        log.setOldValue(oldAgentId != null ? String.valueOf(oldAgentId) : null);
        log.setNewValue(newAgentId != null ? String.valueOf(newAgentId) : null);
        log.setDescription(description);
        populateUserInfo(log);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logResolve(String entityType, Long entityId, String solution, String description) {
        AuditLog log = new AuditLog(entityType, entityId, AuditLog.Action.RESOLVE);
        log.setFieldName("solution");
        log.setNewValue(solution);
        log.setDescription(description);
        populateUserInfo(log);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logClose(String entityType, Long entityId, String description) {
        AuditLog log = new AuditLog(entityType, entityId, AuditLog.Action.CLOSE);
        log.setDescription(description);
        populateUserInfo(log);
        auditLogRepository.save(log);
    }

    private void populateUserInfo(AuditLog log) {
        Long userId = securityUtils.getCurrentUserId();
        String username = securityUtils.getCurrentUsername();

        log.setUserId(userId);
        log.setUsername(username);

        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            log.setIpAddress(getClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
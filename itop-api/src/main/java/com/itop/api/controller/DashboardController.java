package com.itop.api.controller;

import com.itop.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Dashboard", description = "Dashboard statistics APIs")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "Get dashboard statistics", description = "Retrieve all statistics for dashboard")
    @GetMapping("/stats")
    @PreAuthorize("@securityUtils.isAdmin()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 服务器总数
        Long totalServers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM server", Long.class);
        stats.put("totalServers", totalServers);

        // 组织总数
        Long totalOrgs = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM organization", Long.class);
        stats.put("totalOrgs", totalOrgs);

        // CI 总数
        Long totalCIs = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM configuration_item", Long.class);
        stats.put("totalCIs", totalCIs);

        // 活跃用户数
        Long activeUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM \"user\" WHERE status = 'active'", Long.class);
        stats.put("activeUsers", activeUsers);

        // 最近添加的 CI
        List<Map<String, Object>> recentCIs = jdbcTemplate.query(
            "SELECT id, name, final_class, status, created_at FROM configuration_item " +
            "ORDER BY created_at DESC LIMIT 10",
            (rs, rowNum) -> {
                Map<String, Object> ci = new HashMap<>();
                ci.put("id", rs.getLong("id"));
                ci.put("name", rs.getString("name"));
                ci.put("finalClass", rs.getString("final_class"));
                ci.put("status", rs.getString("status"));
                ci.put("createdAt", rs.getTimestamp("created_at"));
                return ci;
            });
        stats.put("recentCIs", recentCIs);

        // 按类型统计 CI
        List<Map<String, Object>> ciByType = jdbcTemplate.query(
            "SELECT final_class, COUNT(*) as count FROM configuration_item GROUP BY final_class ORDER BY count DESC",
            (rs, rowNum) -> {
                Map<String, Object> item = new HashMap<>();
                item.put("type", rs.getString("final_class"));
                item.put("count", rs.getLong("count"));
                return item;
            });
        stats.put("ciByType", ciByType);

        // 按状态统计 CI
        List<Map<String, Object>> ciByStatus = jdbcTemplate.query(
            "SELECT status, COUNT(*) as count FROM configuration_item GROUP BY status",
            (rs, rowNum) -> {
                Map<String, Object> item = new HashMap<>();
                item.put("status", rs.getString("status"));
                item.put("count", rs.getLong("count"));
                return item;
            });
        stats.put("ciByStatus", ciByStatus);

        // 系统运行时间（从第一个组织创建时间开始）
        Map<String, Object> uptime = jdbcTemplate.query(
            "SELECT MIN(created_at) as start_time FROM organization",
            rs -> {
                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("startTime", rs.getTimestamp("start_time"));
                    return result;
                }
                return null;
            });
        stats.put("uptime", uptime);

        // ========== Request / Ticket 统计 ==========
        // 注意：工单工作流状态列是 ticket_status，不是 status（status 是实体的 active/inactive）

        // 工单总数
        Long totalTickets = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ticket WHERE final_class = 'UserRequest'", Long.class);
        stats.put("totalTickets", totalTickets);

        // 按状态统计工单（使用 ticket_status 列，对齐新工作流）
        Long newTickets = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ticket WHERE final_class = 'UserRequest' AND ticket_status = 'NEW'", Long.class);
        stats.put("newTickets", newTickets);

        Long assignedTickets = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ticket WHERE final_class = 'UserRequest' AND ticket_status = 'ASSIGNED'", Long.class);
        stats.put("assignedTickets", assignedTickets);

        Long inProgressTickets = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ticket WHERE final_class = 'UserRequest' AND ticket_status = 'IN_PROGRESS'", Long.class);
        stats.put("inProgressTickets", inProgressTickets);

        Long testingTickets = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ticket WHERE final_class = 'UserRequest' AND ticket_status = 'TESTING'", Long.class);
        stats.put("testingTickets", testingTickets);

        Long resolvedTickets = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ticket WHERE final_class = 'UserRequest' AND ticket_status = 'RESOLVED'", Long.class);
        stats.put("resolvedTickets", resolvedTickets);

        Long userTestFailedTickets = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ticket WHERE final_class = 'UserRequest' AND ticket_status = 'USER_TEST_FAILED'", Long.class);
        stats.put("userTestFailedTickets", userTestFailedTickets);

        Long closedTickets = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ticket WHERE final_class = 'UserRequest' AND ticket_status = 'CLOSED'", Long.class);

        Long activeTeams = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM team WHERE UPPER(status) = 'ACTIVE'", Long.class);
        stats.put("activeTeams", activeTeams);

        Long enabledRoutingRules = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM routing_rule WHERE enabled = true", Long.class);
        stats.put("enabledRoutingRules", enabledRoutingRules);
        stats.put("closedTickets", closedTickets);

        // 按类型统计工单
        List<Map<String, Object>> ticketsByType = jdbcTemplate.query(
            "SELECT final_class, COUNT(*) as count FROM ticket GROUP BY final_class ORDER BY count DESC",
            (rs, rowNum) -> {
                Map<String, Object> item = new HashMap<>();
                item.put("type", rs.getString("final_class"));
                item.put("count", rs.getLong("count"));
                return item;
            });
        stats.put("ticketsByType", ticketsByType);

        // 按状态统计工单
        List<Map<String, Object>> ticketsByStatus = jdbcTemplate.query(
            "SELECT ticket_status, COUNT(*) as count FROM ticket GROUP BY ticket_status ORDER BY count DESC",
            (rs, rowNum) -> {
                Map<String, Object> item = new HashMap<>();
                item.put("status", rs.getString("ticket_status"));
                item.put("count", rs.getLong("count"));
                return item;
            });
        stats.put("ticketsByStatus", ticketsByStatus);

        // 按优先级统计工单
        List<Map<String, Object>> ticketsByPriority = jdbcTemplate.query(
            "SELECT priority, COUNT(*) as count FROM ticket GROUP BY priority ORDER BY priority",
            (rs, rowNum) -> {
                Map<String, Object> item = new HashMap<>();
                item.put("priority", rs.getString("priority"));
                item.put("count", rs.getLong("count"));
                return item;
            });
        stats.put("ticketsByPriority", ticketsByPriority);

        // 最近工单
        List<Map<String, Object>> recentTickets = jdbcTemplate.query(
            "SELECT id, title, final_class, ticket_status, priority, created_at FROM ticket " +
            "ORDER BY created_at DESC LIMIT 10",
            (rs, rowNum) -> {
                Map<String, Object> ticket = new HashMap<>();
                ticket.put("id", rs.getLong("id"));
                ticket.put("title", rs.getString("title"));
                ticket.put("finalClass", rs.getString("final_class"));
                ticket.put("status", rs.getString("ticket_status"));
                ticket.put("priority", rs.getString("priority"));
                ticket.put("createdAt", rs.getTimestamp("created_at"));
                return ticket;
            });
        stats.put("recentTickets", recentTickets);

        // 平均解决时间（小时）
        Double avgResolutionTime = jdbcTemplate.query(
            "SELECT AVG(EXTRACT(EPOCH FROM (close_date - start_date))/3600) as avg_hours " +
            "FROM ticket WHERE close_date IS NOT NULL AND start_date IS NOT NULL",
            rs -> {
                if (rs.next()) {
                    return rs.getDouble("avg_hours");
                }
                return 0.0;
            });
        stats.put("avgResolutionTime", avgResolutionTime != null ? avgResolutionTime : 0.0);

        // 本周新增工单
        Long weeklyNewTickets = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ticket WHERE created_at >= CURRENT_DATE - INTERVAL '7 days'", Long.class);
        stats.put("weeklyNewTickets", weeklyNewTickets);

        // 本周解决工单
        Long weeklyResolvedTickets = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ticket WHERE ticket_status IN ('RESOLVED', 'CLOSED') " +
            "AND last_update_date >= CURRENT_DATE - INTERVAL '7 days'", Long.class);
        stats.put("weeklyResolvedTickets", weeklyResolvedTickets);

        // 最近审计日志（Admin 概览用）
        try {
            List<Map<String, Object>> recentAuditLogs = jdbcTemplate.query(
                "SELECT id, entity_type, entity_id, action, username, description, created_at FROM audit_log " +
                "ORDER BY created_at DESC LIMIT 10",
                (rs, rowNum) -> {
                    Map<String, Object> log = new HashMap<>();
                    log.put("id", rs.getLong("id"));
                    log.put("entityType", rs.getString("entity_type"));
                    log.put("entityId", rs.getLong("entity_id"));
                    log.put("action", rs.getString("action"));
                    log.put("username", rs.getString("username"));
                    log.put("description", rs.getString("description"));
                    log.put("createdAt", rs.getTimestamp("created_at"));
                    return log;
                });
            stats.put("recentAuditLogs", recentAuditLogs);
        } catch (Exception ignored) {
            // audit_log 表可能不存在时跳过
        }

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}

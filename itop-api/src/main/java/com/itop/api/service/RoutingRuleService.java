package com.itop.api.service;

import com.itop.api.dto.RoutingRuleDTO;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.Organization;
import com.itop.core.entity.RoutingRule;
import com.itop.core.entity.Team;
import com.itop.core.repository.OrganizationRepository;
import com.itop.core.repository.RoutingRuleRepository;
import com.itop.core.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 路由规则服务：CRUD + 请求匹配。
 * <p>
 * 匹配算法：按 sortOrder 升序遍历所有启用规则，返回第一条组织/类型/优先级全部命中的规则；
 * 若无命中，返回兜底规则（isFallback=true）。
 */
@Service
@RequiredArgsConstructor
public class RoutingRuleService {

    private final RoutingRuleRepository routingRuleRepository;
    private final OrganizationRepository organizationRepository;
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public List<RoutingRuleDTO> listAll() {
        return routingRuleRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoutingRuleDTO getById(Long id) {
        return routingRuleRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    public RoutingRuleDTO create(RoutingRuleDTO dto) {
        RoutingRule rule = new RoutingRule(dto.getName());
        applyDto(rule, dto);
        if (rule.getSortOrder() == null) {
            rule.setSortOrder(100);
        }
        if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }
        if (rule.getIsFallback() == null) {
            rule.setIsFallback(false);
        }
        rule = routingRuleRepository.save(rule);
        return toDTO(rule);
    }

    @Transactional
    public RoutingRuleDTO update(Long id, RoutingRuleDTO dto) {
        RoutingRule rule = routingRuleRepository.findById(id).orElse(null);
        if (rule == null) {
            return null;
        }
        applyDto(rule, dto);
        rule = routingRuleRepository.save(rule);
        return toDTO(rule);
    }

    @Transactional
    public boolean delete(Long id) {
        RoutingRule rule = routingRuleRepository.findById(id).orElse(null);
        if (rule == null) {
            return false;
        }
        if (Boolean.TRUE.equals(rule.getIsFallback())) {
            throw new IllegalArgumentException("Cannot delete the fallback rule");
        }
        routingRuleRepository.deleteById(id);
        return true;
    }

    @Transactional
    public RoutingRuleDTO setEnabled(Long id, boolean enabled) {
        RoutingRule rule = routingRuleRepository.findById(id).orElse(null);
        if (rule == null) {
            return null;
        }
        rule.setEnabled(enabled);
        rule = routingRuleRepository.save(rule);
        return toDTO(rule);
    }

    /** 重新排序：接收按顺序的规则 id 列表，依次写入 sortOrder */
    @Transactional
    public void reorder(List<Long> orderedIds) {
        if (orderedIds == null) {
            return;
        }
        for (int i = 0; i < orderedIds.size(); i++) {
            final int order = (i + 1) * 10;
            Long id = orderedIds.get(i);
            routingRuleRepository.findById(id).ifPresent(rule -> {
                rule.setSortOrder(order);
                routingRuleRepository.save(rule);
            });
        }
    }

    /**
     * 匹配请求，返回应分配的团队 ID。
     *
     * @param organizationId 请求所属组织（可空）
     * @param requestType    请求类型（可空）
     * @param priority       优先级（可空）
     * @return 命中规则的团队 ID；无命中且无兜底规则时返回 null
     */
    @Transactional(readOnly = true)
    public Long matchRequest(Long organizationId, String requestType, String priority) {
        List<RoutingRule> rules = routingRuleRepository.findByEnabledTrueOrderBySortOrderAscIdAsc();
        for (RoutingRule rule : rules) {
            if (Boolean.TRUE.equals(rule.getIsFallback())) {
                continue;
            }
            if (matches(rule, organizationId, requestType, priority)) {
                return rule.getTeamId();
            }
        }
        return routingRuleRepository.findByIsFallbackTrueAndEnabledTrue()
                .map(RoutingRule::getTeamId)
                .orElse(null);
    }

    private boolean matches(RoutingRule rule, Long organizationId, String requestType, String priority) {
        if (rule.getOrganizationId() != null && !rule.getOrganizationId().equals(organizationId)) {
            return false;
        }
        if (rule.getRequestType() != null && !rule.getRequestType().isEmpty()
                && !rule.getRequestType().equalsIgnoreCase(requestType)) {
            return false;
        }
        if (rule.getPriority() != null && !rule.getPriority().isEmpty()
                && !rule.getPriority().equalsIgnoreCase(priority)) {
            return false;
        }
        return true;
    }

    private void applyDto(RoutingRule rule, RoutingRuleDTO dto) {
        if (dto.getName() != null) {
            rule.setName(dto.getName());
        }
        rule.setDescription(dto.getDescription());
        rule.setOrganizationId(dto.getOrganizationId());
        rule.setRequestType(dto.getRequestType());
        rule.setPriority(dto.getPriority());
        rule.setTeamId(dto.getTeamId());
        if (dto.getEnabled() != null) {
            rule.setEnabled(dto.getEnabled());
        }
        if (dto.getSortOrder() != null) {
            rule.setSortOrder(dto.getSortOrder());
        }
        if (dto.getIsFallback() != null) {
            rule.setIsFallback(dto.getIsFallback());
        }
    }

    private RoutingRuleDTO toDTO(RoutingRule rule) {
        RoutingRuleDTO.RoutingRuleDTOBuilder b = RoutingRuleDTO.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .organizationId(rule.getOrganizationId())
                .requestType(rule.getRequestType())
                .priority(rule.getPriority())
                .teamId(rule.getTeamId())
                .enabled(rule.getEnabled())
                .sortOrder(rule.getSortOrder())
                .isFallback(rule.getIsFallback())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt());
        // 始终按 id 解析名称，避免更新后懒加载关系过期导致显示旧值
        if (rule.getTeamId() != null) {
            teamRepository.findById(rule.getTeamId()).map(Team::getName).ifPresent(b::teamName);
        }
        if (rule.getOrganizationId() != null) {
            organizationRepository.findById(rule.getOrganizationId()).map(Organization::getName).ifPresent(b::organizationName);
        }
        return b.build();
    }

    /** 校验团队存在性，供 controller 复用 */
    public boolean teamExists(Long teamId) {
        return teamId != null && teamRepository.existsById(teamId);
    }
}

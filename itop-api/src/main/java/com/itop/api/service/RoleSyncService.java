package com.itop.api.service;

import com.itop.core.entity.Role;
import com.itop.core.entity.Team;
import com.itop.core.entity.User;
import com.itop.core.repository.RoleRepository;
import com.itop.core.repository.TeamRepository;
import com.itop.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 根据团队成员/Leader 关系自动同步用户角色。
 * <ul>
 *   <li>任意团队成员 → REQUESTER</li>
 *   <li>IT_TEAM 成员 → TECHNICIAN</li>
 *   <li>任意团队 Leader → TEAM_LEAD</li>
 *   <li>ADMIN 角色保留不动（由 UserController 管理开关）</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RoleSyncService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public void syncUserRoles(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        List<Team> memberTeams = teamRepository.findByMemberUsersId(userId);
        List<Team> leaderTeams = teamRepository.findByLeaderUsersId(userId);

        Set<Role> newRoles = new LinkedHashSet<>();

        // Preserve ADMIN
        boolean isAdmin = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getRoleCode()));
        if (isAdmin) {
            roleRepository.findByRoleCode("ADMIN").ifPresent(newRoles::add);
        }

        // Any team membership → REQUESTER
        if (!memberTeams.isEmpty()) {
            roleRepository.findByRoleCode("REQUESTER").ifPresent(newRoles::add);
        }

        // IT_TEAM membership → TECHNICIAN
        boolean inITTeam = memberTeams.stream().anyMatch(t -> "IT_TEAM".equals(t.getTeamType()));
        if (inITTeam) {
            roleRepository.findByRoleCode("TECHNICIAN").ifPresent(newRoles::add);
        }

        // Any team leader → TEAM_LEAD
        if (!leaderTeams.isEmpty()) {
            roleRepository.findByRoleCode("TEAM_LEAD").ifPresent(newRoles::add);
        }

        user.setRoles(new ArrayList<>(newRoles));
        userRepository.save(user);
    }

    @Transactional
    public void syncUsersRoles(Collection<Long> userIds) {
        for (Long userId : userIds) {
            syncUserRoles(userId);
        }
    }
}

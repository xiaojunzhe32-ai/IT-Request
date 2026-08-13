package com.itop.api.controller;

import com.itop.api.dto.PageResponse;
import com.itop.api.dto.TeamDTO;
import com.itop.api.service.AuditService;
import com.itop.api.service.RoleSyncService;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.Team;
import com.itop.core.entity.User;
import com.itop.core.repository.TeamRepository;
import com.itop.core.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Tag(name = "Team", description = "Team management APIs")
@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final RoleSyncService roleSyncService;

    @Operation(summary = "Get all teams", description = "Retrieve a paginated list of all teams")
    @GetMapping
    @PreAuthorize("@securityUtils.hasPermission('team:read')")
    public ResponseEntity<ApiResponse<PageResponse<TeamDTO>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(name = "orgId", required = false) Long orgId,
            @RequestParam(name = "type", required = false) String type) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<Team> teamPage;

        if (orgId != null) {
            teamPage = teamRepository.findByOrganizationId(orgId, pageable);
        } else if (type != null && !type.isEmpty()) {
            teamPage = teamRepository.findByTeamType(type, pageable);
        } else {
            teamPage = teamRepository.findAll(pageable);
        }

        List<TeamDTO> dtos = teamPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<TeamDTO> response = PageResponse.of(dtos, page, size, teamPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get team by ID", description = "Retrieve a single team by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('team:read')")
    public ResponseEntity<ApiResponse<TeamDTO>> getById(@PathVariable("id") Long id) {
        return teamRepository.findById(id)
                .map(team -> ResponseEntity.ok(ApiResponse.success(toDTO(team))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Team not found")));
    }

    @Operation(summary = "Create team", description = "Create a new team")
    @PostMapping
    @PreAuthorize("@securityUtils.hasPermission('team:write')")
    public ResponseEntity<ApiResponse<TeamDTO>> create(@RequestBody TeamDTO dto) {
        // Check for duplicate team code
        if (dto.getTeamCode() != null && !dto.getTeamCode().isEmpty()) {
            if (teamRepository.existsByTeamCode(dto.getTeamCode())) {
                return ResponseEntity.ok(ApiResponse.error(400, "Team code already exists"));
            }
        }

        Team team = new Team(dto.getName(), dto.getOrganizationId());
        team.setTeamCode(dto.getTeamCode());
        team.setTeamType(dto.getTeamType() != null ? dto.getTeamType() : "IT_TEAM");
        team.setMemberUsers(resolveMembers(dto.getMemberIds()));
        team.setLeaderUsers(resolveLeaders(dto.getLeaderIds(), dto.getMemberIds()));
        team.setEmail(dto.getEmail());
        team.setPhone(dto.getPhone());
        team.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");

        team = teamRepository.save(team);
        auditService.logCreate("Team", team.getId(), "Created team: " + team.getName());

        // Sync roles for all members and leaders
        Set<Long> affectedUserIds = new HashSet<>();
        if (dto.getMemberIds() != null) affectedUserIds.addAll(dto.getMemberIds());
        if (dto.getLeaderIds() != null) affectedUserIds.addAll(dto.getLeaderIds());
        roleSyncService.syncUsersRoles(affectedUserIds);

        return ResponseEntity.ok(ApiResponse.success("Team created", toDTO(team)));
    }

    @Operation(summary = "Update team", description = "Update an existing team")
    @PutMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('team:write')")
    public ResponseEntity<ApiResponse<TeamDTO>> update(
            @PathVariable("id") Long id,
            @RequestBody TeamDTO dto) {
        return teamRepository.findById(id)
                .map(team -> {
                    // Collect old member/leader IDs before update (for role re-sync of removed users)
                    Set<Long> affectedUserIds = new HashSet<>();
                    team.getMemberUsers().forEach(u -> affectedUserIds.add(u.getId()));
                    team.getLeaderUsers().forEach(u -> affectedUserIds.add(u.getId()));

                    if (dto.getTeamCode() != null) {
                        // Check for duplicate team code
                        if (!dto.getTeamCode().equals(team.getTeamCode()) &&
                            teamRepository.existsByTeamCode(dto.getTeamCode())) {
                            throw new IllegalArgumentException("Team code already exists");
                        }
                        team.setTeamCode(dto.getTeamCode());
                    }
                    if (dto.getName() != null) {
                        team.setName(dto.getName());
                    }
                    if (dto.getTeamType() != null) {
                        team.setTeamType(dto.getTeamType());
                    }
                    if (dto.getMemberIds() != null) {
                        team.setMemberUsers(resolveMembers(dto.getMemberIds()));
                        affectedUserIds.addAll(dto.getMemberIds());
                    }
                    if (dto.getLeaderIds() != null) {
                        team.setLeaderUsers(resolveLeaders(dto.getLeaderIds(), dto.getMemberIds()));
                        affectedUserIds.addAll(dto.getLeaderIds());
                    }
                    if (dto.getEmail() != null) {
                        team.setEmail(dto.getEmail());
                    }
                    if (dto.getPhone() != null) {
                        team.setPhone(dto.getPhone());
                    }
                    if (dto.getStatus() != null) {
                        team.setStatus(dto.getStatus());
                    }

                    Team saved = teamRepository.save(team);
                    auditService.logUpdate("Team", saved.getId(), "attributes", null, null, "Updated team: " + saved.getName());

                    // Sync roles for all affected users (old + new members/leaders)
                    roleSyncService.syncUsersRoles(affectedUserIds);

                    return ResponseEntity.ok(ApiResponse.success("Team updated", toDTO(saved)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "Team not found")));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
    }

    @Operation(summary = "Delete team", description = "Delete a team by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtils.hasPermission('team:write')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        Team team = teamRepository.findById(id).orElse(null);
        if (team == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Team not found"));
        }
        // Collect member/leader IDs before deletion for role re-sync
        Set<Long> affectedUserIds = new HashSet<>();
        team.getMemberUsers().forEach(u -> affectedUserIds.add(u.getId()));
        team.getLeaderUsers().forEach(u -> affectedUserIds.add(u.getId()));

        auditService.logDelete("Team", id, "Deleted team");
        teamRepository.deleteById(id);

        // Re-sync roles for users who lost this team membership
        roleSyncService.syncUsersRoles(affectedUserIds);

        return ResponseEntity.ok(ApiResponse.success("Team deleted", null));
    }

    @Operation(summary = "Get teams by organization", description = "Get all teams for a specific organization")
    @GetMapping("/by-org/{orgId}")
    @PreAuthorize("@securityUtils.hasPermission('team:read')")
    public ResponseEntity<ApiResponse<List<TeamDTO>>> getByOrganization(@PathVariable("orgId") Long orgId) {
        List<Team> teams = teamRepository.findByOrganizationId(orgId);
        List<TeamDTO> dtos = teams.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    private TeamDTO toDTO(Team team) {
        List<Long> leaderIds = team.getLeaderUsers().stream().map(User::getId).collect(Collectors.toList());
        List<String> leaderNames = team.getLeaderUsers().stream().map(this::displayName).collect(Collectors.toList());
        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .organizationId(team.getOrganizationId())
                .organizationName(team.getOrganization() != null ? team.getOrganization().getName() : null)
                .teamCode(team.getTeamCode())
                .teamType(team.getTeamType())
                .leaderId(leaderIds.isEmpty() ? null : leaderIds.get(0))
                .leaderName(leaderNames.isEmpty() ? null : leaderNames.get(0))
                .leaderIds(leaderIds)
                .leaderNames(leaderNames)
                .memberIds(team.getMemberUsers().stream().map(User::getId).collect(Collectors.toList()))
                .memberNames(team.getMemberUsers().stream().map(this::displayName).collect(Collectors.toList()))
                .email(team.getEmail())
                .phone(team.getPhone())
                .status(team.getStatus())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }

    private List<User> resolveMembers(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) return new ArrayList<>();
        List<User> members = userRepository.findAllById(memberIds);
        if (members.size() != memberIds.stream().distinct().count()) {
            throw new IllegalArgumentException("One or more team members do not exist");
        }
        return new ArrayList<>(members);
    }

    /**
     * 解析 Leader ID 列表为 User 实体列表，并校验 Leader 必须是 Members 的子集。
     */
    private List<User> resolveLeaders(List<Long> leaderIds, List<Long> memberIds) {
        if (leaderIds == null || leaderIds.isEmpty()) return new ArrayList<>();
        List<Long> effectiveMembers = memberIds != null ? memberIds : new ArrayList<>();
        for (Long leaderId : leaderIds) {
            if (!effectiveMembers.contains(leaderId)) {
                throw new IllegalArgumentException("Leader must be a team member (user id=" + leaderId + ")");
            }
        }
        List<User> leaders = userRepository.findAllById(leaderIds);
        if (leaders.size() != leaderIds.stream().distinct().count()) {
            throw new IllegalArgumentException("One or more team leaders do not exist");
        }
        return new ArrayList<>(leaders);
    }

    private String displayName(User user) {
        String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") + " "
                + (user.getLastName() != null ? user.getLastName() : "")).trim();
        return fullName.isEmpty() ? user.getUsername() : fullName;
    }
}

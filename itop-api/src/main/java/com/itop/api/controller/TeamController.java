package com.itop.api.controller;

import com.itop.api.dto.PageResponse;
import com.itop.api.dto.TeamDTO;
import com.itop.api.service.AuditService;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.Organization;
import com.itop.core.entity.Team;
import com.itop.core.entity.User;
import com.itop.core.repository.OrganizationRepository;
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

@Tag(name = "Team", description = "Team management APIs")
@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

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
        // Validate organization exists
        Organization org = organizationRepository.findById(dto.getOrganizationId())
                .orElse(null);
        if (org == null) {
            return ResponseEntity.ok(ApiResponse.error(400, "Organization not found"));
        }

        // Check for duplicate team code
        if (dto.getTeamCode() != null && !dto.getTeamCode().isEmpty()) {
            if (teamRepository.existsByTeamCode(dto.getTeamCode())) {
                return ResponseEntity.ok(ApiResponse.error(400, "Team code already exists"));
            }
        }

        Team team = new Team(dto.getName(), dto.getOrganizationId());
        team.setTeamCode(dto.getTeamCode());
        team.setTeamType(dto.getTeamType() != null ? dto.getTeamType() : "SUPPORT");
        team.setLeaderUserId(dto.getLeaderId());
        team.setMemberUsers(resolveMembers(dto.getMemberIds()));
        team.setEmail(dto.getEmail());
        team.setPhone(dto.getPhone());
        team.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");

        team = teamRepository.save(team);
        auditService.logCreate("Team", team.getId(), "Created team: " + team.getName());
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
                    team.setLeaderUserId(dto.getLeaderId());
                    if (dto.getMemberIds() != null) team.setMemberUsers(resolveMembers(dto.getMemberIds()));
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
        if (!teamRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "Team not found"));
        }
        auditService.logDelete("Team", id, "Deleted team");
        teamRepository.deleteById(id);
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
        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .organizationId(team.getOrganizationId())
                .organizationName(team.getOrganization() != null ? team.getOrganization().getName() : null)
                .teamCode(team.getTeamCode())
                .teamType(team.getTeamType())
                .leaderId(team.getLeaderUserId())
                .leaderName(team.getLeaderUser() != null ? displayName(team.getLeaderUser()) : null)
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
        if (memberIds == null || memberIds.isEmpty()) return new java.util.ArrayList<>();
        List<User> members = userRepository.findAllById(memberIds);
        if (members.size() != memberIds.stream().distinct().count()) {
            throw new IllegalArgumentException("One or more team members do not exist");
        }
        return new java.util.ArrayList<>(members);
    }

    private String displayName(User user) {
        String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") + " "
                + (user.getLastName() != null ? user.getLastName() : "")).trim();
        return fullName.isEmpty() ? user.getUsername() : fullName;
    }
}

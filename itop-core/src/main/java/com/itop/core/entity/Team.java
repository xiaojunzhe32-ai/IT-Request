package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Team - 支持团队/处理团队
 * 用于工单分派，如服务台一线、应用运维组、网络运维组等
 */
@Entity
@Table(name = "team")
@Getter
@Setter
@NoArgsConstructor
public class Team extends BaseEntity {

    @Column(name = "org_id")
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organization organization;

    @Column(name = "team_code", unique = true, length = 50)
    private String teamCode;

    @Column(name = "team_type", length = 50)
    private String teamType = "IT_TEAM"; // IT_TEAM (can receive requests), USER_TEAM

    @Column(name = "leader_id")
    private Long leaderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", insertable = false, updatable = false)
    private Person leader;

    @Column(name = "leader_user_id")
    private Long leaderUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_user_id", insertable = false, updatable = false)
    private User leaderUser;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "team_member",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private List<Person> members = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "team_user_member",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> memberUsers = new ArrayList<>();

    /**
     * 团队负责人列表（多 Leader）。
     * Leader 必须是 Members 的子集，存储在 team_user_leader 关联表中。
     * Leader 仅作为标签，不赋予额外的团队管理权限。
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "team_user_leader",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> leaderUsers = new ArrayList<>();

    public Team(String name, Long organizationId) {
        this.setName(name);
        this.organizationId = organizationId;
    }
}

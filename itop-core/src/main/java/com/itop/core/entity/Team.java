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

    @Column(name = "org_id", nullable = false)
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organization organization;

    @Column(name = "team_code", unique = true, length = 50)
    private String teamCode;

    @Column(name = "team_type", length = 50)
    private String teamType = "SUPPORT"; // HELPDESK, SUPPORT, CHANGE, PROBLEM

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

    public Team(String name, Long organizationId) {
        this.setName(name);
        this.organizationId = organizationId;
    }
}

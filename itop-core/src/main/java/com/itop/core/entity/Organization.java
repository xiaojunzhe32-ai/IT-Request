package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Organization entity
 * Represents companies, departments, or teams
 */
@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorColumn(name = "org_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("company")
public class Organization extends BaseEntity {

    @Column(length = 255)
    private String code;

    @Column(name = "parent_id")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Organization parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Organization> children = new ArrayList<>();

    @Column(length = 100)
    private String type = "company";

    @Column(length = 255)
    private String address;

    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 50)
    private String website;

    public Organization(String name) {
        this.setName(name);
    }
}
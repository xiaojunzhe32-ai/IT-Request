package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Person entity
 * Represents individuals (employees, contractors, etc.)
 */
@Entity
@Table(name = "person")
@DiscriminatorValue("person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Person extends Contact {

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 50)
    private String function;

    @Column(length = 100)
    private String manager;

    @Column(name = "manager_id")
    private Long managerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", insertable = false, updatable = false)
    private Person managerPerson;

    @ManyToMany(mappedBy = "members")
    private java.util.List<Team> teams = new java.util.ArrayList<>();

    public Person(String firstName, String lastName) {
        super(firstName + " " + lastName);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        // Auto-generate name from first/last name
        if (this.firstName != null && this.lastName != null) {
            super.setName(this.firstName + " " + this.lastName);
        }
    }
}
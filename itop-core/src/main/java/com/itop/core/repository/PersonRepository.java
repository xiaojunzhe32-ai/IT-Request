package com.itop.core.repository;

import com.itop.core.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long>,
        JpaSpecificationExecutor<Person>,
        QuerydslPredicateExecutor<Person> {

    List<Person> findByOrganizationId(Long organizationId);

    List<Person> findByLastName(String lastName);

    List<Person> findByEmail(String email);
}
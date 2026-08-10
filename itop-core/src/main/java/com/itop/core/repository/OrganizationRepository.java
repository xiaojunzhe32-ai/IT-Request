package com.itop.core.repository;

import com.itop.core.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>,
        JpaSpecificationExecutor<Organization>,
        QuerydslPredicateExecutor<Organization> {

    Optional<Organization> findByCode(String code);

    List<Organization> findByParentId(Long parentId);

    List<Organization> findByType(String type);

    List<Organization> findByStatus(String status);

    boolean existsByCode(String code);
}
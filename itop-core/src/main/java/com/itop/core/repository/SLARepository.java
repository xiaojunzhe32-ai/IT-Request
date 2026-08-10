package com.itop.core.repository;

import com.itop.core.entity.SLA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SLARepository extends JpaRepository<SLA, Long>,
        JpaSpecificationExecutor<SLA>,
        QuerydslPredicateExecutor<SLA> {

    Optional<SLA> findByCode(String code);

    List<SLA> findByOrganizationId(Long orgId);

    List<SLA> findByPriority(String priority);

    Optional<SLA> findByIsDefaultTrue();

    List<SLA> findByOrganizationIdAndIsDefaultTrue(Long orgId);

    List<SLA> findByStatus(String status);

    boolean existsByCode(String code);
}
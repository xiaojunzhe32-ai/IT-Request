package com.itop.core.repository;

import com.itop.core.entity.SLAPriorityMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SLAPriorityMatrixRepository extends JpaRepository<SLAPriorityMatrix, Long>,
        JpaSpecificationExecutor<SLAPriorityMatrix>,
        QuerydslPredicateExecutor<SLAPriorityMatrix> {

    List<SLAPriorityMatrix> findBySlaId(Long slaId);

    Optional<SLAPriorityMatrix> findBySlaIdAndUrgencyAndImpact(Long slaId, String urgency, String impact);

    List<SLAPriorityMatrix> findByPriority(String priority);
}
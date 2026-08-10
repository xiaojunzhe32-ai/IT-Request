package com.itop.core.repository;

import com.itop.core.entity.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>,
        JpaSpecificationExecutor<Problem> {

    Optional<Problem> findByProblemNumber(String problemNumber);

    List<Problem> findByOrganizationId(Long organizationId);

    Page<Problem> findByOrganizationId(Long organizationId, Pageable pageable);

    List<Problem> findByTeamId(Long teamId);

    List<Problem> findByAgentId(Long agentId);

    Page<Problem> findByProblemType(Problem.ProblemType problemType, Pageable pageable);

    boolean existsByProblemNumber(String problemNumber);
}
package com.itop.core.repository;

import com.itop.core.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>,
        JpaSpecificationExecutor<Team>,
        QuerydslPredicateExecutor<Team> {

    Optional<Team> findByTeamCode(String teamCode);

    List<Team> findByOrganizationId(Long organizationId);

    Page<Team> findByOrganizationId(Long organizationId, Pageable pageable);

    List<Team> findByTeamType(String teamType);

    Page<Team> findByTeamType(String teamType, Pageable pageable);

    List<Team> findByLeaderId(Long leaderId);

    List<Team> findDistinctByMemberUsersIdOrLeaderUserId(Long memberUserId, Long leaderUserId);

    boolean existsByTeamCode(String teamCode);
}

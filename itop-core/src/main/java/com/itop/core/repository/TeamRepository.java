package com.itop.core.repository;

import com.itop.core.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>,
        JpaSpecificationExecutor<Team>,
        QuerydslPredicateExecutor<Team> {

    Optional<Team> findByTeamCode(String teamCode);

    List<Team> findByOrganizationId(Long organizationId);

    Page<Team> findByOrganizationId(Long organizationId, Pageable pageable);

    List<Team> findByTeamType(String teamType);

    List<Team> findByTeamTypeIgnoreCase(String teamType);

    Page<Team> findByTeamType(String teamType, Pageable pageable);

    Page<Team> findByTeamTypeIgnoreCase(String teamType, Pageable pageable);

    List<Team> findByLeaderId(Long leaderId);

    List<Team> findDistinctByMemberUsersIdOrLeaderUserId(Long memberUserId, Long leaderUserId);

    List<Team> findByMemberUsersId(Long userId);

    List<Team> findByLeaderUsersId(Long userId);

    boolean existsByTeamCode(String teamCode);

    /** 查询给定团队集合下所有成员的用户 ID（用于工单可见性：提单人所在团队的成员可见）。 */
    @Query("SELECT u.id FROM Team t JOIN t.memberUsers u WHERE t.id IN :teamIds")
    Set<Long> findMemberUserIdsByTeamIdIn(@Param("teamIds") Collection<Long> teamIds);
}

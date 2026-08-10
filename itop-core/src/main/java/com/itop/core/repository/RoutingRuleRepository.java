package com.itop.core.repository;

import com.itop.core.entity.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Long>, JpaSpecificationExecutor<RoutingRule> {

    /** 所有启用的规则按 sortOrder 升序（含兜底规则，调用方自行处理） */
    List<RoutingRule> findByEnabledTrueOrderBySortOrderAscIdAsc();

    /** 全部规则按 sortOrder 升序 */
    List<RoutingRule> findAllByOrderBySortOrderAscIdAsc();

    /** 兜底规则 */
    Optional<RoutingRule> findByIsFallbackTrueAndEnabledTrue();
}

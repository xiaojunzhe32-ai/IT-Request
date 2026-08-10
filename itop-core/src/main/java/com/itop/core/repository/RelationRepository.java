package com.itop.core.repository;

import com.itop.core.entity.LnKRelation;
import com.itop.core.entity.ConfigurationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationRepository extends JpaRepository<LnKRelation, Long>,
        JpaSpecificationExecutor<LnKRelation>,
        QuerydslPredicateExecutor<LnKRelation> {

    List<LnKRelation> findBySourceCI(ConfigurationItem sourceCI);

    List<LnKRelation> findByTargetCI(ConfigurationItem targetCI);

    List<LnKRelation> findByRelationType(LnKRelation.RelationType relationType);
}
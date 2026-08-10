package com.itop.core.repository;

import com.itop.core.entity.ConfigurationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigurationItemRepository extends JpaRepository<ConfigurationItem, Long>,
        JpaSpecificationExecutor<ConfigurationItem>,
        QuerydslPredicateExecutor<ConfigurationItem> {

    List<ConfigurationItem> findByOrganizationId(Long organizationId);

    List<ConfigurationItem> findByStatus(String status);

    Page<ConfigurationItem> findByStatus(String status, Pageable pageable);

    List<ConfigurationItem> findByFinalClass(String finalClass);

    Page<ConfigurationItem> findByFinalClass(String finalClass, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT ci.finalClass FROM ConfigurationItem ci ORDER BY ci.finalClass")
    List<String> findDistinctFinalClasses();
}
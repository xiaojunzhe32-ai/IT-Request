package com.itop.core.repository;

import com.itop.core.entity.ServiceFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceFamilyRepository extends JpaRepository<ServiceFamily, Long>,
        JpaSpecificationExecutor<ServiceFamily>,
        QuerydslPredicateExecutor<ServiceFamily> {

    Optional<ServiceFamily> findByCode(String code);

    List<ServiceFamily> findByStatus(String status);

    List<ServiceFamily> findAllByOrderBySortOrderAsc();

    boolean existsByCode(String code);
}
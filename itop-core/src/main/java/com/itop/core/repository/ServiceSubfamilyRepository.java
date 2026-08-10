package com.itop.core.repository;

import com.itop.core.entity.ServiceSubfamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceSubfamilyRepository extends JpaRepository<ServiceSubfamily, Long>,
        JpaSpecificationExecutor<ServiceSubfamily>,
        QuerydslPredicateExecutor<ServiceSubfamily> {

    Optional<ServiceSubfamily> findByCode(String code);

    List<ServiceSubfamily> findByFamilyId(Long familyId);

    org.springframework.data.domain.Page<ServiceSubfamily> findByFamilyId(Long familyId, org.springframework.data.domain.Pageable pageable);

    List<ServiceSubfamily> findByFamilyIdOrderBySortOrderAsc(Long familyId);

    List<ServiceSubfamily> findByStatus(String status);

    boolean existsByCode(String code);
}
package com.itop.core.repository;

import com.itop.core.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long>,
        JpaSpecificationExecutor<Service>,
        QuerydslPredicateExecutor<Service> {

    Optional<Service> findByCode(String code);

    List<Service> findBySubfamilyId(Long subfamilyId);

    org.springframework.data.domain.Page<Service> findBySubfamilyId(Long subfamilyId, org.springframework.data.domain.Pageable pageable);

    List<Service> findBySubfamilyIdOrderBySortOrderAsc(Long subfamilyId);

    List<Service> findByServiceType(String serviceType);

    org.springframework.data.domain.Page<Service> findByServiceType(String serviceType, org.springframework.data.domain.Pageable pageable);

    List<Service> findByStatus(String status);

    List<Service> findByOrganizationId(Long orgId);

    boolean existsByCode(String code);
}
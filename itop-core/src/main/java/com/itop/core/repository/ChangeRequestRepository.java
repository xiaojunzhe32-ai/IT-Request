package com.itop.core.repository;

import com.itop.core.entity.ChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long>,
        JpaSpecificationExecutor<ChangeRequest> {

    Optional<ChangeRequest> findByChangeNumber(String changeNumber);

    List<ChangeRequest> findByOrganizationId(Long organizationId);

    Page<ChangeRequest> findByOrganizationId(Long organizationId, Pageable pageable);

    List<ChangeRequest> findByTeamId(Long teamId);

    List<ChangeRequest> findByChangeOwnerId(Long changeOwnerId);

    Page<ChangeRequest> findByChangeType(ChangeRequest.ChangeType changeType, Pageable pageable);

    Page<ChangeRequest> findByChangeCategory(ChangeRequest.ChangeCategory changeCategory, Pageable pageable);

    boolean existsByChangeNumber(String changeNumber);
}
package com.itop.core.repository;

import com.itop.core.entity.KnownError;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnownErrorRepository extends JpaRepository<KnownError, Long>,
        JpaSpecificationExecutor<KnownError> {

    Optional<KnownError> findByErrorCode(String errorCode);

    List<KnownError> findByProblemId(Long problemId);

    Page<KnownError> findByProblemId(Long problemId, Pageable pageable);

    Page<KnownError> findByErrorType(KnownError.ErrorType errorType, Pageable pageable);

    Page<KnownError> findBySeverity(KnownError.Severity severity, Pageable pageable);

    @Query("SELECT ke FROM KnownError ke WHERE " +
           "LOWER(ke.symptoms) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ke.solution) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ke.workaround) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<KnownError> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByErrorCode(String errorCode);
}